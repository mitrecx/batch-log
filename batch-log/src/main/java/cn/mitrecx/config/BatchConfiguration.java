package cn.mitrecx.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import cn.mitrecx.domain.DictEntity;
import cn.mitrecx.processor.BondTradeItemProcessor;
import cn.mitrecx.processor.BuyBackItemProcessor;
import cn.mitrecx.processor.OriginLogItemProcessor;
import cn.mitrecx.reader.BondTradeLogItemReader;
import cn.mitrecx.reader.BuyBackLogItemReader;
import cn.mitrecx.reader.OriginLogItemReader;
import cn.mitrecx.repository.ParseIMIXMapper;
import cn.mitrecx.writer.BondTradeItemWriter;
import cn.mitrecx.writer.BuyBackItemWriter;
import cn.mitrecx.writer.OriginLogItemWriter;

//@Configuration
//@EnableBatchProcessing
public class BatchConfiguration {

    private final static String encoding = "GBK";
    private final static String csv = "C:\\Users\\cx141\\WSWork0419\\batch-log\\src\\main\\resources\\sample-data.csv";

    // private final static String log = "C:\\Users\\cx141\\Desktop\\imix\\agent.log";
    private final static String logBuyBack = "C:\\Users\\cx141\\Desktop\\imix\\agent--BuyBack.log";
    private final static String logBondTrade = "C:\\Users\\cx141\\Desktop\\imix\\agent--BondTrade.log";
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    public DataSource dataSource;

    @Autowired
    private ParseIMIXMapper parseIMIXMapper;

