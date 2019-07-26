package cn.mitrecx.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import cn.mitrecx.domain.entity.FileMappingEntity;
import cn.mitrecx.processor.CommonItemProcessor;
import cn.mitrecx.processor.DbfItemProcessor;
import cn.mitrecx.reader.CommonItemReader;
import cn.mitrecx.reader.DbfItemReader;
import cn.mitrecx.reader.OriginCommonItemReader;
import cn.mitrecx.reader.OriginDbfItemReader;
import cn.mitrecx.reader.XMLItemReader;
import cn.mitrecx.reader.dbf.DBFException;
import cn.mitrecx.reader.dbf.DBFReader;
import cn.mitrecx.repository.DataGatherDetailMapper;
import cn.mitrecx.writer.CommonItemWriter;

/**
 * @author cx
 * @time 2019年7月18日, 下午3:12:15
 * 
 */
@Configuration
@EnableBatchProcessing
public class ProcessCommonConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataGatherDetailMapper dataGatherDetailMapper;

    @Bean
    @JobScope
    public CommonItemReader<Map<String, String>> commonItemReader(@Value("#{JobParameters['path']}") String path, @Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId, OriginCommonItemReader<Map<String, String>> originCommonItemReader) {
        // System.out.println("ItemReader--------ItemReader");
        CommonItemReader<Map<String, String>> itemReader = new CommonItemReader<Map<String, String>>();
        try {
            InputStream isBuyBack = new FileInputStream(new File(path + "\\" + fileName));
            // 文件编码
            String encoding = dataGatherDetailMapper.getEncoding(mappingId);
            InputStreamReader iswBuyBack = new InputStreamReader(isBuyBack, encoding);
            BufferedReader br = new BufferedReader(iswBuyBack);
            itemReader.setReader(br);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        itemReader.setMappingId(mappingId);
        return itemReader;
    }
    @Bean
    @JobScope
    public XMLItemReader<Map<String, String>> xmlItemReader(@Value("#{JobParameters['path']}") String path, @Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId) {
        XMLItemReader<Map<String, String>> itemReader = new XMLItemReader<Map<String, String>>();
        SAXReader reader = new SAXReader();
        FileMappingEntity fileMappingEntity =dataGatherDetailMapper.getFileMapping(mappingId);
        String parentElementName= fileMappingEntity.getParseRule();
        File file = new File(path + "\\" + fileName);
        try {
            Document document = reader.read(file);
            Element root = document.getRootElement();
            Iterator iterator = root.elementIterator(parentElementName);
            itemReader.setIterator(iterator);
        }catch (Exception e) {
            // TODO: handle exception
        }
        return itemReader;
    }

    @Bean
    @JobScope
    public DbfItemReader<Map<String, String>> dbfItemReader(@Value("#{JobParameters['path']}") String path, @Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId) {
        // System.out.println("DBF------ItemReader");
        DbfItemReader<Map<String, String>> itemReader = new DbfItemReader<Map<String, String>>();
        try {
            InputStream is = new FileInputStream(new File(path + "\\" + fileName));
            // 文件编码
            String encoding = dataGatherDetailMapper.getEncoding(mappingId);
            DBFReader reader = new DBFReader(is);
            reader.setCharactersetName(encoding);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DBFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        itemReader.setMappingId(mappingId);
        return itemReader;
    }

    @Bean
    @StepScope
    public OriginDbfItemReader<Map<String, String>> originDbfItemReader(@Value("#{JobParameters['path']}") String path, @Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId, DbfItemReader<Map<String, String>> dbfItemReader) {
        // System.out.println("origin--DBF---ItemReader");
        OriginDbfItemReader<Map<String, String>> itemReader = new OriginDbfItemReader<Map<String, String>>();
        itemReader.setMappingId(mappingId);
        itemReader.setFileName(path + "\\" + fileName);
        // 为 dbfItemReader(bean) 的成员变量 headFields , reader 设置值.
        itemReader.setDbfItemReader(dbfItemReader);
        return itemReader;
    }

    /**
     * 为 CommonItemReader 准备 startLine, endLine , fileMappingEntity
     */
    @Bean
    @StepScope
    public OriginCommonItemReader<Map<String, String>> originCommonItemReader(@Value("#{JobParameters['path']}") String path, @Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId,CommonItemReader<Map<String, String>> commonItemReader) {
        OriginCommonItemReader<Map<String, String>> itemReader = new OriginCommonItemReader<Map<String, String>>();
        itemReader.setCommonItemReader(commonItemReader);
        itemReader.setMappingId(mappingId);
        itemReader.setFileName(path + "\\" + fileName);
        return itemReader;
    }

    @Bean
    @StepScope
    public CommonItemProcessor commonItemProcessor(@Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId) {
        return new CommonItemProcessor(fileName, mappingId);
    }

    @Bean
    @StepScope
    public DbfItemProcessor dbfItemProcessor(@Value("#{JobParameters['fileName']}") String fileName, @Value("#{JobParameters['mappingId']}") String mappingId) {
        return new DbfItemProcessor(fileName, mappingId);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    @Scope("prototype")
    public CommonItemWriter<Map<String, Object>> commonItemWriter() {
        return new CommonItemWriter<Map<String, Object>>();
    }

    @Bean
    @JobScope
    public Step step1(OriginCommonItemReader<Map<String, String>> origincommonItemReader, CommonItemProcessor commonItemProcessor, CommonItemWriter<Map<String, Object>> commonItemWriter) {
        StepBuilder stepBuilder = stepBuilderFactory.get("step1");
        SimpleStepBuilder<Map<String, String>, Map<String, Object>> simpleStepBuilder = stepBuilder.<Map<String, String>, Map<String, Object>>chunk(300);
        // ItemReader
        simpleStepBuilder = simpleStepBuilder.reader(origincommonItemReader);
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(commonItemProcessor);
        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(commonItemWriter);
        TaskletStep taskletStep = simpleStepBuilder.build();
        return taskletStep;
    }

    @Bean
    @JobScope
    public Step step2(CommonItemReader<Map<String, String>> commonItemReader, CommonItemProcessor commonItemProcessor, CommonItemWriter<Map<String, Object>> commonItemWriter, TaskExecutor taskExecutor) {
        StepBuilder stepBuilder = stepBuilderFactory.get("step2");
        SimpleStepBuilder<Map<String, String>, Map<String, Object>> simpleStepBuilder = stepBuilder.<Map<String, String>, Map<String, Object>>chunk(300);
        // ItemReader
        simpleStepBuilder = simpleStepBuilder.reader(commonItemReader);
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(commonItemProcessor);
        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(commonItemWriter);
        TaskletStep taskletStep = simpleStepBuilder.build();
        return taskletStep;
//        return simpleStepBuilder.taskExecutor(taskExecutor).throttleLimit(10).build();
    }
    @Bean
    @JobScope
    public Step xmlStep1(XMLItemReader<Map<String, String>> xmlItemReader, CommonItemProcessor commonItemProcessor, CommonItemWriter<Map<String, Object>> commonItemWriter) {
        StepBuilder stepBuilder = stepBuilderFactory.get("xmlStep1");
        SimpleStepBuilder<Map<String, String>, Map<String, Object>> simpleStepBuilder = stepBuilder.<Map<String, String>, Map<String, Object>>chunk(300);
        // ItemReader
        simpleStepBuilder = simpleStepBuilder.reader(xmlItemReader);
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(commonItemProcessor);
        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(commonItemWriter);
        TaskletStep taskletStep = simpleStepBuilder.build();
        return taskletStep;
//        return simpleStepBuilder.taskExecutor(taskExecutor).throttleLimit(10).build();
    }

    @Bean
    @Scope("prototype")
    public Job commonJob(CommonListener listener, Step step1, Step step2) {
        String[] requiredKeys = { "mappingId" };
        String[] optionalKeys = new String[0];
        JobBuilder jobBuilder = jobBuilderFactory.get("commonJob").validator(new DefaultJobParametersValidator(requiredKeys, optionalKeys));
        jobBuilder = jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder = jobBuilder.listener(listener);

        SimpleJobBuilder JobFlowBuilder = jobBuilder.start(step1).next(step2);
        Job job = JobFlowBuilder.build();
        return job;
    }
    @Bean
    @Scope("prototype")
    public Job XMLCommonJob(DbfListener listener, Step xmlStep1) {
        String[] requiredKeys = { "mappingId" };
        String[] optionalKeys = new String[0];
        JobBuilder jobBuilder = jobBuilderFactory.get("XMLJob").validator(new DefaultJobParametersValidator(requiredKeys, optionalKeys));
        jobBuilder = jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder = jobBuilder.listener(listener);
        
        SimpleJobBuilder JobFlowBuilder = jobBuilder.start(xmlStep1);
        Job job = JobFlowBuilder.build();
        return job;
    }

    @Bean
    @JobScope
    public Step dbfStep1(OriginDbfItemReader<Map<String, String>> dbfItemReader, DbfItemProcessor dbfItemProcessor, CommonItemWriter<Map<String, Object>> commonItemWriter) {
        StepBuilder stepBuilder = stepBuilderFactory.get("dbfStep1");
        SimpleStepBuilder<Map<String, String>, Map<String, Object>> simpleStepBuilder = stepBuilder.<Map<String, String>, Map<String, Object>>chunk(300);
        // ItemReader
        simpleStepBuilder = simpleStepBuilder.reader(dbfItemReader);
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(dbfItemProcessor);
        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(commonItemWriter);
        TaskletStep taskletStep = simpleStepBuilder.build();
        return taskletStep;
        // return simpleStepBuilder.taskExecutor(taskExecutor).throttleLimit(10).build();
    }

    @Bean
    @JobScope
    public Step dbfStep2(DbfItemReader<Map<String, String>> dbfItemReader, DbfItemProcessor dbfItemProcessor, CommonItemWriter<Map<String, Object>> commonItemWriter) {
        StepBuilder stepBuilder = stepBuilderFactory.get("dbfStep2");
        SimpleStepBuilder<Map<String, String>, Map<String, Object>> simpleStepBuilder = stepBuilder.<Map<String, String>, Map<String, Object>>chunk(300);
        // ItemReader
        simpleStepBuilder = simpleStepBuilder.reader(dbfItemReader);
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(dbfItemProcessor);
        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(commonItemWriter);
        TaskletStep taskletStep = simpleStepBuilder.build();
        return taskletStep;
        // return simpleStepBuilder.taskExecutor(taskExecutor).throttleLimit(10).build();
    }

    @Bean
    @Scope("prototype")
    public Job dbfCommonJob(DbfListener dbfListener, Step dbfStep1, Step dbfStep2) {
        String[] requiredKeys = { "mappingId" };
        String[] optionalKeys = new String[0];
        JobBuilder jobBuilder = jobBuilderFactory.get("dbfCommonJob").validator(new DefaultJobParametersValidator(requiredKeys, optionalKeys));
        jobBuilder = jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder = jobBuilder.listener(dbfListener);

        SimpleJobBuilder JobFlowBuilder = jobBuilder.start(dbfStep1).next(dbfStep2);
        Job job = JobFlowBuilder.build();
        return job;
    }
}
