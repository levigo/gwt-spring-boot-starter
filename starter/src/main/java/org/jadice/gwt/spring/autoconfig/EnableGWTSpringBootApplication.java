package org.jadice.gwt.spring.autoconfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to enable support for GWT in a Spring Boot application. As
 * argument for this annotation list all GWT modules which shall be deployed.
 */
@Target({
    ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableGWTSpringBootApplication {

  /**
   * The GWT module names to run on startup
   */
  String[] value() default {};

}