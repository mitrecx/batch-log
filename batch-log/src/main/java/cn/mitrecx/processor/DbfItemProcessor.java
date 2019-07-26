package cn.mitrecx.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;

import cn.com.yusys.yusp.commons.util.DateUtil;
import cn.com.yusys.yusp.commons.util.StringUtil;
import cn.mitrecx.domain.entity.FileMappingDetailsEntity;
import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月19日, 下午1:56:33
 * 
 */
public class DbfItemProcessor implements ItemProcessor<Map<String, String>, Map<String, Object>> {
    @Autowired
    private DataGatherDetailMapper dataGatherDetailMapper;
    private String fileName;
    private String mappingId;


    public DbfItemProcessor() {
    }

    public DbfItemProcessor(String fileName, String mappingId) {
        this.fileName = fileName;
        this.mappingId = mappingId;
    }

    @Override
    public Map<String, Object> process(Map<String, String> map) throws Exception {
        List<FileMappingDetailsEntity> fileMappingDetails = dataGatherDetailMapper.getFileMappingDetails(mappingId);
        map.put("fileDate", DateUtil.format(new Date(), DateUtil.PATTERN_DATETIME));
        map.put("fileName", fileName);
        
        return convert(fileMappingDetails, map);
    }

    /**
     * 
     * @param fileMappingDetails test_file_mapping_details 表的字段映射关系
     * @param fileColumnMap reader返回的 文件字段map
     * @return 返回 map: key 数据库表字段, value 表字段对应的值
     */
    Map<String, Object> convert(List<FileMappingDetailsEntity> fileMappingDetails, Map<String, String> fileColumnMap) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (FileMappingDetailsEntity fileMappingDetailsEntity : fileMappingDetails) {
            // 文件列名为空, 映射表达式为空
            if (fileMappingDetailsEntity.getFileColumnName() == null && fileMappingDetailsEntity.getFileColumnExp() == null) {
                continue;
                // 文件列名 <不空>, 映射表达式为空
            } else if (fileMappingDetailsEntity.getFileColumnName() != null && fileMappingDetailsEntity.getFileColumnExp() == null) {
                map.put(fileMappingDetailsEntity.getTableColumnName(), fileColumnMap.get(fileMappingDetailsEntity.getFileColumnName().toUpperCase()));
                // 文件列名为空, 映射表达式 <不空>
            } else if (fileMappingDetailsEntity.getFileColumnName() == null && fileMappingDetailsEntity.getFileColumnExp() != null) {
                String exp = fileMappingDetailsEntity.getFileColumnExp();
                map.put(fileMappingDetailsEntity.getTableColumnName(), getStaticInfo(exp, fileColumnMap));
            }
        }
        return map;
    }

    /**
     * 获取映射表达式的 实际值
     * 
     * @param fileName 文件名
     * @param exp 映射表达式
     * @return
     */
    Object getStaticInfo(String exp, Map<String, String> fileColumnMap) {
        // 文件名
        if (exp.replaceAll("\\s", "").toLowerCase().equals("date from file name".replaceAll("\\s", "").toLowerCase())) {
            String re = "\\d{8,10}";
            Pattern pattern = Pattern.compile(re);
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                return matcher.group();
            }
            return null;
        }
        if (exp.replaceAll("\\s", "").toLowerCase().equals("row num".replaceAll("\\s", "").toLowerCase())) { // TODO
            return Integer.parseInt(fileColumnMap.get("rowNum"));
        }
        if (exp.replaceAll("\\s", "").toLowerCase().equals("unique index".replaceAll("\\s", "").toLowerCase())) { // TODO
            // externalId
            return DateUtil.format(DateUtil.getCurrentTime(), DateUtil.PATTERN_DATETIME3) + StringUtil.getNextSeq3();
        }
        if (exp.replaceAll("\\s", "").toLowerCase().contains("#{")) {
            Object result = null;
            String re = "#\\{\\w*?\\}";
            Pattern p = Pattern.compile(re);
            Matcher m = p.matcher(exp);
            List<String> varList = new LinkedList<String>();
            while (m.find()) {
                //System.out.println(m.group());
                varList.add(m.group());
            }
            int size = varList.size();
            for (int i = 0; i < size; i++) {
                exp = exp.replace(varList.get(i), "\"" + fileColumnMap.get(varList.get(i).substring(2, varList.get(i).length() - 1)) + "\"");
            }
            //System.out.println(exp);
            ExpressRunner runner = new ExpressRunner();
            DefaultContext<String, Object> context = new DefaultContext<>();
            try {
                result = runner.execute(exp, context, null, true, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        if (exp.replaceAll("\\s", "").toLowerCase().contains("return")) {
            Object result = null;
            ExpressRunner runner = new ExpressRunner();
            DefaultContext<String, Object> context = new DefaultContext<>();
            try {
                result = runner.execute(exp, context, null, true, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        if (!exp.replaceAll("\\s", "").equals("")) {
            Object result = null;
            ExpressRunner runner = new ExpressRunner();
            DefaultContext<String, Object> context = new DefaultContext<>();
            try {
                result = runner.execute(exp, context, null, true, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        return exp;
    }

    public static void main(String[] args) {
        String exp="return new Date()";
        Object result;
        ExpressRunner runner = new ExpressRunner();
        DefaultContext<String, Object> context = new DefaultContext<>();
        try {
            result = runner.execute(exp, context, null, true, false);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }

}
