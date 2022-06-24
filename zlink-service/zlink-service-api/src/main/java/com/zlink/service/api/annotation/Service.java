package com.zlink.service.api.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";

    String version() default "V1";

}
