package cn.mitrecx.writer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cn.mitrecx.repository.ParseIMIXMapper;

/**
 * @author cx
 * @time 2019年7月10日, 上午11:35:39
 * 
 */
public class BondTradeItemWriter<T> implements ItemWriter<T> {
//    private static ExecutorService executorService = Executors.newCachedThreadPool();
//    // 闭锁--用于检查线程池有多少执行的线程
////    public static CountDownLatch countDownLatch;
//    private final static int EXECUTE_BATCH = 200;
    @Autowired
    ParseIMIXMapper parseIMIXMapper;

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
//                // subList 截取区间 含头不含尾
//                List<Map<String, String>> listBondTrade = (List<Map<String, String>>) items.subList(start, end);
//                // 新建线程执行 插入现券买卖表 操作
//                BondThread bondThread = new BondThread(listBondTrade, parseIMIXMapper);
//                executorService.execute(bondThread);
//                start += EXECUTE_BATCH;
//                end += EXECUTE_BATCH;
//                if (end > size) {
//                    end = size;
//                }
//            }
            parseIMIXMapper.insertIntoBondTrade((List<Map<String, String>>) items);
            System.out.println("插入现券买卖表 " + parseIMIXMapper.getCountBondTrade("2019-01-31") + " 条数据");
        }
    }

}

class BondThread extends Thread {
    private final Logger log = LoggerFactory.getLogger(BondThread.class);
    private List<Map<String, String>> listBond;
    private ParseIMIXMapper parseIMIXMapper;

    public BondThread(List<Map<String, String>> listBond, ParseIMIXMapper parseIMIXMapper) {
        this.listBond = listBond;
        this.parseIMIXMapper = parseIMIXMapper;
    }

    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + "正在执行… …");
        parseIMIXMapper.insertIntoBondTrade(listBond);
//        BuyBackItemWriter.countDownLatch.countDown();
    }
}
