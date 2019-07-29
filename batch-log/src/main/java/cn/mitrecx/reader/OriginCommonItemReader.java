package cn.mitrecx.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cn.mitrecx.domain.entity.FileMappingEntity;
import cn.mitrecx.processor.CommonItemProcessor;
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
    CommonItemProcessor commonItemProcessor;
    
    private FileMappingEntity fileMappingEntity;
    private String mappingId;
    private String fileName;

    private long startLine = 0;
    private long endLine = Long.MAX_VALUE;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        
        this.fileMappingEntity = dataGatherDetailMapper.getFileMapping(mappingId);
        test();
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

    void test() {
        //String separator = dataGatherDetailMapper.getParseRule(mappingId);
        // Map<String, String> headerTrailer = dataGatherDetailMapper.getFileMappingHeaderTailer(mappingId);
        String head = fileMappingEntity.getHeader();
        String tail = fileMappingEntity.getTrailer();
        String encoding = fileMappingEntity.getEncodingType();
        String separator = fileMappingEntity.getParseRule();
        if ((head != null && head.equals("Y")) || (tail != null && tail.equals("Y"))) {
//        if (headerTrailer != null && ((headerTrailer.get("trailer") != null && headerTrailer.get("trailer").equals("Y")) || (headerTrailer.get("header") != null && headerTrailer.get("header").equals("Y")))) {
            InputStream is;
            try {
                is = new FileInputStream(new File(fileName));

                InputStreamReader isr = new InputStreamReader(is, encoding);
                BufferedReader br = new BufferedReader(isr);
                String firstLine = "";
                String lastLine = "";
                int lineCnt = 1;
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (lineCnt == 1) {
                        firstLine = line;
                    }
                    lineCnt++;
                    lastLine = line;
                }
                // 特殊行--首行
                if (head != null && head.equals("Y")) {
                    String[] arrayfirst = firstLine.split(separator);
                    int length = arrayfirst.length;
                    Map<String, String> readfirst = new HashMap<String, String>();
                    for (int i = 0; i < length; i++) {
                        readfirst.put("column" + i, arrayfirst[i].trim());
                    }
                    commonItemProcessor.setHeader(readfirst);
                }
                // 特殊行--尾行
                if (tail != null && tail.equals("Y")) {
                    String[] arrayLast = lastLine.split(separator);
                    int length = arrayLast.length;
                    Map<String, String> readLast = new HashMap<String, String>();
                    for (int i = 0; i < length; i++) {
                        readLast.put("column" + i, arrayLast[i].trim());
                    }
                    commonItemProcessor.setTailer(readLast);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public CommonItemProcessor getCommonItemProcessor() {
        return commonItemProcessor;
    }

    public void setCommonItemProcessor(CommonItemProcessor commonItemProcessor) {
        this.commonItemProcessor = commonItemProcessor;
    }

}
