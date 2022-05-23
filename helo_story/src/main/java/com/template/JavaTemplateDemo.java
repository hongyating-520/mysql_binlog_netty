package com.template;

import javassist.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
 * @author ZZQ
 * @Date 2022/5/23 9:04 下午
 */
public class JavaTemplateDemo  {
    public static void main(String[] args) throws IOException, CannotCompileException, NotFoundException {
        javaassist();
        //thymeleaf();
        //velocity();

    }
    private static void javaassist() throws IOException, CannotCompileException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        //创建一个空类
        CtClass ctClass = pool.makeClass("test.javaassistdemo");
        //添加一个main方法
        CtMethod ctMethod = new CtMethod(CtClass.voidType, "main", new CtClass[]{pool.get(String[].class.getName())}, ctClass);
        //将main方法声明为public static类型
        ctMethod.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
        //设置方法体
        ctMethod.setBody("{" +
                "System.out.println(\"Javassist Hello World \");" +
                "}");
        ctClass.addMethod(ctMethod);
        //将生成的类的class文件输出的磁盘
        ctClass.writeFile("/Users/mima0000/Desktop/Tx2.java");

        //返回HelloWorld的Class实例
       //ctClass.toClass();
    }

    private static void thymeleaf() throws IOException {
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(JavaTemplateDemo.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "templates/");
        templateResolver.setTemplateMode("TEXT");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        Context context = new Context();
        context.setVariable("core","测试demo");
        String process = templateEngine.process("demo.java", context);
        System.out.println("process:"+process);
        File file = new File("/Users/mima0000/Desktop/Tx.java");
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(process);
        fileWriter.flush();
        fileWriter.close();
    }

    private static void velocity() throws IOException {
        String path = "templates/javademo.vm";
        Properties properties=new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        properties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        properties.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        VelocityEngine velocityEngine = new VelocityEngine(properties);
        velocityEngine.init();
        // 获取模板文件
        Template template = velocityEngine.getTemplate(path);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("core", "world");
        File file = new File("/Users/mima0000/Desktop/Tx1.java");
        file.createNewFile();
        //new StringWriter()
        FileWriter fileWriter = new FileWriter(file);
        template.merge(velocityContext,fileWriter );
        fileWriter.flush();fileWriter.close();
     }
}
