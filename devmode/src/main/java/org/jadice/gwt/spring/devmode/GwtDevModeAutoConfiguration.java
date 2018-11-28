package org.jadice.gwt.spring.devmode;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.devtools.autoconfigure.DevToolsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gwt.dev.DevMode;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({
    DevMode.class, DevToolsProperties.class
})
public class GwtDevModeAutoConfiguration {
  @Bean
  public DevModeLauncher createDevModeLauncher() {
    return new DevModeLauncher();
  }
}