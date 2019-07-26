package cn.mitrecx.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import cn.mitrecx.reader.dbf.DBFReader;

/**
 * @author cx
 * @time 2019年7月23日, 上午11:17:53
 * 
 */
public class DbfItemReader<T> implements ItemReader<T> {
    private DBFReader reader;
    private List<String> headFields;
    private String mappingId;
    // 行号
    private AtomicInteger lineCount = new AtomicInteger(0);
    
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Object[] rowValues = null;  
        Map<String, String> readResult = null;
        if((rowValues = reader.nextRecord()) != null){
            int length = rowValues.length;
            readResult = new HashMap<String, String>();
            for (int i = 0; i < length; i++) {
                readResult.put(headFields.get(i), rowValues[i].toString().trim());
            }

            readResult.put("rowNum", lineCount.addAndGet(1) + "");
            return (T)readResult;
        }
        return null;
    }
    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }
    public List<String> getHeadFields() {
        return headFields;
    }
    public void setHeadFields(List<String> headFields) {
        this.headFields = headFields;
    }
    public DBFReader getReader() {
        return reader;
    }
    public void setReader(DBFReader reader) {
        this.reader = reader;
    }
    
}
