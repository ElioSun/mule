/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.streaming.bytes.factory;

import static org.mule.runtime.core.api.functional.Either.left;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.functional.Either;
import org.mule.runtime.core.internal.streaming.CursorManager;
import org.mule.runtime.core.internal.streaming.bytes.ByteBufferManager;
import org.mule.runtime.core.internal.streaming.bytes.CursorStreamAdapter;
import org.mule.runtime.core.internal.streaming.bytes.CursorStreamProviderAdapter;
import org.mule.runtime.core.streaming.bytes.CursorStreamProviderFactory;

import java.io.InputStream;

public class ManagedCursorStreamProviderFactory implements CursorStreamProviderFactory {

  private final CursorStreamProviderFactory delegate;
  private final CursorManager cursorManager;
  private final ByteBufferManager bufferManager;

  /**
   * Creates a new instance
   *
   * @param cursorManager the manager which will track the produced providers.
   * @param bufferManager    the {@link ByteBufferManager} that will be used to allocate all buffers
   */
  protected ManagedCursorStreamProviderFactory(CursorStreamProviderFactory delegate, CursorManager cursorManager, ByteBufferManager bufferManager) {
    this.delegate = delegate;
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

    Either<CursorStreamProvider, InputStream> value = delegate.of(event, inputStream);
    return value.mapLeft(provider -> {
      CursorStreamProviderAdapter adapter = new CursorStreamProviderAdapter(provider, event);
      cursorManager.man(adapter);
      return new AbstractCursorStreamProviderFactory.ManagedCursorStreamProvider(provider);
    });
  }
  @Override
  public Either<CursorStreamProvider, InputStream> of(Event event, InputStream inputStream) {
    return null;
  }
}
