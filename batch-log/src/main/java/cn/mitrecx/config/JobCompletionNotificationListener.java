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
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cn.mitrecx.reader.BondTradeLogItemReader;
import cn.mitrecx.reader.BuyBackLogItemReader;
import cn.mitrecx.repository.ParseIMIXMapper;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final static String encoding = "GBK";

    private final static String logBuyBack = "C:\\Users\\cx141\\Desktop\\imix\\agent--BuyBack.log";
    private final static String logBondTrade = "C:\\Users\\cx141\\Desktop\\imix\\agent--BondTrade.log";
    private final JdbcTemplate jdbcTemplate;
    // 作业开始时间
    private long jobStartTime;
    private static int jobCount = 0;

    @Autowired
    private ParseIMIXMapper parseIMIXMapper;
//    @Autowired
//    private String bizDate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

        String date = jobExecution.getJobParameters().getString("date");
        String filePath = jobExecution.getJobParameters().getString("filePath");
        log.info("jobCount " + (++jobCount));
        jobStartTime = System.currentTimeMillis();
        log.info("===处理 log 入库 作业 开始 ===");

        String line = null;
        try {

            InputStream is = new FileInputStream(new File(filePath));
            InputStreamReader isr = new InputStreamReader(is, encoding);
            BufferedReader reader = new BufferedReader(isr);

            OutputStream osBuyBack = new FileOutputStream(new File(logBuyBack));
            OutputStreamWriter oswBuyBack = new OutputStreamWriter(osBuyBack, encoding);
            BufferedWriter bwBuyBack = new BufferedWriter(oswBuyBack);

            OutputStream osBondTrade = new FileOutputStream(new File(logBondTrade));
            OutputStreamWriter oswBondTrade = new OutputStreamWriter(osBondTrade, encoding);
            BufferedWriter bwBondTrade = new BufferedWriter(oswBondTrade);
            while ((line = reader.readLine()) != null) {
                // 如果行是outgoing(包含 627= ), 则不予处理
                String rePass = "^((?!" + (char) 0x01 + "627=).)*$";
                // 质押式/买断式 回购市场 匹配行
                String reBuyBackPledgeLine = "^<" + date + ".*?150=F.*?10176=(9|10)" + (char) 0x01 + ".*";
                // 现券买卖
                String reBondLine = "^<" + date + ".*?150=F.*?10176=4" + (char) 0x01 + ".*";
                // 如果行是 质押式/买断式 回购市场 数据
                if (line.matches(rePass) && Pattern.matches(reBuyBackPledgeLine, line)) {
                    bwBuyBack.write(line + "\r\n");
                }
                // 如果行是 现券买卖市场 数据
                if (line.matches(rePass) && Pattern.matches(reBondLine, line)) {
                    bwBondTrade.write(line + "\r\n");
                }
            }
            reader.close();
            bwBuyBack.close();
            bwBondTrade.close();

            // 重置流--回购
            InputStream isBuyBack = new FileInputStream(new File(logBuyBack));
            InputStreamReader iswBuyBack = new InputStreamReader(isBuyBack, encoding);
            BufferedReader brBuyBack = new BufferedReader(iswBuyBack);
            BuyBackLogItemReader.reader = brBuyBack;
            // 重置流--现券
            InputStream isBondTrade = new FileInputStream(new File(logBondTrade));
            InputStreamReader iswBondTrade = new InputStreamReader(isBondTrade, encoding);
            BufferedReader brBondTrade = new BufferedReader(iswBondTrade);
            BondTradeLogItemReader.reader = brBondTrade;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        parseIMIXMapper.deleteBuyback(date);
        parseIMIXMapper.deleteBond(date);

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("处理 log 入库 作业 已完成 !");
//                jdbcTemplate.query("select count(1) from T_VALEXT_BANK_BUYBACKTRADE", (rs, row) -> new Integer(rs.getString(1))).forEach(count -> log.info("回购表插入 <" + count + "> 条数据."));
//                jdbcTemplate.query("select count(1) from T_VALEXT_BANK_BONDTRADE", (rs, row) -> new Integer(rs.getString(1))).forEach(count -> log.info("现券买卖表插入 <" + count + "> 条数据."));
            log.info("插入回购表 " + parseIMIXMapper.getCountBuyBack(jobExecution.getJobParameters().getString("date")) + " 条数据");
            log.info("插入现券买卖表 " + parseIMIXMapper.getCountBondTrade(jobExecution.getJobParameters().getString("date")) + " 条数据");
            long jobEndTime = System.currentTimeMillis();
            log.info("解析+插入 总耗时: " + (jobEndTime - jobStartTime) / 1000.0 + " 秒");
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            // job failure
            parseIMIXMapper.deleteBuyback(jobExecution.getJobParameters().getString("date"));
            parseIMIXMapper.deleteBond(jobExecution.getJobParameters().getString("date"));
            log.info("job 执行 失败 ! ! ! ");
        }
    }

}
