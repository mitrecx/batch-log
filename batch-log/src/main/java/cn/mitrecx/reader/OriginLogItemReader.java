package cn.mitrecx.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import cn.mitrecx.repository.ParseIMIXMapper;

/**
 * @author cx
 * @time 2019年7月9日, 下午2:47:05
 * 
 */
public class OriginLogItemReader<T> implements ItemReader<T> {
    private BufferedReader reader;
    private BufferedWriter buyBackWriter;
    private BufferedWriter bondTradeWriter;
    // 业务日期
    private String date;
    @Autowired
    private ParseIMIXMapper parseIMIXMapper;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (date == null) {
            throw new Exception("业务日期 不能为空, date格式: yyyy-MM-dd");
        } else if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new Exception("业务日期 格式: yyyy-MM-dd");
        }
        parseIMIXMapper.deleteBuyback(date);
        parseIMIXMapper.deleteBond(date);
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                // 如果行是outgoing(包含 627= ), 则不予处理
                String rePass = "^((?!" + (char) 0x01 + "627=).)*$";
                // 质押式/买断式 回购市场 匹配行
                String reBuyBackPledgeLine = "^<" + date + ".*?150=F.*?10176=(9|10)" + (char) 0x01 + ".*";
                // 现券买卖
                String reBondLine = "^<" + date + ".*?150=F.*?10176=4" + (char) 0x01 + ".*";
                // 如果行是 质押式/买断式 回购市场 数据
                if (line.matches(rePass) && Pattern.matches(reBuyBackPledgeLine, line)) {
                    buyBackWriter.write(line+"\r\n");
                }
                // 如果行是 现券买卖市场 数据
                if (line.matches(rePass) && Pattern.matches(reBondLine, line)) {
                    bondTradeWriter.write(line+"\r\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //TODO
//            reader.close();
//            buyBackWriter.close();
//            bondTradeWriter.close();
//            reader.mark(0);
//            reader.reset();
        }
        return (T) line;
    }

    
    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public BufferedWriter getBuyBackWriter() {
        return buyBackWriter;
    }

    public void setBuyBackWriter(BufferedWriter buyBackWriter) {
        this.buyBackWriter = buyBackWriter;
    }

    public BufferedWriter getBondTradeWriter() {
        return bondTradeWriter;
    }

    public void setBondTradeWriter(BufferedWriter bondTradeWriter) {
        this.bondTradeWriter = bondTradeWriter;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
