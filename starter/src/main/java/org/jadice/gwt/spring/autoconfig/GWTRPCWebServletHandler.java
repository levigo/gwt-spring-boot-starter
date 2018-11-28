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

import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.util.StringUtils;

/**
 * Handler for {@link WebServlet}-annotated classes.
 *
 * This class ist in large parts a copy of {@link WebServletHandler} which isn't extensible,
 * unfortunately.
 */
class GWTRPCWebServletHandler extends GWTRPCServletComponentHandler {

  GWTRPCWebServletHandler() {
    super(WebServlet.class);
  }

  @Override
  public void doHandle(final Map<String, Object> attributes, final ScannedGenericBeanDefinition beanDefinition,
      final BeanDefinitionRegistry registry) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServletRegistrationBean.class);
    builder.addPropertyValue("asyncSupported", attributes.get("asyncSupported"));
    builder.addPropertyValue("initParameters", extractInitParameters(attributes));
    builder.addPropertyValue("loadOnStartup", attributes.get("loadOnStartup"));
    String name = determineName(attributes, beanDefinition);
    builder.addPropertyValue("name", name);
    builder.addPropertyValue("servlet", beanDefinition);
    builder.addPropertyValue("urlMappings", extractUrlPatterns("urlPatterns", attributes));
    builder.addPropertyValue("multipartConfig", determineMultipartConfig(beanDefinition));
    registry.registerBeanDefinition(name, builder.getBeanDefinition());
  }

  private String determineName(final Map<String, Object> attributes, final BeanDefinition beanDefinition) {
    return (String) (StringUtils.hasText((String) attributes.get("name"))
        ? attributes.get("name")
        : beanDefinition.getBeanClassName());
  }

  private MultipartConfigElement determineMultipartConfig(final ScannedGenericBeanDefinition beanDefinition) {
    Map<String, Object> attributes = beanDefinition.getMetadata().getAnnotationAttributes(
        MultipartConfig.class.getName());
    if (attributes == null) {
      return null;
    }
    return new MultipartConfigElement((String) attributes.get("location"), (Long) attributes.get("maxFileSize"),
        (Long) attributes.get("maxRequestSize"), (Integer) attributes.get("fileSizeThreshold"));
  }

}
