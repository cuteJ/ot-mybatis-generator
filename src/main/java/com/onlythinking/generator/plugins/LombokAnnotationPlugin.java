package com.onlythinking.generator.plugins;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class LombokAnnotationPlugin extends PluginAdapter {

    private String builder;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addAnnotation("@Data");
        builder = properties.getProperty("builder");
        if ("true".equals(builder)) {
            topLevelClass.addAnnotation("@Builder");
            topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.Builder"));
        }
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.Data"));
        List<Method> methods = topLevelClass.getMethods();
        List<Method> remove = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getBodyLines().size() < 2) {
                remove.add(method);
                System.out.println("-----------------" + topLevelClass.getType().getShortName() + "'s method=" + method.getName() + " removed cause lombok annotation.");
            }
        }
        methods.removeAll(remove);
        return true;
    }

}
