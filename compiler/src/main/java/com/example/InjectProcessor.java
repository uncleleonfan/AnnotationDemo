package com.example;

import com.example.annotations.InjectInt;
import com.example.annotations.InjectString;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.example.annotations.InjectString", "com.example.annotations.InjectInt"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class InjectProcessor extends AbstractProcessor{

    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    private HashMap<String,GenerateJavaFile> mGenerateJavaFiles = new HashMap<String, GenerateJavaFile>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "InjectProcessor process");
        for (TypeElement typeElement : set) {
            messager.printMessage(Diagnostic.Kind.NOTE, "typeElement:" + typeElement.getSimpleName());
            for (Element element : roundEnvironment.getElementsAnnotatedWith(typeElement)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "element:" + element.getSimpleName());
                messager.printMessage(Diagnostic.Kind.NOTE, "element_package:" + processingEnv.getElementUtils().getPackageOf(element).getQualifiedName());
                messager.printMessage(Diagnostic.Kind.NOTE, "element_type:" + element.asType().toString());
                messager.printMessage(Diagnostic.Kind.NOTE, "element_kind:" + element.getKind().name());
            }
        }

        Set<? extends Element> stringElements = roundEnvironment.getElementsAnnotatedWith(InjectString.class);
        for (Element element : stringElements) {
            int value = element.getAnnotation(InjectString.class).value();
            messager.printMessage(Diagnostic.Kind.NOTE, "element_string_id:" + value);
            addElementToGenerateJavaFile(element, messager);
        }

        Set<? extends Element> intElements = roundEnvironment.getElementsAnnotatedWith(InjectInt.class);
        for (Element element : intElements) {
            int value = element.getAnnotation(InjectInt.class).value();
            messager.printMessage(Diagnostic.Kind.NOTE, "element_int_id:" + value );
            addElementToGenerateJavaFile(element, messager);
        }

        generateJavaFile();
        return true;
    }

    private void generateJavaFile() {
        for (String className : mGenerateJavaFiles.keySet()) {
            GenerateJavaFile file = mGenerateJavaFiles.get(className);
            MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CONTEXT, "context");
            for (Element element: file.elements) {
                if (element.asType().toString().equals("int")) {
                    builder.addStatement("(($N)$N).$N = context.getResources().getInteger(R.integer.$N)", file.className.split("_")[0], "context", element.getSimpleName(), element.getSimpleName());
                } else if (element.asType().toString().equals("java.lang.String")) {
                    builder.addStatement("(($N)$N).$N = context.getResources().getString(R.string.$N)", file.className.split("_")[0], "context", element.getSimpleName(), element.getSimpleName());
                }

            }
            TypeSpec typeSpec = TypeSpec.classBuilder(file.className)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(builder.build())
                    .build();
            JavaFile javaFile = JavaFile.builder(file.packageName, typeSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addElementToGenerateJavaFile(Element element, Messager messager) {
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        String[] split = typeElement.getQualifiedName().toString().split("\\.");
        String className = split[split.length - 1];
        messager.printMessage(Diagnostic.Kind.NOTE, "add element to generate file " + className);

        GenerateJavaFile generateJavaFile = mGenerateJavaFiles.get(className);
        if (generateJavaFile == null) {
            GenerateJavaFile file = new GenerateJavaFile();
            file.packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
            file.className = className + "_Inject";
            file.elements = new ArrayList<Element>();
            file.elements.add(element);
            mGenerateJavaFiles.put(className, file);
        } else {
            generateJavaFile.elements.add(element);
        }
    }

    private static class GenerateJavaFile {
        String packageName;
        String className;
        List<Element> elements;
    }
}
