package cn.mitrecx.writer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.mitrecx.repository.ParseIMIXMapper;

/**
 * @author cx
 * @time 2019年7月10日, 上午11:35:39
 * 
 */
public class BuyBackItemWriter<T> implements ItemWriter<T> {
//    private static ExecutorService executorService = Executors.newCachedThreadPool();
//    private final static int EXECUTE_BATCH = 200;
    // 闭锁--用于检查线程池有多少执行的线程
//    public static CountDownLatch countDownLatch;
    @Autowired
    ParseIMIXMapper parseIMIXMapper;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void write(final List<? extends T> items) throws Exception {

        if (!items.isEmpty()) {
//            int size = items.size();
//            /*
//             * int threadCnt = size / EXECUTE_BATCH + (size % EXECUTE_BATCH > 0 ? 1 : 0); countDownLatch = new
//             * CountDownLatch(threadCnt);
//             */
//            int start = 0;
//            int end = EXECUTE_BATCH;
//            if (size < EXECUTE_BATCH) {
//                end = size;
//            }
//            while (end <= size && start < end) {
//                List<Map<String, String>> listBuyback = (List<Map<String, String>>) items.subList(start, end);
//                // 新建线程执行 插入回购表 操作
//                BuybackThread buybackThread = new BuybackThread(listBuyback, parseIMIXMapper);
//                executorService.execute(buybackThread);
//
//                // listBuyback = new LinkedList<Map<String, String>>();
//                start += EXECUTE_BATCH;
//                end += EXECUTE_BATCH;
//                if (end > size) {
//                    end = size;
//                }
//                /*
//                 * if(end>=400) { // 只能是英文异常信息, 否则无法正常结束job throw new
//                 * Exception("cx exception");//RuntimeException(); }
//                 */
//
//            }
            parseIMIXMapper.insertIntoBuyback((List<Map<String, String>>) items);
            System.out.println("插入回购表 " + parseIMIXMapper.getCountBuyBack("2019-01-31") + " 条数据");
        }
    }


}

class BuybackThread extends Thread {
    private final Logger log = LoggerFactory.getLogger(BuybackThread.class);
    private List<Map<String, String>> listBuyback;
    private ParseIMIXMapper parseIMIXMapper;

    public BuybackThread(List<Map<String, String>> listBuyback, ParseIMIXMapper parseIMIXMapper) {
        this.listBuyback = listBuyback;
        this.parseIMIXMapper = parseIMIXMapper;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + "正在执行… …");
        parseIMIXMapper.insertIntoBuyback(listBuyback);
//        BuyBackItemWriter.countDownLatch.countDown();
    }
}
