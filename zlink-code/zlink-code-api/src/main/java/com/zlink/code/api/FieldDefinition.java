package com.zlink.code.api;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import javax.lang.model.element.Modifier;
import java.util.List;

@Data
@Builder
public class FieldDefinition {
    private TypeName type;
    private String name;
    @Singular
    private List<Modifier> modifiers;
    @Singular
    private List<AnnotationDefinition> annotationDefinitions;
    private CodeBlock initializer;
}
