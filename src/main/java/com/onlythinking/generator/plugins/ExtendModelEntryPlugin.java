package com.onlythinking.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.io.File;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

// todo 二期开发
public class ExtendModelEntryPlugin extends PluginAdapter {
    private String baseModelType;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        baseModelType = properties.getProperty("baseModelType");
        if (!stringHasValue(baseModelType)) {
            System.err.println("[ERROR] ExtendModelEntryPlugin baseModelType property must be provided");
            return false;
        }
        FullyQualifiedJavaType baseRecordJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        interfaze.addImportedType(baseRecordJavaType);

        FullyQualifiedJavaType baseModelJavaType = new FullyQualifiedJavaType(baseModelType);

        interfaze.addSuperInterface(baseModelJavaType);
        interfaze.addImportedType(baseModelJavaType);

        String targetPackage = getContext().getJavaClientGeneratorConfiguration().getTargetProject();
        String path = targetPackage + File.separator + interfaze.getType().getFullyQualifiedName().replaceAll("\\.", File.separator) + ".java";
        File file = new File(path);
        return !file.exists();
    }

}
