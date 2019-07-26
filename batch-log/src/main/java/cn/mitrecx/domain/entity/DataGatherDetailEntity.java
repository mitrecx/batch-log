package cn.mitrecx.domain.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.com.yusys.yusp.commons.util.DateUtil;

/**
 * 数据获取结果详细表 T_DATA_GATHER_DETAIL
 * 
 * @author cx
 * @time 2019年7月18日, 下午3:40:40
 * 
 */
public class DataGatherDetailEntity {
    // 数据库主键-无实际意义
    private String id;

    // 处理批次-批次文件夹名称
    private String processBatch;

    private String filename;
    // 文件上传方式: 1-自动, 2-手动
    private String dataSource;
    // 业务类型;1-第三方数据,2-交易所券商清算,3-交易所集中清算,4-中登结算数据,5-行情数据,6-TA数据
    private String bizType;
    // 业务日期(文件上传不用判断业务日期)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bizDate;

    // 映射配置id
    private String mappingId;
    // 映射配置名称
    private String mappingName;
    // 处理日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date processDate;
    // 处理开始时间
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    // 处理结束时间
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    // 处理结果;1-未处理;2--处理中,3-处理成功;4-处理失败
    private String processResult;
    // 结果备注
    private String resultRemarks;
    // 外部系统ID;用于查询外部系统信息
    private String externalId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getProcessBatch() {
        return processBatch;
    }

    public void setProcessBatch(String processBatch) {
        this.processBatch = processBatch == null ? null : processBatch.trim();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource == null ? null : dataSource.trim();
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType == null ? null : bizType.trim();
    }

    public Date getBizDate() {
        return bizDate;
    }

    public void setBizDate(Date bizDate) {
        this.bizDate = bizDate;
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId == null ? null : mappingId.trim();
    }

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName == null ? null : mappingName.trim();
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getProcessResult() {
        return processResult;
    }

    public void setProcessResult(String processResult) {
        this.processResult = processResult == null ? null : processResult.trim();
    }

    public String getResultRemarks() {
        return resultRemarks;
    }

    public void setResultRemarks(String resultRemarks) {
        this.resultRemarks = resultRemarks == null ? null : resultRemarks.trim();
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId == null ? null : externalId.trim();
    }

    public String toLogString() {
        return "uniqueindex:" + getExternalId() + ", filename:" + getFilename() + ", mappingId:" + getMappingId() + ", bizDate:" + DateUtil.format(getBizDate(), DateUtil.PATTERN_DATE) + ", processBatch:" + getProcessBatch();
    }
}
