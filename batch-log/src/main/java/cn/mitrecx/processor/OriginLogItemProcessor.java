package cn.mitrecx.processor;

import org.springframework.batch.item.ItemProcessor;

public class OriginLogItemProcessor implements ItemProcessor<String, String> {

    //private static final Logger log = LoggerFactory.getLogger(OriginLogtemProcessor.class);

    @Override
    public String process(final String line) throws Exception {
        //log.info("原始数据不做 处理, 直接分类插入 子log文件中.");
        //System.out.println("原始数据不做 处理, 直接分类插入 子log文件中.");
        return line;
    }


}
