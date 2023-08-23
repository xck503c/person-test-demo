package com.xck.jdk.file.calcite;

import java.io.File;
import java.util.Map;

public interface TableMetaReader {

    Map<String, String> getTableFieldTypeMap(File file);
}
