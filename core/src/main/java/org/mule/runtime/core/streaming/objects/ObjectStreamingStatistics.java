/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.streaming.objects;

import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

public interface ObjectStreamingStatistics {

  /**
   * @return How many {@link CursorStreamProvider} instances are currently open
   */
  int getOpenCursorProvidersCount();

  /**
   * @return How many {@link CursorStream} instances are currently open
   */
  int getOpenCursorsCount();
}
