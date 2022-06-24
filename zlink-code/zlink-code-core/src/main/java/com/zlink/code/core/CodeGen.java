package com.zlink.code.core;

import com.squareup.javapoet.*;
import com.zlink.code.api.*;
import lombok.Data;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.io.*;
import java.util.List;
import java.util.Map;


public class CodeGen {

    public static void main(String[] args) throws Exception {
        ClassDefinition classDefinition = ClassDefinition.builder()
                .javaFileRootDir("zlink-code/zlink-code-core/src/main/java/")
                .packageName("com.zlink.code.core")
                .className("TestGenCode")
                //.superClass(TypeName.get(String.class))
                .annotation(AnnotationDefinition.builder().type(Data.class).build())
                .modifier(Modifier.PUBLIC)
                //.implClass(ParameterizedTypeName.get(ClassName.get(Map.class), TypeVariableName.get("T"),TypeVariableName.get("S")))
                .typeVariableName(TypeVariableName.get("T"))
                .field(FieldDefinition.builder()
                        .modifier(Modifier.PRIVATE)
                        .type(ParameterizedTypeName.get(ClassName.get(List.class), TypeVariableName.get("T")))
                        .name("obj")
                        .build())
                .field(FieldDefinition.builder()
                        .modifier(Modifier.PRIVATE)
                        .type(TypeName.get(String.class))
                        .name("name")
                        .build())
                .constructor(MethodDefinition.builder()
                        .modifier(Modifier.PUBLIC)
                        .param(FieldDefinition.builder()
                                .type(ParameterizedTypeName.get(ClassName.get(List.class), TypeVariableName.get("T")))
                                .name("obj")
                                .build())
                        .methodBody(CodeBlock.builder()
                                .addStatement("this.$L = $L", "obj", "obj")
                                .build())
                        .build())
                .method(MethodDefinition.builder()
                        .modifier(Modifier.PUBLIC)
                        .returnType(TypeName.get(void.class))
                        .name("init")
                        .methodBody(CodeBlock.builder()
                                .beginControlFlow("for($T str: $L)", TypeVariableName.get("T"), "obj")
                                .addStatement("System.out.println($L.toString())", "str")
                                .endControlFlow()
                                .build())
                        .build())
                .method(MethodDefinition.builder()
                        .modifier(Modifier.PUBLIC)
                        .returnType(ParameterizedTypeName.get(ClassName.get(List.class), TypeVariableName.get("T")))
                        .name("getObj")
                        .methodBody(CodeBlock.builder()
                                .addStatement("return $L", "obj")
                                .build())
                        .build())
                .build();
        CodeGen codeGen = new CodeGen();
        codeGen.genCode(classDefinition);
    }

