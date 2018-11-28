/**
 * <pre>
 * Copyright (c), levigo holding gmbh.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * </pre>
 */
package org.jadice.gwt.spring.demo.server;

import org.jadice.gwt.spring.autoconfig.EnableGWTSpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * This class illustrates a minimal Spring Boot application which uses GWT support. It exposes a
 * minimal GWT-RPC service which is consumed by the client-side part of the application.
 */
@SpringBootApplication

// Activate GWT support specifiying the GWT module name for the entry point
@EnableGWTSpringBootApplication("org.jadice.gwt.spring.demo.Application")

// Activate scanning for RPC service servlets so that TimeServiceImpl is picked up
@ServletComponentScan
public class DemoApplication extends SpringBootServletInitializer {
  public static void main(final String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
