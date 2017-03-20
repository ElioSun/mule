/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.streaming;

import static org.mule.runtime.core.internal.streaming.CursorManager.Status.DISPOSABLE;
import static org.mule.runtime.core.internal.streaming.CursorManager.Status.NORMAL;
import static org.mule.runtime.core.internal.streaming.CursorManager.Status.SURVIVOR;
import static reactor.core.publisher.Mono.from;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.EventContext;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CursorManager {

  private static Logger LOGGER = LoggerFactory.getLogger(CursorManager.class);
  
  private final LoadingCache<String, EventStreamingState> registry =
      CacheBuilder.newBuilder()
          .removalListener((RemovalNotification<String, EventStreamingState> notification) -> notification.getValue().dispose())
          .build(new CacheLoader<String, EventStreamingState>() {

            @Override
            public EventStreamingState load(String key) throws Exception {
              return new EventStreamingState();
            }
          });

  private DefaultStreamingStatistics statistics;

  public CursorProvider managed(CursorProvider provider, Event creatorEvent) {
    final EventContext ownerContext = getRoot(creatorEvent.getContext());
    registerEventContext(ownerContext);
    registry.getUnchecked(ownerContext.getId()).addProvider(provider);
    statistics.incrementOpenProviders();
    return new ManagedCursorProvider(provider, ownerContext);
  }

  public void onOpen(CursorProvider provider, Cursor cursor, EventContext ownerContext) {
    registry.getUnchecked(ownerContext.getId()).addCursor(provider, cursor);
    statistics.incrementOpenCursors();
  }

  private class ManagedCursorProvider implements CursorProvider {

    private CursorProvider delegate;
    private final EventContext ownerContext;

    public ManagedCursorProvider(CursorProvider delegate, EventContext ownerContext) {
      this.delegate = delegate;
      this.ownerContext = ownerContext;
    }

    @Override
    public Cursor openCursor() {
      Cursor cursor = delegate.openCursor();
      onOpen(delegate, cursor, ownerContext);

      return new ManagedCursorDecorator(cursor);
    }

    @Override
    public void close() {
      delegate.close();
    }

    @Override
    public boolean isClosed() {
      return delegate.isClosed();
    }
  }

  public void onClose(CursorProvider provider, Cursor cursor, EventContext rootEventContext) {
    final String eventId = rootEventContext.getId();
    EventStreamingState state = registry.getIfPresent(eventId);

    if (state != null && state.removeCursor(provider, cursor) == DISPOSABLE) {
      state.dispose();
      registry.invalidate(eventId);
    }
  }


  /*
   * Duplicate registration will occur if cursors are opened in multiple child flows or processing branches. This means terminate
   * will fire multiple times sequentially during completion of the parent EventContext. After the first terminate all other
   * invocation will literally be no-ops. This is preferred to introducing contention here given multiple thread may be opening
   * cursors concurrently.
   */
  private void registerEventContext(EventContext eventContext) {
    from(eventContext.getCompletionPublisher()).doFinally(signal -> terminated(eventContext)).subscribe();
  }

  private EventContext getRoot(EventContext eventContext) {
    return eventContext.getParentContext()
        .map(this::getRoot)
        .orElse(eventContext);
  }

  private class EventStreamingState {

    private Status status = NORMAL;
    private boolean disposed = false;

    private final LoadingCache<CursorProvider, List<Cursor>> cursors = CacheBuilder.newBuilder()
        .build(new CacheLoader<CursorProvider, List<Cursor>>() {

          @Override
          public List<Cursor> load(CursorProvider key) throws Exception {
            return new LinkedList<>();
          }
        });

    private synchronized void addProvider(CursorProvider adapter) {
      cursors.getUnchecked(adapter);
    }

    private Status terminate() {
      if (cursors.size() == 0) {
        status = DISPOSABLE;
      } else {
        boolean allCursorsClosed = true;
        for (Map.Entry<CursorProvider, List<Cursor>> entry : cursors.asMap().entrySet()) {
          closeProvider(entry.getKey());
          final List<Cursor> cursors = entry.getValue();
          if (!cursors.isEmpty()) {
            allCursorsClosed = allCursorsClosed && cursors.stream().allMatch(Cursor::isClosed);
          }
        }
        status = allCursorsClosed ? DISPOSABLE : SURVIVOR;
      }
      return status;
    }

    private void addCursor(CursorProvider provider, Cursor cursor) {
      cursors.getUnchecked(provider).add(cursor);
    }

    private Status removeCursor(CursorProvider provider, Cursor cursor) {
      List<Cursor> openCursors = cursors.getUnchecked(provider);
      if (openCursors.remove(cursor)) {
        statistics.decrementOpenCursors();
      }

      if (openCursors.isEmpty()) {
        if (provider.isClosed() || status == SURVIVOR) {
          dispose();
          status = DISPOSABLE;
          cursors.invalidate(provider);
        }
      }

      return status;
    }

    private void dispose() {
      if (disposed) {
        return;
      }

      cursors.asMap().forEach((provider, cursors) -> {
        try {
          closeProvider(provider);
          closeAll(cursors);
        } finally {
          LifecycleUtils.disposeIfNeeded(provider, LOGGER);
        }
      });

      disposed = true;
    }

    private void closeAll(List<Cursor> cursors) {
      cursors.forEach(cursor -> {
        try {
          cursor.close();
          statistics.decrementOpenCursors();
        } catch (Exception e) {
          LOGGER.warn("Exception was found trying to close cursor. Execution will continue", e);
        }
      });
    }

    private void closeProvider(CursorProvider provider) {
      if (!provider.isClosed()) {
        provider.close();
        statistics.decrementOpenProviders();
      }
    }
  }


  enum Status {
    NORMAL, SURVIVOR, DISPOSABLE
  }

  private class ManagedCursorDecorator extends CursorStream {

    private final CursorStream delegate;
    private final CursorProvider provider;
    private final EventContext ownerEventContext;

    private ManagedCursorDecorator(CursorStream delegate, CursorProvider provider, EventContext ownerEventContext) {
      this.delegate = delegate;
      this.provider = provider;
      this.ownerEventContext = ownerEventContext;
    }

    @Override
    public void close() throws IOException {
      try {
        delegate.close();
      } finally {
        onClose(provider, delegate, ownerEventContext);
      }
    }

    @Override
    public long getPosition() {
      return delegate.getPosition();
    }

    @Override
    public void seek(long position) throws IOException {
      delegate.seek(position);
    }

    @Override
    public boolean isClosed() {
      return delegate.isClosed();
    }

    @Override
    public int read() throws IOException {
      return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
      return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
      return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
      return delegate.available();
    }

    @Override
    public void mark(int readlimit) {
      delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
      delegate.reset();
    }

    @Override
    public boolean markSupported() {
      return delegate.markSupported();
    }
  }
}
