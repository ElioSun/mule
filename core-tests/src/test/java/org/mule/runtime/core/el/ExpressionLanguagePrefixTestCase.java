/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.el;

import static java.lang.System.lineSeparator;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.mule.runtime.core.el.mvel.MVELExpressionLanguage;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Before;
import org.junit.Test;

public class ExpressionLanguagePrefixTestCase extends AbstractMuleTestCase {

  private ExtendedExpressionLanguageAdapter elAdapter;
  private DataWeaveExpressionLanguage dwLanguage;
  private MVELExpressionLanguage melLanguage;

  @Before
  public void before() {
    dwLanguage = mock(DataWeaveExpressionLanguage.class);
    melLanguage = mock(MVELExpressionLanguage.class);
    elAdapter = new ExtendedExpressionLanguageAdapter(dwLanguage, melLanguage);
  }

  @Test
  public void singleLineNoPrefixNoMarker() {
    elAdapter.validate("expr");
    verify(dwLanguage).validate(anyString());
    verify(melLanguage, never()).validate(anyString());
  }

  @Test
  public void singleLineNoPrefixMarker() {
    elAdapter.validate("#[expr]");
    verify(dwLanguage).validate(anyString());
    verify(melLanguage, never()).validate(anyString());
  }

  @Test
  public void singleLineMelPrefixNoMarker() {
    elAdapter.validate("mel:expr");
    verify(dwLanguage, never()).validate(anyString());
    verify(melLanguage).validate(anyString());
  }

  @Test
  public void singleLineMelPrefixMarker() {
    elAdapter.validate("#[mel:expr]");
    verify(dwLanguage, never()).validate(anyString());
    verify(melLanguage).validate(anyString());
  }

  @Test
  public void multiLineNoPrefixNoMarker() {
    elAdapter.validate("expr" + lineSeparator() + "a:b");
    verify(dwLanguage).validate(anyString());
    verify(melLanguage, never()).validate(anyString());
  }

  @Test
  public void multiLineNoPrefixMarker() {
    elAdapter.validate("#[expr" + lineSeparator() + "a:b]");
    verify(dwLanguage).validate(anyString());
    verify(melLanguage, never()).validate(anyString());
  }

  @Test
  public void multiLineMelPrefixNoMarker() {
    elAdapter.validate("mel:expr" + lineSeparator() + "a:b");
    verify(dwLanguage, never()).validate(anyString());
    verify(melLanguage).validate(anyString());
  }

  @Test
  public void multiLineMelPrefixMarker() {
    elAdapter.validate("#[mel:expr" + lineSeparator() + "a:b]");
    verify(dwLanguage, never()).validate(anyString());
    verify(melLanguage).validate(anyString());
  }

  @Test
  public void paddedNoPrefixNoMarker() {
    elAdapter.validate("    expr a:b");
    verify(dwLanguage).validate(anyString());
    verify(melLanguage, never()).validate(anyString());
  }

  @Test
  public void paddedNoPrefixMarker() {
    elAdapter.validate("#[    expr a:b]");
    verify(dwLanguage).validate(anyString());
    verify(melLanguage, never()).validate(anyString());
  }

  @Test
  public void paddedMelPrefixNoMarker() {
    elAdapter.validate("    mel:expr a:b");
    verify(dwLanguage, never()).validate(anyString());
    verify(melLanguage).validate(anyString());
  }

  @Test
  public void paddedMelPrefixMarker() {
    elAdapter.validate("#[    mel:expr a:b]");
    verify(dwLanguage, never()).validate(anyString());
    verify(melLanguage).validate(anyString());
  }
}
