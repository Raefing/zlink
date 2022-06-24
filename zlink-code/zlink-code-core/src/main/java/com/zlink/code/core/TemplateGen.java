package com.zlink.code.core;

import com.zlink.code.api.ClassDefinition;

public class TemplateGen {
    public ClassDefinition genTemplate(Class clazz) {
        Class superClass = clazz.getSuperclass();
        Class[] interfaces = clazz.getInterfaces();
        return null;
    }
}
