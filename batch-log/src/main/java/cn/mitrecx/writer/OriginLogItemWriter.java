package cn.mitrecx.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

/**
 * @author cx
 * @time 2019年7月10日, 上午11:35:39
 * 
 */
public class OriginLogItemWriter<T> implements ItemWriter<T> {
    @Override
    public void write(final List<? extends T> items) throws Exception {
        
    }

  
}
