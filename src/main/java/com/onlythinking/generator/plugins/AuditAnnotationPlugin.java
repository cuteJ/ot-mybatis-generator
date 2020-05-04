package com.onlythinking.generator.plugins;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuditAnnotationPlugin extends PluginAdapter {

    private String packageBasePath;

    private String createdTime;
    private String lastModifiedTime;
    private String createdUser;
    private String lastModifiedUser;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        packageBasePath = properties.getProperty("packageBasePath");
        if (StringUtils.isBlank(packageBasePath)) {
            System.err.println("packageBasePath is blank.");
            return true;
        }
        createdTime = properties.getProperty("createdTime");
        lastModifiedTime = properties.getProperty("lastModifiedTime");
        createdUser = properties.getProperty("createdUser");
        lastModifiedUser = properties.getProperty("lastModifiedUser");

        if (StringUtils.isNotBlank(createdTime) && atLeastOne(introspectedTable, createdTime)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(packageBasePath + "CreatedTime"));
        }
        if (StringUtils.isNotBlank(lastModifiedTime) && atLeastOne(introspectedTable, lastModifiedTime)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(packageBasePath + "LastModifiedTime"));
        }
        if (StringUtils.isNotBlank(createdUser) && atLeastOne(introspectedTable, createdUser)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(packageBasePath + "CreatedUser"));
        }
        if (StringUtils.isNotBlank(lastModifiedUser) && atLeastOne(introspectedTable, lastModifiedUser)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(packageBasePath + "LastModifiedUser"));
        }
        return true;
    }

    private boolean atLeastOne(IntrospectedTable introspectedTable, String commentName) {
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(commentName)) {
                return true;
            }
        }
        return false;
    }


    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType) {

        packageBasePath = properties.getProperty("packageBasePath");
        if (StringUtils.isBlank(packageBasePath)) {
            System.err.println("packageBasePath is blank.");
            return true;
        }
        createdTime = properties.getProperty("createdTime");
        lastModifiedTime = properties.getProperty("lastModifiedTime");
        createdUser = properties.getProperty("createdUser");
        lastModifiedUser = properties.getProperty("lastModifiedUser");
        if (StringUtils.isNotBlank(createdTime)) {
            if (introspectedColumn.getJavaProperty().equals(createdTime)) {
                field.addAnnotation("@CreatedTime");
            }
        }
        if (StringUtils.isNotBlank(lastModifiedTime)) {
            if (introspectedColumn.getJavaProperty().equals(lastModifiedTime)) {
                field.addAnnotation("@LastModifiedTime");
            }
        }
        if (StringUtils.isNotBlank(createdUser)) {
            if (introspectedColumn.getJavaProperty().equals(createdUser)) {
                field.addAnnotation("@CreatedUser");
            }
        }
        if (StringUtils.isNotBlank(lastModifiedUser)) {
            if (introspectedColumn.getJavaProperty().equals(lastModifiedUser)) {
                field.addAnnotation("@LastModifiedUser");
            }
        }
        return true;
    }
}
