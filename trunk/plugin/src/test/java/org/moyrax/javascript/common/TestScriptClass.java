package org.moyrax.javascript.common;

import org.junit.Ignore;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.Script;

@Ignore
@Script
public class TestScriptClass {

  @Function
  public String ping(final String msg, int any) {
    return "PONG[" + any + "]: " + msg;
  }

  public String getClassName() {
    return "SomeScriptable";
  }
}
