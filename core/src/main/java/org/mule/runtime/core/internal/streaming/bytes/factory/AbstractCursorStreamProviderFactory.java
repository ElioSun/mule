/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.streaming.bytes.factory;

import static org.mule.runtime.core.api.functional.Either.left;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.functional.Either;
import org.mule.runtime.core.internal.streaming.CursorManager;
import org.mule.runtime.core.internal.streaming.bytes.ByteBufferManager;
import org.mule.runtime.core.internal.streaming.bytes.ByteStreamingManagerAdapter;
import org.mule.runtime.core.internal.streaming.bytes.CursorStreamAdapter;
import org.mule.runtime.core.internal.streaming.bytes.CursorStreamProviderAdapter;
import org.mule.runtime.core.streaming.bytes.CursorStreamProviderFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Base implementation of {@link CursorStreamProviderFactory} which contains all the base behaviour and template
 * methods.
 * <p>
 * It interacts with the {@link ByteStreamingManagerAdapter} in order to track all allocated resources and make
 * sure they're properly disposed of once they're no longer necessary.
 *
 * @since 4.0
 */
public abstract class AbstractCursorStreamProviderFactory implements CursorStreamProviderFactory {

  private final CursorManager cursorManager;
  private final ByteBufferManager bufferManager;

  /**
   * Creates a new instance
   *
   * @param cursorManager the manager which will track the produced providers.
   * @param bufferManager    the {@link ByteBufferManager} that will be used to allocate all buffers
   */
  protected AbstractCursorStreamProviderFactory(CursorManager cursorManager, ByteBufferManager bufferManager) {
    this.cursorManager = cursorManager;
    this.bufferManager = bufferManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Either<CursorStreamProvider, InputStream> of(Event event, InputStream inputStream) {
    if (inputStream instanceof CursorStreamAdapter) {
      return left(((CursorStreamAdapter) inputStream).getProvider());
    }

    Either<CursorStreamProviderAdapter, InputStream> value = resolve(inputStream, event);
    return value.mapLeft(provider -> {
      cursorManager.track(provider);
      return new ManagedCursorStreamProvider(provider);
    });
  }

  /**
   * @return the {@link ByteBufferManager} that <b>MUST</b> to be used to allocate byte buffers
   */
  protected ByteBufferManager getBufferManager() {
    return bufferManager;
  }

  /**
   * Implementations should use this method to actually create the output value
   *
   * @param inputStream
   * @param event
   * @return
   */
  protected abstract Either<CursorStreamProviderAdapter, InputStream> resolve(InputStream inputStream, Event event);





}
