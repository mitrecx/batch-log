package cn.mitrecx.domain.entity;

/**
 * 文件列与表字段 映射配置表 TEST_FILE_MAPPING_DETAILS
 * 
 * @author cx
 * @time 2019年7月18日, 下午4:20:16
 * 
 */
public class FileMappingDetailsEntity {
    // 配置id
    private String mappingId;
    // 表字段名
    private String tableColumnName;
    // 文件字段名/列编号
    private String fileColumnName;
    // 文件列 与 表字段 映射表达式
    private String fileColumnExp;
    // 字段删除标志
    private String deleteMark;
    
    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public String getTableColumnName() {
        return tableColumnName;
    }

    public void setTableColumnName(String tableColumnName) {
        this.tableColumnName = tableColumnName;
    }

    public String getFileColumnName() {
        return fileColumnName;
    }

    public void setFileColumnName(String fileColumnName) {
        this.fileColumnName = fileColumnName;
    }

    public String getFileColumnExp() {
        return fileColumnExp;
    }

    public void setFileColumnExp(String fileColumnExp) {
        this.fileColumnExp = fileColumnExp;
    }

    public String getDeleteMark() {
        return deleteMark;
    }

    public void setDeleteMark(String deleteMark) {
        this.deleteMark = deleteMark;
    }
    

}
