package com.onlythinking.generator.plugins;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * <p> The describe </p>
 *
 * @author Li Xingping
 */
public class RemarksCommentPlugin extends PluginAdapter {

    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType) {
        String remark = introspectedColumn.getRemarks();
        field.addJavaDocLine("/**");
        field.addJavaDocLine(" * " + remark);
        field.addJavaDocLine(" */");
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
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
        PluginUtils.addDocToClassOrInterface(null, remark, table.getIntrospectedTableName(), topLevelClass);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
                                                      IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
