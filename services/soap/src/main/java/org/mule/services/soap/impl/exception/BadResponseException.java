/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.services.soap.impl.exception;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.exception.MuleRuntimeException;

/**
 * {@link Exception} implementation that aims to be thrown when an error occur while parsing or processing the SOAP response.
 *
 * @since 4.0
 */
public class BadResponseException extends MuleRuntimeException {

  public BadResponseException(String message) {
    super(createStaticMessage(message));
  }

  public BadResponseException(String message, Throwable cause) {
    super(createStaticMessage(message), cause);
  }
}
