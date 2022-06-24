package com.zlink.code.api;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

@Data
@Builder
public class AnnotationDefinition {
    private Class type;
    @Singular
    private Map<String, Object> params;
}
