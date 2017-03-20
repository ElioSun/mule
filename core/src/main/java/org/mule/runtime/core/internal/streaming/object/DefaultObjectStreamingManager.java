/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.streaming.object;

import org.mule.runtime.core.streaming.objects.CursorIteratorProviderFactory;
import org.mule.runtime.core.streaming.objects.InMemoryCursorIteratorConfig;
import org.mule.runtime.core.streaming.objects.ObjectStreamingManager;

public class DefaultObjectStreamingManager implements ObjectStreamingManager {

  @Override
  public CursorIteratorProviderFactory getInMemoryCursorProviderFactory(InMemoryCursorIteratorConfig config) {
    return null;
  }

  @Override
  public CursorIteratorProviderFactory getNullCursorProviderFactory() {
    return null;
  }

  @Override
  public CursorIteratorProviderFactory getDefaultCursorProviderFactory() {
    return null;
  }
}
