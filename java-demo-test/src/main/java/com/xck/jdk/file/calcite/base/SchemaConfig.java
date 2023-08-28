package com.xck.jdk.file.calcite.base;

import java.util.Properties;

public class SchemaConfig {

    private Properties properties;

    private SchemaConfig(Properties properties) {
        this.properties = properties;
    }

    public static SchemaConfig defaultConfig() {
        Properties properties = new Properties();
        return new SchemaConfig(properties);
    }

    public SchemaConfig modelConfig(String filePath) {
        properties.put("model", filePath);
        return this;
    }

    public SchemaConfig lex(String lex) {
        properties.put("lex", lex);
        return this;
    }

    public SchemaConfig caseSensitive(boolean iscaseSensitive) {
        properties.put("caseSensitive", iscaseSensitive);
        return this;
    }

    public Properties build() {
        return properties;
    }
}
