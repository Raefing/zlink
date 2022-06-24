package com.zlink.service.api.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServicePoint {
    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";

}
