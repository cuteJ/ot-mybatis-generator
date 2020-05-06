package com.onlythinking.generator.plugins;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * <p> Generator extend baseMapper mapper. </p>
 *
 * @author Li Xingping
 */
public class BaseMapperPlugin extends PluginAdapter {

    private String baseMapperType;
    private boolean noDisabled;

    private final static FullyQualifiedJavaType MAPPER_ANNO = new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper");

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        baseMapperType = properties.getProperty("baseMapperType");
        if (!stringHasValue(baseMapperType)) {
            System.err.println("[ERROR] BaseMapperPlugin baseMapperType property must be provided");
            return false;
        }

        if (stringHasValue(properties.getProperty("noDisabled"))) {
            noDisabled = Boolean.parseBoolean(properties.getProperty("noDisabled"));
        }

        FullyQualifiedJavaType baseRecordJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        interfaze.addImportedType(baseRecordJavaType);

        FullyQualifiedJavaType baseMapperJavaType = new FullyQualifiedJavaType(baseMapperType + "<" + baseRecordJavaType.getShortName() + ">");

        interfaze.addSuperInterface(baseMapperJavaType);
        interfaze.addImportedType(baseMapperJavaType);

        interfaze.addAnnotation("@Mapper");
        interfaze.addImportedType(MAPPER_ANNO);

