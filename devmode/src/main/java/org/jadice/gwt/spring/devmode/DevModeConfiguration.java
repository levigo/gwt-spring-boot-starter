package org.jadice.gwt.spring.devmode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gwt.dev.jjs.JsOutputOption;
import com.google.gwt.dev.util.arg.OptionScriptStyle;

@ConfigurationProperties(prefix = "gwt")
@Component
public class DevModeConfiguration implements OptionScriptStyle {
  private JsOutputOption outputOption = JsOutputOption.PRETTY;
  
  private boolean generateJsInteropExports = true;

  @Override
  public JsOutputOption getOutput() {
    return outputOption;
  }

  @Override
  public void setOutput(final JsOutputOption obfuscated) {
    this.outputOption = obfuscated;
  }

  public boolean isGenerateJsInteropExports() {
    return generateJsInteropExports;
  }

  public void setGenerateJsInteropExports(boolean generateJsInteropExports) {
    this.generateJsInteropExports = generateJsInteropExports;
  }
}
