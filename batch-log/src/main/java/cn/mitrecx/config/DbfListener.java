package cn.mitrecx.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.yusys.yusp.commons.exception.YuspRuntimeException;
import cn.com.yusys.yusp.commons.util.DateUtil;
import cn.com.yusys.yusp.commons.util.StringUtil;
import cn.mitrecx.reader.DbfItemReader;
import cn.mitrecx.reader.dbf.DBFField;
import cn.mitrecx.reader.dbf.DBFReader;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月18日, 下午5:19:51
 * 
 */
@Component
public class DbfListener extends JobExecutionListenerSupport {
    @Autowired
    DataGatherDetailMapper dataGatherDetailMapper;
    private long jobStartTime;

  
    /**
     * @param jobExecution
     *
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        synchronized (DbfListener.class) {

            System.out.println("===job 开始执行===");
            jobStartTime = System.currentTimeMillis();
            // 插入 T_DATA_GATHER_DETAIL 记录执行过程
            String id = StringUtil.genericUUid(); // CL
            String processBatch = StringUtil.genericUUid(); // CL
            String fileName = jobExecution.getJobParameters().getString("fileName");
            String dataSource = "2";// 上传方式;1-自动;2-手动 TODO
            String bizType = jobExecution.getJobParameters().getString("bizType");
            Date bizDate = DateUtil.toDate(jobExecution.getJobParameters().getString("bizDate"), DateUtil.PATTERN_DATE);
            String mappingId = jobExecution.getJobParameters().getString("mappingId");
            String mappingName = jobExecution.getJobParameters().getString("mappingName");
            Date processDate = DateUtil.toDate(DateUtil.format(new Date()), DateUtil.PATTERN_DATE);
            Date startTime = new Date();
            // 处理结果;1-未处理;2--处理中,3-处理成功;4-处理失败
            String processResult = "1";
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("id", id);
            param.put("processBatch", processBatch);
            param.put("filename", fileName);
            param.put("dataSource", dataSource);
            param.put("bizType", bizType);
            param.put("bizDate", bizDate);
            param.put("mappingId", mappingId);
            param.put("mappingName", mappingName);
            param.put("processDate", processDate);
            param.put("startTime", startTime);
            param.put("processResult", processResult);
            // 插入 数据获取结果详细表T_DATA_GATHER_DETAIL
            dataGatherDetailMapper.insertDataGatherDetailMapper(param);
            // TODO 按条件--删除 目标表 数据
            String tableName = dataGatherDetailMapper.getTableName(mappingId);
            int delCnt = dataGatherDetailMapper.deleteTargetTable(tableName);
            System.out.println("删除 " + tableName + " 表中 " + delCnt + " 条数据");

            
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            // 更新 T_DATA_GATHER_DETAIL 状态
            long jobEndTime = System.currentTimeMillis();
            String mappingId = jobExecution.getJobParameters().getString("mappingId");
            String tableName = dataGatherDetailMapper.getTableName(mappingId);
            int cnt = dataGatherDetailMapper.getCount(tableName);
            System.out.println("0000000插入表 " + tableName + " " + cnt + " 条数据");
            //System.out.println("解析+插入 总耗时: " + (jobEndTime - jobStartTime) / 1000.0 + " 秒");

        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            // 更新 T_DATA_GATHER_DETAIL 状态
            System.out.println("job 执行失败.");
        }
    }
}
