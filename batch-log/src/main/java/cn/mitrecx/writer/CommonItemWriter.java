package cn.mitrecx.writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cn.mitrecx.repository.DataGatherDetailMapper;

/**
 * @author cx
 * @time 2019年7月19日, 下午3:32:06
 * 
 */
public class CommonItemWriter<T> implements StepExecutionListener, ItemWriter<T> {

    private String mappingId;

    private StepExecution stepExecution;

    public CommonItemWriter() {
    }

    public CommonItemWriter(String mappingId) {
        this.mappingId = mappingId;
    }

    @Autowired
    private DataGatherDetailMapper dataGatherDetailMapper;

    @Override
    public void write(List<? extends T> items) throws Exception {
        if (items != null && items.size() > 0) {
            mappingId = stepExecution.getJobParameters().getString("mappingId");
            //System.out.println("---writer: "+mappingId);
            String tableName = dataGatherDetailMapper.getTableName(mappingId);
            List<Map<String, Object>> list = (List<Map<String, Object>>) items;
            try {
                dataGatherDetailMapper.insertTargetTable2(tableName, list);
            } catch (Exception e) {
                System.out.println("*************begin");
                System.out.println("*************tableName: " + tableName);
                System.out.println("************* " + list);
                System.out.println("************* " + e);
                System.out.println("*************end");
            }
//        for (Map<String, Object> map : list) {
//            System.out.println(tableName);
//            System.out.println(map);
//            dataGatherDetailMapper.insertTargetTable(tableName, map);
//        }
        }
    }

    /**
     * @param stepExecution
     *
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        // TODO Auto-generated method stub
        this.stepExecution = stepExecution;
    }

    /**
     * @param stepExecution
     * @return
     *
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // TODO Auto-generated method stub
        return null;
    }

}
