package cn.mitrecx.domain.vo;

/**
 * 文件上传 前端传入数据
 * 
 * @author cx
 * @time 2019年7月18日, 下午3:31:25
 * 
 */
public class FileUploadVo {
    // 文件名
    private String fileName;
    // 映射配置id
    private String mappingId;
    // 映射配置名称
    private String mappingName;
    // 业务类型;1-第三方数据,2-交易所券商清算,3-交易所集中清算,4-中登结算数据,5-行情数据,6-TA数据
    private String bizType;
    private String bizDate;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizDate() {
        return bizDate;
    }

    public void setBizDate(String bizDate) {
        this.bizDate = bizDate;
    }

}
