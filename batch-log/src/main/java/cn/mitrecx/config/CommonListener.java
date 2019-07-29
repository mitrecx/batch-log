package cn.mitrecx.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.yusys.yusp.commons.util.DateUtil;
import cn.com.yusys.yusp.commons.util.StringUtil;
import cn.mitrecx.domain.entity.FileMappingDetailsEntity;
import cn.mitrecx.processor.CommonItemProcessor;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月18日, 下午5:19:51
 * 
 */
@Component
public class CommonListener extends JobExecutionListenerSupport {
    @Autowired
    DataGatherDetailMapper dataGatherDetailMapper;
    private long jobStartTime;

    /**
     * @param jobExecution
     *
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("===job 开始执行===");
        jobStartTime = System.currentTimeMillis();
        // 插入 T_DATA_GATHER_DETAIL 记录执行过程
        String id = StringUtil.genericUUid(); // CL
        String processBatch = StringUtil.genericUUid(); // CL
        String filename = jobExecution.getJobParameters().getString("fileName");
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
        param.put("filename", filename);
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
        String path = jobExecution.getJobParameters().getString("path") + "\\";
        String encoding = dataGatherDetailMapper.getEncoding(mappingId);
//        try {
//            InputStream is = new FileInputStream(new File(path + filename));
//
//            InputStreamReader isr = new InputStreamReader(is, encoding);
//            BufferedReader reader = new BufferedReader(isr);
//            deleteFromTargetTable(mappingId, reader);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }

        
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

    public static void main(String[] args) {
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("name", "mitre");
        map1.put("age", "12");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("name", "mitre");
        map2.put("age", "12");
        if (map1.equals(map2)) {
            System.out.println("相同");
        } else {
            System.out.println("不相同!");
        }
    }

    void deleteFromTargetTable(String mappingId, BufferedReader reader) {
        // 表名
        String tableName = dataGatherDetailMapper.getTableName(mappingId);
        int deleteColumnCnt = dataGatherDetailMapper.getDeleteColumnCount(mappingId);
        List<FileMappingDetailsEntity> fileMappingDetails = dataGatherDetailMapper.getFileMappingDetails(mappingId);
        
        if (deleteColumnCnt == 1) {// 根据 1个 字段 删除目标表中的数据
            String fileColumn = null;
            String tableColumn = null;
            for (FileMappingDetailsEntity detail : fileMappingDetails) {
                if ("Y".equals(detail.getDeleteMark())) {
                    fileColumn = detail.getFileColumnName();
                    tableColumn = detail.getTableColumnName();
                    break;
                }
            }
            // 删除字段标识不为空 (比如为, column0 等等)
            if (fileColumn != null) {
                Map<String, String> readResult = null;
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null && !line.contains("TRAILER") && !line.contains("HEADER")) {
                        String separator = dataGatherDetailMapper.getParseRule(mappingId);
                        String[] array = line.split(separator);
                        int length = array.length;
                        readResult = new HashMap<String, String>();
                        for (int i = 0; i < length; i++) {
                            readResult.put("column" + i, array[i].trim());
                        }
                        // System.out.println(lineCount);
                        list.add(readResult);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Set<String> set = new HashSet<String>();
                if (list.size() > 0) {
                    for (Map<String, String> m : list) {
                        // deleteColumn 字段
                        String deleteValue = m.get(fileColumn);
                        set.add(deleteValue);
                    }
                }
                Map<String, Set<String>> tableDeleteMap = new HashMap<String, Set<String>>();
                tableDeleteMap.put(tableColumn, set);
                int delCnt = dataGatherDetailMapper.deleteTargetTableByConfig(tableName, tableDeleteMap);
                System.out.println("删除 " + tableName + " 表中 " + delCnt + " 条数据.");
            }
        } else if (deleteColumnCnt > 1) {// 根据 多个 字段 删除目标表中的数据
            Map<String, String> readResult = null;
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            try {
                String line = null;
                while ((line = reader.readLine()) != null && !line.contains("TRAILER") && !line.contains("HEADER")) {
                    String separator = dataGatherDetailMapper.getParseRule(mappingId);
                    String[] array = line.split(separator);
                    int length = array.length;
                    readResult = new HashMap<String, String>();
                    for (int i = 0; i < length; i++) {
                        readResult.put("column" + i, array[i].trim());
                    }
                    Map<String, String> deleteMap=new HashMap<String, String>();
                    for (FileMappingDetailsEntity fileMappingDetailsEntity : fileMappingDetails) {
                        if (fileMappingDetailsEntity.getFileColumnName() != null && fileMappingDetailsEntity.getFileColumnExp() == null && "Y".equals(fileMappingDetailsEntity.getDeleteMark())) {
                            deleteMap.put(fileMappingDetailsEntity.getTableColumnName(), readResult.get(fileMappingDetailsEntity.getFileColumnName()));
                        }
                    }
                    // System.out.println(lineCount);
                    list.add(deleteMap);
                }
                int delCnt = dataGatherDetailMapper.deleteTargetTableByConfigMulti(tableName, list);
                System.out.println("删除 " + tableName + " 表中 " + delCnt + " 条数据.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
