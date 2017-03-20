/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.factories.streaming;

import org.mule.runtime.core.streaming.objects.CursorIteratorProviderFactory;
import org.mule.runtime.core.streaming.objects.InMemoryCursorIteratorConfig;

public class InMemoryCursorIteratorProviderObjectFactory
    extends AbstractCursorProviderObjectFactory<CursorIteratorProviderFactory> {

  private final int initialBufferSize;
  private final int bufferSizeIncrement;
  private final int maxInMemorySize;

  public InMemoryCursorIteratorProviderObjectFactory(int initialBufferSize, int bufferSizeIncrement, int maxInMemorySize) {
    this.initialBufferSize = initialBufferSize;
    this.bufferSizeIncrement = bufferSizeIncrement;
    this.maxInMemorySize = maxInMemorySize;
  }

  @Override
  public CursorIteratorProviderFactory doGetObject() throws Exception {
    InMemoryCursorIteratorConfig config =
        new InMemoryCursorIteratorConfig(initialBufferSize, bufferSizeIncrement, maxInMemorySize);

    return streamingManager.forObjects().getInMemoryCursorProviderFactory(config);
  }
}
