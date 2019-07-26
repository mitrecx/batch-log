package cn.mitrecx.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cn.com.yusys.yusp.commons.exception.YuspRuntimeException;
import cn.mitrecx.domain.entity.FileMappingEntity;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @param <T>
 * @time 2019年7月19日, 上午11:19:50
 * 
 */
public class CommonItemReader<T> implements ItemReader<T> {
    private BufferedReader reader;
    @Autowired
    private DataGatherDetailMapper dataGatherDetailMapper;
    
    private FileMappingEntity fileMappingEntity;
    private String mappingId;
    private AtomicInteger lineCount = new AtomicInteger(1);
    private long startLine;
    private long endLine;
    
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Map<String, String> readResult = new HashMap<String, String>();
        try {
            // 防止并发读取导致 行号错乱
            synchronized (CommonItemReader.class) {
                String line = reader.readLine();
                if (line != null) {
                    if (lineCount.intValue() > this.endLine) {
                        return null;
                    }
                    if (!line.contains("TRAILER") && !line.contains("HEADER") && lineCount.intValue() > startLine && lineCount.intValue() <= endLine) {
                        FileMappingEntity fileMappingEntity = dataGatherDetailMapper.getFileMapping(mappingId);
                        String encodingType = fileMappingEntity.getEncodingType();
                        // 解析类型: 1-固定列, 2-分隔符 (文件类型为2-txt/csv/tsv的文件 只有这两种解析类型)
                        String parseType = fileMappingEntity.getParseType();
                        if ("1".equals(parseType)) { // 固定列
                            String parseRule = fileMappingEntity.getParseRule();
                            
                            parseFixedColumn(parseRule, line, encodingType, readResult);

                        } else if ("2".equals(parseType)) {// 分隔符
                            String separator = dataGatherDetailMapper.getParseRule(mappingId);
                            String[] array = line.split(separator);
                            int length = array.length;
                            for (int i = 0; i < length; i++) {
                                readResult.put("column" + i, array[i].trim());
                            }
                            readResult.put("rowNum", lineCount + "");
                            //System.out.println(lineCount + ": " + line);
                            // System.out.println(lineCount);
                        }

                    }

                    lineCount.incrementAndGet();
                    return (T) readResult;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 固定列 解析
     * 
     * @param parseRule 固定列的解析规则
     * @param line 文件行数据
     * @param encodingType 编码类型
     * @param readResult 读取结果
     */
    private void parseFixedColumn(String parseRule, String line, String encodingType, Map<String, String> readResult) {
        String[] fixLengthArr = parseRule.split(",");

        int beginIndex = 0;
        int endIndex = 0;
        int i = 0;
        byte[] bytes;
        try {
            bytes = line.getBytes(encodingType);
            for (String fixLengthStr : fixLengthArr) {
                String column = "column" + i;
                int fixLength = 0;
                try {
                    fixLength = Integer.parseInt(fixLengthStr.trim());
                } catch (Exception e) {
                    throw new YuspRuntimeException("解析规则错误, 固定列需配置逗号分隔的数字！", "31030009");
                }
                endIndex = beginIndex + fixLength;
                String value = "";
                if (beginIndex > bytes.length) {
                    value = "";
                } else {
                    value = new String(Arrays.copyOfRange(bytes, beginIndex, endIndex), encodingType).trim();
                }
                readResult.put(column, value);
                beginIndex = endIndex;
                i++;
            }
            readResult.put("rowNum", lineCount + "");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    

    public long getStartLine() {
        return startLine;
    }

    public void setStartLine(long startLine) {
        this.startLine = startLine;
    }

    public long getEndLine() {
        return endLine;
    }

    public void setEndLine(long endLine) {
        this.endLine = endLine;
    }

    public FileMappingEntity getFileMappingEntity() {
        return fileMappingEntity;
    }

    public void setFileMappingEntity(FileMappingEntity fileMappingEntity) {
        this.fileMappingEntity = fileMappingEntity;
    }

    public static void main(String[] args) {
        String file = "C:\\Users\\cx141\\Desktop\\20180330数据文件\\港股通\\cx\\20180330bond_valuation.txt";
        
    }
}
