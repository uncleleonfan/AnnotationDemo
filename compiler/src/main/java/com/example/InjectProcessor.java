package com.example;

import com.google.auto.service.AutoService;
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
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

//配置注解处理器支持处理的注解类型为InjectString和InjectInt
@SupportedAnnotationTypes({"com.example.annotations.InjectString", "com.example.annotations.InjectInt"})
//配置注解处理器支持的Java版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
//定义注解处理器继承自AbstractProcessor
public class InjectProcessor extends AbstractProcessor{

    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    //待生成java文件的的集合，key为被注解的类的类名，value为GenerateJavaFile对象
    private HashMap<String,GenerateJavaFile> mGenerateJavaFiles = new HashMap<String, GenerateJavaFile>();

    /**
     * 实现process方法，完成注解的解析和处理，通常生成文件或者校验处理
     * @param set 定义的注解类型的集合
     * @param roundEnvironment 回合环境，注解的处理可能要经过几个回合的处理，每个回合处理一批注解
     * @return 返回true表示注解被当前注解处理器处理，就不会再交给其他注解处理器
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //遍历所有的TypeElement的，一个注解类型对应一个TypeElement
        for (TypeElement typeElement : set) {
            //遍历在代码中使用typeElement对应注解类型来注解的元素
            //例如：如果typeElement对应的是InjectString注解类型，那么Element对应为使用@InjectString注解的成员变量
            for (Element element : roundEnvironment.getElementsAnnotatedWith(typeElement)) {
                //添加注解元素到将要生成的java文件对应的GenerateJavaFile的对象中
                addElementToGenerateJavaFile(element);
            }
        }
        //生成java文件
        createJavaFile();
        return true;
    }

    /**
     * 生成java文件
     */
    private void createJavaFile() {
        //遍历GenerateJavaFile集合
        for (String className : mGenerateJavaFiles.keySet()) {

            //获取一个GenerateJavaFile对象
            GenerateJavaFile file = mGenerateJavaFiles.get(className);

            //构建一个构造方法，该构造方法带有一个Context类型的参数
            MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CONTEXT, "context");

            //遍历该类中需要处理的注解元素
            for (Element element: file.elements) {
                //如果注解的成员变量是一个int类型
                if (element.asType().toString().equals("int")) {
                    //在构造方法中添加一条语句
                    //例如：((MainActivity)context).one = context.getResources().getInteger(R.integer.one);
                    builder.addStatement("(($N)context).$N = context.getResources().getInteger(R.integer.$N)",
                            file.className.split("_")[0], element.getSimpleName(), element.getSimpleName());
                //如果注解的是一个String类型
                } else if (element.asType().toString().equals("java.lang.String")) {
                    //在构造方法中添加一条语句
                    //例如：((MainActivity)context).hello = context.getResources().getString(R.string.hello);
                    builder.addStatement("(($N)context).$N = context.getResources().getString(R.string.$N)",
                            file.className.split("_")[0], element.getSimpleName(), element.getSimpleName());
                }
            }
            //构建一个类，添加一个上述的构造方法
            TypeSpec typeSpec = TypeSpec.classBuilder(file.className)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(builder.build())
                    .build();
            try {
                //输出java文件
                JavaFile javaFile = JavaFile.builder(file.packageName, typeSpec).build();
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加一个注解元素到对应的GenerateJavaFile对象中
     * @param element 注解元素
     */
    private void addElementToGenerateJavaFile(Element element) {
        //获取element对应成员变量所在的类，即被注解的类
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        String[] split = typeElement.getQualifiedName().toString().split("\\.");
        String className = split[split.length - 1];

        //通过父类的processingEnv获取报信者，用于在编译过程中打印log
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "add element to generate file " + className);

        //获取被注解类对应的GenerateJavaFile对象，如果没有，则创建
        GenerateJavaFile generateJavaFile = mGenerateJavaFiles.get(className);
        if (generateJavaFile == null) {
            GenerateJavaFile file = new GenerateJavaFile();
            //设置待生成java文件的包名
            file.packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
            //设置待生成java文件的类名
            file.className = className + "_Inject";
            //初始化元素集合
            file.elements = new ArrayList<Element>();
            file.elements.add(element);
            //保存被注解类所对应要生成java类的GenerateJavaFile对象
            mGenerateJavaFiles.put(className, file);
        } else {
            //将注解元素添加到有的generateJavaFile对象中
            generateJavaFile.elements.add(element);
        }
    }

    /**
     * 描述一个待生成的Java文件
     */
    private static class GenerateJavaFile {
        String packageName;//包名
        String className;//类名
        List<Element> elements;//注解元素集合
    }
}