        String targetPackage = getContext().getJavaClientGeneratorConfiguration().getTargetProject();
        String path = targetPackage + "/" + interfaze.getType().getFullyQualifiedName().replaceAll("\\.", "/") + ".java";
        File file = new File(path);
        PluginUtils.addDocToClassOrInterface(null, null, null, interfaze);
        return !file.exists();
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        return true;
    }

    /* sql mapper.*/
    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    private List<IntrospectedColumn> getAllColumns(IntrospectedTable introspectedTable) {

        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        List<IntrospectedColumn> allColumns = new ArrayList<>();
        for (IntrospectedColumn col : columns) {
            if (!"_MYCAT_OP_TIME".equals(col.getActualColumnName().toUpperCase())) {
                allColumns.add(col);
            }
        }
        return allColumns;
    }

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = getAllColumns(introspectedTable);
        element.getElements().clear();
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn col = columns.get(i);
            String content = col.getActualColumnName() + "\t" + col.getJavaProperty();
            if (i != columns.size() - 1) {
                content += ",";
            }
            element.addElement(
              new TextElement(content)
            );
        }
        return true;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        replaceAttr(element, "id", "id", "getByPK");
        replaceAttr(element, "resultMap", "resultType", introspectedTable.getBaseRecordType());
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String trimCols = getTestCol(getAllColumns(introspectedTable));
        IntrospectedColumn id = getPk(introspectedTable);
        String getMethodXml = MessageFormat.format(QUERY_TEMPLATE_GETONE, "getOne", introspectedTable.getBaseRecordType(), introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), trimCols);
        String getListMethodXml = MessageFormat.format(QUERY_TEMPLATE_GETLIST, "getList", introspectedTable.getBaseRecordType(), introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), trimCols);
        String delBatchMethodXml = MessageFormat.format(DELETEINBATCH_TEMPLATE, "deleteInBatch", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), id.getActualColumnName(), id.getJavaProperty(), "#{" + id.getJavaProperty() + "}");
        String insertBatchMethodXml = MessageFormat.format(INSERTINBATCH_TEMPLATE, "insertInBatch", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), getTestCol_insertInBatch1(getAllColumns(introspectedTable)), getTestCol_insertInBatch2(getAllColumns(introspectedTable)));
        String countMethodXml = MessageFormat.format(COUNT_TEMPLATE_GETONE, "count", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), trimCols);

        document.getRootElement().getElements().add(new TextElement(countMethodXml));
        document.getRootElement().getElements().add(new TextElement(getMethodXml));
        document.getRootElement().getElements().add(new TextElement(getListMethodXml));
        document.getRootElement().getElements().add(new TextElement(delBatchMethodXml));
        document.getRootElement().getElements().add(new TextElement(insertBatchMethodXml));

        if (!this.noDisabled) {
            String disabledByPkMethodXml = MessageFormat.format(DISABLEDBYPK_TEMPLATE, "disabledByPk", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), "disabled = true", id.getJavaProperty(), "#{" + id.getJavaProperty() + "}");
            String disabledMethodXml = MessageFormat.format(DISABLED_TEMPLATE, "disabledBySelective", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime(), "disabled = true", trimCols);
            document.getRootElement().getElements().add(new TextElement(disabledByPkMethodXml));
            document.getRootElement().getElements().add(new TextElement(disabledMethodXml));
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        replaceAttr(element, "id", "id", "updateByPK");
        updateByPK(element, introspectedTable);
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        replaceAttr(element, "id", "id", "updateBySelective");
        replaceAttr(element, "parameterType", "parameterType", "java.util.Map");
        updateBySelective(element, introspectedTable);
        return true;
    }

    private void updateByPK(XmlElement element, IntrospectedTable introspectedTable) {
        element.getElements().clear();
        IntrospectedColumn id = getPk(introspectedTable);
        String updateSql = MessageFormat.format(UPDATEBYPK_TEMPLATE, introspectedTable.getFullyQualifiedTableNameAtRuntime()
          , getTestCol_updateByPK(getAllColumns(introspectedTable))
          , id.getActualColumnName()
          , "#{" + id.getJavaProperty() + ",jdbcType=" + id.getJdbcTypeName() + "}");
        element.getElements().add(new TextElement(updateSql));
    }

    private void updateBySelective(XmlElement element, IntrospectedTable introspectedTable) {
        element.getElements().clear();
        IntrospectedColumn id = getPk(introspectedTable);
        String updateSql = MessageFormat.format(UPDATEBYSELECTIVE_TEMPLATE, introspectedTable.getFullyQualifiedTableNameAtRuntime()
          , getTestCol_updateBySelective(getAllColumns(introspectedTable))
          , id.getActualColumnName()
          , "#{" + id.getJavaProperty() + ",jdbcType=" + id.getJdbcTypeName() + "}");
        element.getElements().add(new TextElement(updateSql));
    }

    private IntrospectedColumn getPk(IntrospectedTable introspectedTable) {
        IntrospectedColumn id = introspectedTable.getColumn("id");
        if (null == id) {
            List<IntrospectedColumn> pks = introspectedTable.getPrimaryKeyColumns();
            if (null != pks && !pks.isEmpty()) {
                id = pks.get(0);
            } else {
                System.err.println(String.format("%s 不存在主键", introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            }
        }
        return id;
    }

    private String getTestCol(List<IntrospectedColumn> columns) {
        StringBuilder trimCols = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            trimCols.append("\t\t\t<if test=\"").append(col.getJavaProperty()).append(" != null");
            if (col.getJdbcTypeName().equals("VARCHAR")) {
                trimCols.append(" and ").append(col.getJavaProperty()).append(" != ''");
            }
            trimCols.append("\">\r\n");
            trimCols.append("\t\t\t\tand ").append(col.getActualColumnName()).append(" = #{").append(col.getJavaProperty()).append(",").append("jdbcType=").append(col.getJdbcTypeName()).append("}\r\n");
            trimCols.append("\t\t\t</if>\r\n");
        }
        return trimCols.toString();
    }

    private String getTestCol_updateByPK(List<IntrospectedColumn> columns) {
        StringBuilder trimCols = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            if ("id".equals(col.getJavaProperty())) continue;
            trimCols.append("\t\t\t<if test=\"").append(col.getJavaProperty()).append(" != null");
            if (col.getJdbcTypeName().equals("VARCHAR")) {
                trimCols.append(" and ").append(col.getJavaProperty()).append(" != ''");
            }
            trimCols.append("\">\r\n");
            trimCols.append("\t\t\t\t").append(col.getActualColumnName()).append(" = #{").append(col.getJavaProperty()).append(",").append("jdbcType=").append(col.getJdbcTypeName()).append("},\r\n");
            trimCols.append("\t\t\t</if>\r\n");
        }
        trimCols.deleteCharAt(trimCols.lastIndexOf(","));
        return trimCols.toString();
    }

    private String getTestCol_updateBySelective(List<IntrospectedColumn> columns) {
        StringBuilder trimCols = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            if ("id".equals(col.getJavaProperty())) continue;
            trimCols.append("\t\t\t<if test=\"").append(col.getJavaProperty()).append(" != null");
            trimCols.append("\">\r\n");
            trimCols.append("\t\t\t\t").append(col.getActualColumnName()).append(" = #{").append(col.getJavaProperty()).append(",").append("jdbcType=").append(col.getJdbcTypeName()).append("},\r\n");
            trimCols.append("\t\t\t</if>\r\n");
        }
        trimCols.deleteCharAt(trimCols.lastIndexOf(","));
        return trimCols.toString();
    }

    private String getTestCol_insertInBatch1(List<IntrospectedColumn> columns) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            sb.append("\t\t\t");
            sb.append(col.getActualColumnName());
            sb.append(",\n");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    private String getTestCol_insertInBatch2(List<IntrospectedColumn> columns) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            sb.append("\t\t\t#{item.");
            sb.append(col.getJavaProperty());
            sb.append("},\n");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    private void replaceAttr(XmlElement element, String originalName, String name, String value) {
        element.getAttributes().stream().filter(attr -> originalName.equals(attr.getName())).findFirst().ifPresent(id -> element.getAttributes().remove(id));
        element.getAttributes().add(new Attribute(name, value));
    }

    private final static String QUERY_TEMPLATE_GETONE =
      "<select id=\"{0}\" parameterType=\"java.util.Map\" resultType=\"{1}\">\n" +
        "\t\tselect\n" +
        "\t\t\t<include refid=\"Base_Column_List\"/>\n" +
        "\t\tfrom {2}\n" +
        "\t\t<trim prefix=\"where\" prefixOverrides=\"and |or \">\n" +
        "{3}" +
        "\t\t</trim>\n" +
        "\t</select>";
    private final static String QUERY_TEMPLATE_GETLIST =
      "<select id=\"{0}\" parameterType=\"java.util.Map\" resultType=\"{1}\">\n" +
        "\t\tselect\n" +
        "\t\t\t<include refid=\"Base_Column_List\"/>\n" +
        "\t\tfrom {2}\n" +
        "\t\t<trim prefix=\"where\" prefixOverrides=\"and |or \">\n" +
        "{3}" +
        "\t\t</trim>\n" +
        "\t</select>";

    private final static String UPDATEBYPK_TEMPLATE =
      "\tupdate {0}\n" +
        "\t\t\t<set>\n" +
        "{1}" +
        "\t\t\t</set>\n" +
        "\t\t\twhere {2} = {3}";

    private final static String UPDATEBYSELECTIVE_TEMPLATE =
      "\tupdate {0}\n" +
        "\t\t\t<set>\n" +
        "{1}" +
        "\t\t\t</set>\n" +
        "\t\t\twhere {2} = {3}";

    private final static String DELETEINBATCH_TEMPLATE =
      "<delete id=\"{0}\" parameterType=\"java.util.List\">\n" +
        "\t\tdelete from {1} where {2} in\n" +
        "\t\t<foreach collection=\"array\" item=\"{3}\" open=\"(\" separator=\",\" close=\")\">{4}</foreach>\n" +
        "\t</delete>";

    private final static String INSERTINBATCH_TEMPLATE =
      "<insert id=\"{0}\" parameterType=\"java.util.List\">\n" +
        "\t\tinsert into {1}\n" +
        "\t\t(\n" +
        "{2}" +
        "\t\t)\n" +
        "\t\tvalues\n" +
        "\t\t<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n" +
        "\t\t(\n" +
        "{3}" +
        "\t\t)\n" +
        "\t\t</foreach>\n" +
        "\t</insert>";

    private final static String COUNT_TEMPLATE_GETONE =
      "<select id=\"{0}\" parameterType=\"java.util.Map\" resultType=\"java.lang.Long\">\n" +
        "\t\tselect\n" +
        "\t\t\tcount(0)\n" +
        "\t\tfrom {1}\n" +
        "\t\t<trim prefix=\"where\" prefixOverrides=\"and |or \">\n" +
        "{2}" +
        "\t\t</trim>\n" +
        "\t</select>";

    private final static String DISABLEDBYPK_TEMPLATE =
      "<update id=\"{0}\" parameterType=\"java.lang.String\" >\n" +
        "\t\tupdate {1}\n" +
        "\t\t<set>\n" +
        "\t\t\t{2}\n" +
        "\t\t</set>\n" +
        "\t\twhere {3} = {4}\n" +
        "</update>";

    private final static String DISABLED_TEMPLATE =
      "<update id=\"{0}\" parameterType=\"java.util.Map\" >\n" +
        "\t\tupdate {1}\n" +
        "\t\t<set>\n" +
        "\t\t\t{2}\n" +
        "\t\t</set>\n" +
        "\t\t<trim prefix=\"where\" prefixOverrides=\"and |or \">\n" +
        "{3}" +
        "\t\t</trim>\n" +
        "\t\t</update>";

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = getAllColumns(introspectedTable);
        element.getElements().clear();
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ")
          .append(introspectedTable.getFullyQualifiedTableNameAtRuntime())
          .append("\n\t\t(\n");
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn col = columns.get(i);
            sb.append("\t\t\t").append(col.getActualColumnName());
            if (i != columns.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("\n\t\t)\n\t\tvalues\n\t\t(\n");
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn col = columns.get(i);
            sb.append("\t\t\t#{").append(col.getJavaProperty()).append(",jdbcType=").append(col.getJdbcTypeName()).append("}");
            if (i != columns.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n\t\t)");
            }
        }
        element.addElement(new TextElement(sb.toString()));
        return true;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        replaceAttr(element, "id", "id", "deleteByPK");
        return true;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    /* java mapper.*/
    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if ("MYCATOPTIME".equals(field.getName().toUpperCase())) return false;
        return true;
    }
}
