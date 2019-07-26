package cn.mitrecx.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 文件与表 映射配置 TEST_FILE_MAPPING
 * 
 * @author cx
 * @time 2019年7月18日, 下午4:10:39
 * 
 */
public class FileMappingEntity {
    // 配置id
    private String mappingId;
    // 配置名称
    private String mappingName;
    // 文件类型;1-DBF,2-TXT/CSV/TSV,3-XML;4-XLS/XLSX
    private String fileType;
    // 编码格式
    private String encodingType;
    // 业务类型: 1-第三方数据,2-交易所券商清算,3-交易所集中清算,4-中登结算数据,5-行情数据,6-TA数据
    private String bizType;
    // 映射表名
    private String mappingTableName;
    // 去除起始行行数
    private BigDecimal excludeHead;
    // 去除结束行行数
    private BigDecimal excludeTail;
    // 解析类型: 1-固定列,2-分隔符,3-按节点,4-按属性,5-按名称,6-按索引
    private String parseType;
    // 解析规则
    private String parseRule;
    // 创建人id
    private String createUserId;
    // 创建时间
    private Date createTime;
    // 修改人id
    private String updateUserId;
    // 修改时间
    private Date updateTime;
    // 数据头标识
    private String header;
    // 数据尾标识
    private String trailer;

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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getMappingTableName() {
        return mappingTableName;
    }

    public void setMappingTableName(String mappingTableName) {
        this.mappingTableName = mappingTableName;
    }

    public BigDecimal getExcludeHead() {
        return excludeHead;
    }

    public void setExcludeHead(BigDecimal excludeHead) {
        this.excludeHead = excludeHead;
    }

    public BigDecimal getExcludeTail() {
        return excludeTail;
    }

    public void setExcludeTail(BigDecimal excludeTail) {
        this.excludeTail = excludeTail;
    }

    public String getParseType() {
        return parseType;
    }

    public void setParseType(String parseType) {
        this.parseType = parseType;
    }

    public String getParseRule() {
        return parseRule;
    }

    public void setParseRule(String parseRule) {
        this.parseRule = parseRule;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

}
