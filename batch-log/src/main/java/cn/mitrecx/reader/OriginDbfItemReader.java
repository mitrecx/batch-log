package cn.mitrecx.reader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cn.com.yusys.yusp.commons.exception.YuspRuntimeException;
import cn.mitrecx.reader.dbf.DBFField;
import cn.mitrecx.reader.dbf.DBFReader;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月24日, 下午2:49:46
 * 
 */
public class OriginDbfItemReader<T> implements ItemReader<T> {
    DbfItemReader<Map<String, String>> dbfItemReader;
    @Autowired
    DataGatherDetailMapper dataGatherDetailMapper;

    private String mappingId;
    private String fileName;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        String encoding = dataGatherDetailMapper.getEncoding(mappingId);
        // String path = jobExecution.getJobParameters().getString("path") + "\\"; path + fileName
        // 获取 dbf 文件字段标识行(首行)
        InputStream fis = null;
        DBFReader readerStep2 = null;
        try {
            List<String> columnNameList = new ArrayList<>();
            fis = new FileInputStream(fileName);
            DBFReader reader = new DBFReader(fis);
            reader.setCharactersetName(encoding);
            
            InputStream fis2 = new FileInputStream(fileName);
            readerStep2=new DBFReader(fis2);
            readerStep2.setCharactersetName(encoding);
            int fieldsCount = reader.getFieldCount();
            for (int i = 0; i < fieldsCount; i++) {
                DBFField field = reader.getField(i);
                // CSRCC开头的文件字段名有的包含()%./等特色字符, 统一转成_
                String name = field.getName().trim().toUpperCase();
                //name = name.replaceAll("\\(|\\)|\\.|%|/", "_");
                columnNameList.add(name);
            }
            dbfItemReader.setHeadFields(columnNameList);
            dbfItemReader.setReader(readerStep2);
        } catch (Exception e) {
            throw new YuspRuntimeException("failed to get file's column name！", "31030007");
        } finally {
        }
        return null;
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DbfItemReader<Map<String, String>> getDbfItemReader() {
        return dbfItemReader;
    }

    public void setDbfItemReader(DbfItemReader<Map<String, String>> dbfItemReader) {
        this.dbfItemReader = dbfItemReader;
    }

}
