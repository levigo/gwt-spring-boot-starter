/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jadice.gwt.spring.autoconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link BeanFactoryPostProcessor} that registers beans for GWT-Servlet components found via
 * package scanning.
 *
 * This class ist in large parts a copy of {@link GWTRPCServletComponentRegisteringPostProcessor}
 * which isn't extensible, unfortunately.
 * 
 * @see ServletComponentScan
 * @see GWTRPCServletComponentScanRegistrar
 */
class GWTRPCServletComponentRegisteringPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

  private static final List<GWTRPCServletComponentHandler> HANDLERS;

  static {
    List<GWTRPCServletComponentHandler> handlers = new ArrayList<GWTRPCServletComponentHandler>();
    handlers.add(new GWTRPCWebServletHandler());
    HANDLERS = Collections.unmodifiableList(handlers);
  }

  private final Set<String> packagesToScan;

  private ApplicationContext applicationContext;

  GWTRPCServletComponentRegisteringPostProcessor(final Set<String> packagesToScan) {
    this.packagesToScan = packagesToScan;
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
    if (isRunningInEmbeddedContainer()) {
      ClassPathScanningCandidateComponentProvider componentProvider = createComponentProvider();
      for (String packageToScan : this.packagesToScan) {
        scanPackage(componentProvider, packageToScan);
      }
    }
  }

  private void scanPackage(final ClassPathScanningCandidateComponentProvider componentProvider,
      final String packageToScan) {
    for (BeanDefinition candidate : componentProvider.findCandidateComponents(packageToScan)) {
      if (candidate instanceof ScannedGenericBeanDefinition) {
        for (GWTRPCServletComponentHandler handler : HANDLERS) {
          handler.handle(((ScannedGenericBeanDefinition) candidate), (BeanDefinitionRegistry) this.applicationContext);
        }
      }
    }
  }

  private boolean isRunningInEmbeddedContainer() {
    return this.applicationContext instanceof WebApplicationContext
        && ((WebApplicationContext) this.applicationContext).getServletContext() == null;
  }

  private ClassPathScanningCandidateComponentProvider createComponentProvider() {
    ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
        false);
    componentProvider.setEnvironment(this.applicationContext.getEnvironment());
    componentProvider.setResourceLoader(this.applicationContext);
    for (GWTRPCServletComponentHandler handler : HANDLERS) {
      componentProvider.addIncludeFilter(handler.getTypeFilter());
    }
    return componentProvider;
  }

  Set<String> getPackagesToScan() {
    return Collections.unmodifiableSet(this.packagesToScan);
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
