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
public class MethodDefinition {
    private String name;
    @Builder.Default
    private TypeName returnType = TypeName.get(void.class);
    @Singular
    private List<Modifier> modifiers;
    @Singular
    private List<FieldDefinition> params;
    @Singular
    private List<AnnotationDefinition> annotations;
    @Singular
    private List<TypeVariableName> typeVariableNames;
    private CodeBlock methodBody;
}
