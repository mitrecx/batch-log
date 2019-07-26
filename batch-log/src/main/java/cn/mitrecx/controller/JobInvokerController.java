package cn.mitrecx.controller;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.mitrecx.domain.ParseIMIXLogVo;
import cn.mitrecx.domain.vo.FileUploadVo;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月11日, 上午10:10:30
 * 
 */
//@EnableScheduling
@Configuration
@RestController
public class JobInvokerController {
//
//    @Autowired
//    JobLauncher jobLauncher;

    @Autowired
    JobRepository jobRepository;
    
    @Autowired
    DataGatherDetailMapper dataGatherDetailMapper;

    @Bean
    public JobLauncher myJobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        // the launching needs to be done asynchronously so that the SimpleJobLauncher returns immediately
        // to its caller.
        // 解决办法: configuring a TaskExecutor
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        try {
            jobLauncher.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobLauncher;
    }

//    @Autowired
//    Job importUserJob;
    @Autowired
    Job commonJob;
    @Autowired
    Job dbfCommonJob;
    @Autowired
    Job XMLCommonJob;

    // 每20秒执行一次
//    @Scheduled(cron = "*/20 * *  * * * ")
//    @RequestMapping("/start")
//    public String handle(@RequestBody ParseIMIXLogVo parseIMIXLogVo) throws Exception {
//
//        System.out.println(parseIMIXLogVo.getFilePath());
//        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder().addLong("time", System.currentTimeMillis());
//        jobParametersBuilder.addString("filePath", parseIMIXLogVo.getFilePath());
//        jobParametersBuilder.addString("date", parseIMIXLogVo.getDate());
//        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
//        JobExecution jobExecution = myJobLauncher().run(importUserJob, jobParameters);
//        // Thread.sleep(8000);
//        // STARTING, STARTED, STOPPING, STOPPED, FAILED, COMPLETED, ABANDONED
//        return "BatchStatus: " + jobExecution.getStatus().toString() + " ExitStatus:" + jobExecution.getExitStatus();
//    }

    @RequestMapping("/common")
    public String common(@RequestBody List<FileUploadVo> fileUploadVos) throws Exception {
        String path="C:\\Users\\cx141\\Desktop\\20180330数据文件\\港股通\\cx";
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder().addLong("time", System.currentTimeMillis());
        for (FileUploadVo fileUploadVo : fileUploadVos) {
            jobParametersBuilder.addString("fileName", fileUploadVo.getFileName());
            jobParametersBuilder.addString("mappingId", fileUploadVo.getMappingId());
            jobParametersBuilder.addString("mappingName", fileUploadVo.getMappingName());
            jobParametersBuilder.addString("bizType", fileUploadVo.getBizType());
            jobParametersBuilder.addString("bizDate", fileUploadVo.getBizDate());
            jobParametersBuilder.addString("path", path);
            JobParameters jobParameters = jobParametersBuilder.toJobParameters();
            String fileType=dataGatherDetailMapper.getFileType(fileUploadVo.getMappingId());
            if("1".equals(fileType)) { // dbf 文件
                myJobLauncher().run(dbfCommonJob, jobParameters);
            }else if("2".equals(fileType)) { // 分隔符/固定列文件 (txt,csv,tsv)
                JobExecution jobExecution = myJobLauncher().run(commonJob, jobParameters);
            }else if("3".equals(fileType)) { // xml 文件
                myJobLauncher().run(XMLCommonJob, jobParameters);
            }
        }
        // Thread.sleep(8000);
        // STARTING, STARTED, STOPPING, STOPPED, FAILED, COMPLETED, ABANDONED
        //return "BatchStatus: " + jobExecution.getStatus().toString() + " ExitStatus:" + jobExecution.getExitStatus();
        return "==";
    }
}