    /**
     * 读 原始log 文件 OriginLogItemReader --- ItemReader
     */
    @Bean
    @StepScope
    public OriginLogItemReader<String> originLogItemReader(@Value("#{JobParameters['filePath']}") String filePath, @Value("#{JobParameters['date']}") String date) {
        OriginLogItemReader<String> originLogReader = new OriginLogItemReader<String>();
        try {
            InputStream is = new FileInputStream(new File(filePath));
            InputStreamReader isr = new InputStreamReader(is, encoding);
            BufferedReader br = new BufferedReader(isr);

            OutputStream osBuyBack = new FileOutputStream(new File(logBuyBack));
            OutputStreamWriter oswBuyBack = new OutputStreamWriter(osBuyBack, encoding);
            BufferedWriter bwBuyBack = new BufferedWriter(oswBuyBack);

            OutputStream osBondTrade = new FileOutputStream(new File(logBondTrade));
            OutputStreamWriter oswBondTrade = new OutputStreamWriter(osBondTrade, encoding);
            BufferedWriter bwBondTrade = new BufferedWriter(oswBondTrade);

            originLogReader.setReader(br);
            originLogReader.setBuyBackWriter(bwBuyBack);
            originLogReader.setBondTradeWriter(bwBondTrade);
            originLogReader.setDate(date);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return originLogReader;
    }

    /**
     * 读 回购市场类 log BuyBackLogItemReader --- ItemReader
     */
    @Bean
    public BuyBackLogItemReader<String> buyBackLogItemReader() {
        System.out.println("BuyBackLogItemReader---------BuyBackLogItemReader");
        BuyBackLogItemReader<String> buyBackLogItemReader = new BuyBackLogItemReader<String>();
        try {
            InputStream isBuyBack = new FileInputStream(new File(logBuyBack));
            InputStreamReader iswBuyBack = new InputStreamReader(isBuyBack, encoding);
            BufferedReader brBuyBack = new BufferedReader(iswBuyBack);
            BuyBackLogItemReader.reader = brBuyBack;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return buyBackLogItemReader;
    }

    /**
     * 读 现券买卖 市场类 log BondTradeLogItemReader --- ItemReader
     */
    @Bean
    public BondTradeLogItemReader<String> bondTradeLogItemReader() {
        BondTradeLogItemReader<String> bondTradeLogItemReader = new BondTradeLogItemReader<String>();
        try {
            InputStream isBondTrade = new FileInputStream(new File(logBondTrade));
            InputStreamReader iswBondTrade = new InputStreamReader(isBondTrade, encoding);
            BufferedReader brBondTrade = new BufferedReader(iswBondTrade);

            BondTradeLogItemReader.reader = brBondTrade;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bondTradeLogItemReader;
    }

    /**
     * ItemProcessor
     */
    @Bean
    public OriginLogItemProcessor originLogItemProcessor() {
        return new OriginLogItemProcessor();
    }

    @Bean
    @StepScope
    public BuyBackItemProcessor buyBackItemProcessor(@Value("#{JobParameters['date']}") String date) {
        String dictCodeBuyback = "DATA_IMIX_BUYBACK";
        List<DictEntity> buybackDicts = parseIMIXMapper.getDict(dictCodeBuyback);
        return new BuyBackItemProcessor(buybackDicts, date);
    }

    @Bean
    @StepScope
    public BondTradeItemProcessor bondTradeItemProcessor(@Value("#{JobParameters['date']}") String date) {
        String dictCodeBond = "DATA_IMIX_BOND";
        List<DictEntity> bondTradeDicts = parseIMIXMapper.getDict(dictCodeBond);
        return new BondTradeItemProcessor(bondTradeDicts, date);
    }

    /**
     * OriginLogItemWriter ---ItemWriter
     */
    @Bean
    public OriginLogItemWriter<String> originLogItemWriter() {
        OriginLogItemWriter<String> originLogItemWriter = new OriginLogItemWriter<String>();
        return originLogItemWriter;
    }

    @Bean
    public BuyBackItemWriter<Map<String, String>> buyBackItemWriter() {
        BuyBackItemWriter<Map<String, String>> buyBackItemWriter = new BuyBackItemWriter<Map<String, String>>();
        return buyBackItemWriter;
    }

    @Bean
    public BondTradeItemWriter<Map<String, String>> bondTradeItemWriter() {
        BondTradeItemWriter<Map<String, String>> bondTradeItemWriter = new BondTradeItemWriter<Map<String, String>>();
        return bondTradeItemWriter;
    }

    /**
     * Job 配置
     */
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1, Flow splitFlow) {
        String[] requiredKeys = {"date"};
        String[] optionalKeys=new String[0];
        JobBuilder jobBuilder = jobBuilderFactory.get("importUserJob")
                .validator(new DefaultJobParametersValidator(requiredKeys,optionalKeys));
        jobBuilder = jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder = jobBuilder.listener(listener); 

//        SimpleJobBuilder JobFlowBuilder = jobBuilder.start(step1).next(step2);
//        Job job = JobFlowBuilder.build();
//        return job;
         return jobBuilder.start(splitFlow).build().build();
//        return jobBuilderFactory.get("importUserJob")
//                .start(splitFlow)
//                .next(step1)
//                .build()
//                .build();
    }

    @Bean
    public Step step1(OriginLogItemWriter<String> writer, OriginLogItemReader<String> originLogItemReader) {
        System.out.println(writer);
        StepBuilder stepBuilder = stepBuilderFactory.get("step1");
        SimpleStepBuilder<String, String> simpleStepBuilder = stepBuilder.<String, String>chunk(10);
        // ItemReader
        simpleStepBuilder = simpleStepBuilder.reader(originLogItemReader);
        // ItemProcessor
        OriginLogItemProcessor processor = originLogItemProcessor();
        simpleStepBuilder = simpleStepBuilder.processor(processor);

        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(writer);
        TaskletStep taskletStep = simpleStepBuilder.build();
        return taskletStep;
    }

    @Bean
    public Flow splitFlow(Flow flow1, Flow flow2) {
        return new FlowBuilder<SimpleFlow>("splitFlow").split(taskExecutor()).add(flow1, flow2).build();
    }

    @Bean
    public Flow flow1(Step step2) {
        return new FlowBuilder<SimpleFlow>("flow1").start(step2).build();
    }

    @Bean
    public Flow flow2(Step step3) {
        return new FlowBuilder<SimpleFlow>("flow2").start(step3).build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    /**
     * buyBack 入 Buyback 表 ---------------FINISHED
     * 
     */
    @Bean
    public Step step2(BuyBackItemWriter<Map<String, String>> buyBackItemWriter, BuyBackItemProcessor buyBackItemProcessor, TaskExecutor taskExecutor) {
        StepBuilder stepBuilder = stepBuilderFactory.get("step2");
        SimpleStepBuilder<String, Map<String, String>> simpleStepBuilder = stepBuilder.<String, Map<String, String>>chunk(1000);
        // ItemReader
        BuyBackLogItemReader<String> buyBackLogItemReader = buyBackLogItemReader();
        simpleStepBuilder = simpleStepBuilder.reader(buyBackLogItemReader);
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(buyBackItemProcessor);

        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(buyBackItemWriter);
//        TaskletStep taskletStep = simpleStepBuilder.build();
//        return taskletStep;
         return simpleStepBuilder.taskExecutor(taskExecutor).throttleLimit(4).build();
    }

    /**
     * 现券买卖 入 BondTrade 表 ---------------FINISHED
     * 
     */
    @Bean
    public Step step3(BondTradeItemWriter<Map<String, String>> bondTradeItemWriter, BondTradeItemProcessor bondTradeItemProcessor, TaskExecutor taskExecutor) {
        StepBuilder stepBuilder = this.stepBuilderFactory.get("step3");
        SimpleStepBuilder<String, Map<String, String>> simpleStepBuilder = stepBuilder.<String, Map<String, String>>chunk(1000);
        // ItemReader
        BondTradeLogItemReader<String> bondTradeLogItemReader = bondTradeLogItemReader(); 
        simpleStepBuilder = simpleStepBuilder.reader(bondTradeLogItemReader); 
        // ItemProcessor
        simpleStepBuilder = simpleStepBuilder.processor(bondTradeItemProcessor);

        // ItemWriter
        simpleStepBuilder = simpleStepBuilder.writer(bondTradeItemWriter);
//        TaskletStep taskletStep = simpleStepBuilder.build();
//        return taskletStep;
        return simpleStepBuilder.taskExecutor(taskExecutor).build();
    }

    /**
     * TODO: JobRepositoryFactoryBean
     * 
     * JobRepository: DataSource, TransactionManager, IsolationLevel(ISOLATION_REPEATABLE_READ),
     * TablePrefix(batch框架表前缀)
     * 
     * 
     * @return
     */
    @Bean
    public BatchConfigurer batchConfigurer() {
        return new DefaultBatchConfigurer(dataSource);
//            {
//                    @Override
//                    public PlatformTransactionManager getTransactionManager() {
//                            return new MyTransactionManager();
//                    }
//            };
    }

}
