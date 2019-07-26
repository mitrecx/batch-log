package cn.mitrecx.reader;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cn.mitrecx.domain.entity.FileMappingEntity;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月24日, 下午2:49:46
 * 
 */
public class OriginCommonItemReader<T> implements ItemReader<T> {
    @Autowired
    DataGatherDetailMapper dataGatherDetailMapper;
    CommonItemReader<Map<String, String>> commonItemReader;
    private FileMappingEntity fileMappingEntity;
    private String mappingId;
    private String fileName;

    private long startLine = 0;
    private long endLine = Long.MAX_VALUE;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        this.fileMappingEntity = dataGatherDetailMapper.getFileMapping(mappingId);
        BigDecimal excludeHead = fileMappingEntity.getExcludeHead();
        BigDecimal excludeTail = fileMappingEntity.getExcludeTail();
        if (excludeHead != null) {
            startLine = excludeHead.longValue();
        }
        long excludeTailLong = 0;
        if (excludeTail != null) {
            excludeTailLong = excludeTail.longValue();
        }
        // 文件总行数
        long lineTotal = Long.MAX_VALUE;
        FileReader fileReader = null;
        LineNumberReader lineNumberReader = null;
        try {
            fileReader = new FileReader(fileName);
            lineNumberReader = new LineNumberReader(fileReader);
            lineNumberReader.skip(Long.MAX_VALUE);
            lineTotal = lineNumberReader.getLineNumber() + 1;
            this.endLine = lineTotal - excludeTailLong;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                lineNumberReader.close();
            } catch (Exception e) {
            }
        }
        commonItemReader.setEndLine(endLine);
        commonItemReader.setStartLine(startLine);
        commonItemReader.setFileMappingEntity(fileMappingEntity);
        System.out.println("startLine " + startLine);
        System.out.println("endLine " + endLine);
        return null;
    }

    public CommonItemReader<Map<String, String>> getCommonItemReader() {
        return commonItemReader;
    }

    public void setCommonItemReader(CommonItemReader<Map<String, String>> commonItemReader) {
        this.commonItemReader = commonItemReader;
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

}
