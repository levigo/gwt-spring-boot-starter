package org.jadice.gwt.spring.autoconfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.google.gwt.core.server.GwtServletBase;

/**
 * Use this annotation to expose a GWT RPC service as a servlet.
 */
@Target({
    ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnWebApplication
@ConditionalOnClass(value=GwtServletBase.class)
//@ServletComponentScan
@Component
@Import(GWTRPCServletComponentScanRegistrar.class)
public @interface GWTRPCService {

}