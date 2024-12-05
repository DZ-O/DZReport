package com.dz.eToSQL.emums;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 14:58
 */
public enum FileTypeEnum {
    /**
     * 文件类型
     */
    EXCEL_XLS("xls"),
    EXCEL_XLSX("xlsx"),
    CSV("csv"),
    TXT("txt"),
    DOC("doc"),
    DOCX("docx"),
    PDF("pdf"),
    JSON("json"),
    SQL("sql"),
    ZIP("zip"),
    RAR("rar"),
    ;

    private String type;

    FileTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static boolean isValidFileType(String fileExtension) {
        for (FileTypeEnum fileType : FileTypeEnum.values()) {
            if (fileType.getType().equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }
}
