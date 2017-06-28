package com.example;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.example.annotations.InjectString", "com.example.annotations.InjectInt"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class InjectProcessor extends AbstractProcessor{

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "InjectProcessor process");
        for (TypeElement typeElement : set) {
            messager.printMessage(Diagnostic.Kind.NOTE, "typeElement:" + typeElement.getSimpleName());
            for (Element element : roundEnvironment.getElementsAnnotatedWith(typeElement)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "element:" + element.getSimpleName());
                messager.printMessage(Diagnostic.Kind.NOTE, "element_package:" + processingEnv.getElementUtils().getPackageOf(element));
                messager.printMessage(Diagnostic.Kind.NOTE, "element_type:" + element.asType().toString());
                messager.printMessage(Diagnostic.Kind.NOTE, "element_kind:" + element.getKind().name());
            }
        }
        return true;
    }
}
