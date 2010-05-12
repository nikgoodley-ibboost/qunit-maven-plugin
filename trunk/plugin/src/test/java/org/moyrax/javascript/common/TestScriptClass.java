package org.moyrax.javascript.common;

import org.junit.Ignore;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.Script;

@Ignore
@Script
public class TestScriptClass {

  @Function
  public void testProc(final String msg, int any) {
    System.out.println(msg + ": " + any);
  }

  public String getClassName() {
    return "SomeScriptable";
  }
}
