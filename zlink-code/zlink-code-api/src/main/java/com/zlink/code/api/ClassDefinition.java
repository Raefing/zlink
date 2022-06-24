package com.zlink.code.api;


import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.List;

@Data
@Builder
public class ClassDefinition {
    private String javaFileRootDir;
    private String packageName;
    private String className;
    @Singular
    private List<Modifier> modifiers ;
    @Builder.Default
    private ElementKind type = ElementKind.CLASS;
    private TypeName superClass;
    @Singular
    private List<TypeName> implClasses;
    @Singular
    private List<TypeVariableName> typeVariableNames;
    @Singular
    private List<CodeBlock> staticDeclares;
    @Singular
    private List<AnnotationDefinition> annotations;
    @Singular
    private List<FieldDefinition> fields;
    @Singular
    private List<MethodDefinition> constructors;
    @Singular
    private List<MethodDefinition> methods;
    @Singular
    private List<ClassDefinition> innerClasses;
}
