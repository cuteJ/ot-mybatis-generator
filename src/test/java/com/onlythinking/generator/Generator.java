package com.onlythinking.generator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class Generator {

    @Test
    public void generatorTest() {

        String root = System.getProperty("user.dir");
        String projectPath = root + "/src/test/java";
        String xmlProjectPath = root + "/src/test/java";

        String modelPackage = "com.test.model";
        String mapperPackage = "com.test.mapper";
        String xmlPackage = "/com/test/mapper";

        String configXmlLocation = root + "/src/main/resources/generatorConfig.xml";

        try {
            List<String> warnings = new ArrayList<String>();
            File configFile = new File(configXmlLocation);

            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configFile);

            config.getContexts().get(0).getJavaModelGeneratorConfiguration().setTargetProject(projectPath);
            config.getContexts().get(0).getJavaModelGeneratorConfiguration().setTargetPackage(modelPackage);

            config.getContexts().get(0).getJavaClientGeneratorConfiguration().setTargetProject(projectPath);
            config.getContexts().get(0).getJavaClientGeneratorConfiguration().setTargetPackage(mapperPackage);

            config.getContexts().get(0).getSqlMapGeneratorConfiguration().setTargetProject(xmlProjectPath);
            config.getContexts().get(0).getSqlMapGeneratorConfiguration().setTargetPackage(xmlPackage);

            DefaultShellCallback callback = new DefaultShellCallback(true);
            ProgressCallback progressCallback = new VerboseProgressCallback();
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(progressCallback);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
