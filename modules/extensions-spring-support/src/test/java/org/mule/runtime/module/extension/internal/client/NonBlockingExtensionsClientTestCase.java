/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.client;

import org.mule.runtime.api.message.Attributes;
import org.mule.runtime.extension.api.client.OperationParameters;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features("ExtensionsClient")
@Stories("Non-Blocking Client")
public class NonBlockingExtensionsClientTestCase extends ExtensionsClientTestCase {

  @Override
  <T, A extends Attributes> Result<T, A> doExecute(String extension, String operation, OperationParameters params)
      throws Throwable {
    CompletableFuture<Result<T, A>> future = client.executeAsync(extension, operation, params);
    try {
      return future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException("Failure. The test throw an exception: " + e.getMessage(), e);
    } catch (ExecutionException e) {
      throw e.getCause();
    }
  }
}
