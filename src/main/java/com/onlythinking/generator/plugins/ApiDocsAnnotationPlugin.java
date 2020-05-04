package com.onlythinking.generator.plugins;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiDocsAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String remark = "";
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        try {
            Connection connection = ConnectionFactory.getInstance().getConnection(
              context.getJdbcConnectionConfiguration());
            ResultSet rs = connection.getMetaData().getTables(table.getIntrospectedCatalog(),
              table.getIntrospectedSchema(), table.getIntrospectedTableName(), null);
            if (null != rs && rs.next()) {
                //must set useInformationSchema = true
                remark = rs.getString("REMARKS");
            }
            if (null != rs) {
                rs.close();
            }
            connection.close();
        } catch (SQLException ignored) {
        }
        if (StringUtils.isBlank(remark)) {
            topLevelClass.addAnnotation("@ApiModel");
        } else {
            topLevelClass.addAnnotation(String.format("@ApiModel(\"%s\")", remark));
        }
        topLevelClass.addImportedType(new FullyQualifiedJavaType("io.swagger.annotations.ApiModel"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Id"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.hibernate.validator.constraints.NotBlank"));
        return true;
    }

    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType) {
        String remark = introspectedColumn.getRemarks();
        boolean isId = false;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            if (introspectedColumn == column) {
                isId = true;
                field.addAnnotation("@Id");
                break;
            }
        }

        if (!introspectedColumn.isNullable() && !isId) {
            if (introspectedColumn.isStringColumn()) {
                field.addAnnotation("@NotBlank");
            }
            if(remark.length() > 8){}
            field.addAnnotation(String.format("@ApiModelProperty(value = \"%s\", required = true)", remark));
        } else {
            field.addAnnotation("@ApiModelProperty(value = \"" + remark + "\")");
        }
        return true;
    }

}