    public void genCode(ClassDefinition definition) {
        try {
            TypeSpec typeSpec = _genClass(definition);
            JavaFile javaFile = JavaFile.builder(definition.getPackageName(), typeSpec)
                    .skipJavaLangImports(true)
                    .build();
            javaFile.writeTo(new File(definition.getJavaFileRootDir()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TypeSpec _genClass(ClassDefinition definition) {
        //根据classBuilder
        TypeSpec.Builder classBuilder = _genType(definition.getType(), definition.getClassName());
        //类描述
        _modifier(classBuilder, definition.getModifiers());
        //处理继承和实现
        _superClass(classBuilder, definition.getSuperClass(), definition.getImplClasses());
        //类泛型
        _typeVariable(classBuilder, definition.getTypeVariableNames());
        //处理类的静态块
        _declare(classBuilder, definition.getStaticDeclares());
        //处理类注解
        _annotation(classBuilder, definition.getAnnotations());
        //类属性
        _field(classBuilder, definition.getFields());
        //构造方法
        _constructor(classBuilder, definition.getConstructors());
        //普通方法
        _method(classBuilder, definition.getMethods());
        //内部类
        _innerClass(classBuilder, definition.getInnerClasses());
        return classBuilder.build();
    }

    private TypeSpec.Builder _genType(ElementKind type, String name) {
        switch (type) {
            case CLASS:
                return TypeSpec.classBuilder(name);
            case INTERFACE:
                return TypeSpec.interfaceBuilder(name);
            case ENUM:
                return TypeSpec.enumBuilder(name);
            case ANNOTATION_TYPE:
                return TypeSpec.annotationBuilder(name);
            default:
                return null;
        }
    }

    private void _modifier(TypeSpec.Builder classBuilder, List<Modifier> modifiers) {
        if (modifiers != null) {
            modifiers.forEach(modifier -> {
                classBuilder.addModifiers(modifier);
            });
        }
    }

    private void _superClass(TypeSpec.Builder classBuilder, TypeName superClass, List<TypeName> implClass) {
        if (superClass != null) {
            classBuilder.superclass(superClass);
        }
        if (implClass != null) {
            implClass.forEach(aClass -> {
                classBuilder.addSuperinterface(aClass);
            });
        }
    }

    /**
     * 类泛型处理
     *
     * @param classBuilder
     * @param variableNames
     */
    private void _typeVariable(TypeSpec.Builder classBuilder, List<TypeVariableName> variableNames) {
        if (variableNames != null) {
            variableNames.forEach(variableName -> {
                classBuilder.addTypeVariable(variableName);
            });
        }
    }

    /**
     * 构建类注解
     *
     * @param classBuilder
     * @param annotationDefinitions
     */
    private void _annotation(TypeSpec.Builder classBuilder, List<AnnotationDefinition> annotationDefinitions) {
        if (annotationDefinitions != null) {
            annotationDefinitions.forEach(annotationDefinition -> {
                classBuilder.addAnnotation(__annotation(annotationDefinition));
            });
        }
    }

    /**
     * 构建静态代码块
     *
     * @param classBuilder
     * @param staticDefinition
     */
    private void _declare(TypeSpec.Builder classBuilder, List<CodeBlock> staticDefinition) {
        if (staticDefinition != null) {
            staticDefinition.forEach(declareDefinition -> {
                classBuilder.addStaticBlock(declareDefinition);
            });
        }
    }

    /**
     * 构建类属性，包括静态熟悉
     *
     * @param classBuilder
     * @param fieldDefinitions
     */
    private void _field(TypeSpec.Builder classBuilder, List<FieldDefinition> fieldDefinitions) {
        if (fieldDefinitions != null) {
            fieldDefinitions.forEach(fieldDefinition -> {
                FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldDefinition.getType(), fieldDefinition.getName());
                if (fieldDefinition.getModifiers() != null) {
                    fieldDefinition.getModifiers().forEach(modifier -> {
                        fieldSpecBuilder.addModifiers(modifier);
                    });
                }
                if (fieldDefinition.getAnnotationDefinitions() != null)
                    fieldDefinition.getAnnotationDefinitions().forEach(annotationDefinition -> {
                        fieldSpecBuilder.addAnnotation(__annotation(annotationDefinition));
                    });
                if (fieldDefinition.getInitializer() != null) {
                    fieldSpecBuilder.initializer(fieldDefinition.getInitializer());
                }
                classBuilder.addField(fieldSpecBuilder.build());
            });
        }
    }

    /**
     * 构建构造函数
     *
     * @param classBuilder
     * @param constructors
     */
    private void _constructor(TypeSpec.Builder classBuilder, List<MethodDefinition> constructors) {
        if (constructors != null) {
            constructors.forEach(methodDefinition -> {
                MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder();
                classBuilder.addMethod(__method(methodSpecBuilder, methodDefinition, true));
            });
        }
    }

    /**
     * 构建方法列表
     *
     * @param classBuilder
     * @param methodDefinitionList
     */
    private void _method(TypeSpec.Builder classBuilder, List<MethodDefinition> methodDefinitionList) {
        if (methodDefinitionList != null) {
            methodDefinitionList.forEach(methodDefinition -> {
                MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodDefinition.getName());
                classBuilder.addMethod(__method(methodSpecBuilder, methodDefinition, false));
            });
        }
    }

    /**
     * 构建内部类
     *
     * @param classBuilder
     * @param classDefinitions
     */
    private void _innerClass(TypeSpec.Builder classBuilder, List<ClassDefinition> classDefinitions) {
        if (classDefinitions != null) {
            classDefinitions.forEach(classDefinition -> {
                classBuilder.addType(_genClass(classDefinition));
            });
        }
    }

    /**
     * 构建方法
     *
     * @param builder
     * @param methodDefinition
     * @return
     */
    private MethodSpec __method(MethodSpec.Builder builder, MethodDefinition methodDefinition, boolean isConstructor) {
        if (methodDefinition.getModifiers() != null) {
            methodDefinition.getModifiers().forEach(modifier -> {
                builder.addModifiers(modifier);
            });
        }
        if (methodDefinition.getAnnotations() != null) {
            methodDefinition.getAnnotations().forEach(annotationDefinition -> {
                builder.addAnnotation(__annotation(annotationDefinition));
            });
        }
        if (methodDefinition.getParams() != null) {
            methodDefinition.getParams().forEach(fieldDefinition -> {
                builder.addParameter(__parameter(fieldDefinition));
            });
        }
        if (methodDefinition.getMethodBody() != null) {
            builder.addCode(methodDefinition.getMethodBody());
        }
        if (!isConstructor && methodDefinition.getReturnType() != null) {
            builder.returns(methodDefinition.getReturnType());
        }
        if (!isConstructor && methodDefinition.getTypeVariableNames() != null) {
            builder.addTypeVariables(methodDefinition.getTypeVariableNames());
        }
        return builder.build();
    }

    /**
     * 构建注解
     *
     * @param annotationDefinition
     * @return
     */
    private AnnotationSpec __annotation(AnnotationDefinition annotationDefinition) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(annotationDefinition.getType());
        if (annotationDefinition.getParams() != null) {
            annotationDefinition.getParams().forEach((k, v) -> {
                Object obj = v;
                if (v instanceof AnnotationDefinition) {
                    obj = __annotation((AnnotationDefinition) v);
                }
                builder.addMember(k, "$L", obj);
            });
        }
        return builder.build();
    }

    /**
     * 构建方法参数列表
     *
     * @param definition
     * @return
     */
    private ParameterSpec __parameter(FieldDefinition definition) {
        ParameterSpec.Builder builder = ParameterSpec.builder(definition.getType(), definition.getName());
        if (definition.getModifiers() != null) {
            definition.getModifiers().forEach(modifier -> {
                builder.addModifiers(modifier);
            });
        }
        if (definition.getAnnotationDefinitions() != null) {
            definition.getAnnotationDefinitions().forEach(annotationDefinition -> {
                builder.addAnnotation(__annotation(annotationDefinition));
            });
        }

        return builder.build();
    }
}
