package cn.mitrecx.reader;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * @author cx
 * @time 2019年7月23日, 上午11:17:53
 * 
 */
public class XMLItemReader<T> implements ItemReader<T> {
    private Iterator iterator;
 // 行号
    private AtomicInteger lineCount = new AtomicInteger(1);
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Element parent=null;
        Map<String,String> readResult=null;
        // 遍历各个 Security 元素
        if (iterator.hasNext()) {
            readResult=new HashMap<String,String>();
            parent = (Element) iterator.next();
            // 获取 Security 元素 下的 NODE
            Iterator<Node> it = parent.nodeIterator();
            // 遍历 各个 NODE
            int cnt = 0;
            while (it.hasNext()) {
                Node node = it.next();
                if (node instanceof Element) {
                    Element ee = (Element) node;
                    readResult.put("column"+cnt, ee.getText().trim());
                    //System.out.println("column" + cnt + ": " + ee.getText());
                    cnt++;
                }
            }
            readResult.put("rowNum", lineCount.getAndIncrement()+"");
            return (T)readResult;
        }
        return null;
    }

    
    
    
    public Iterator getIterator() {
        return iterator;
    }




    public void setIterator(Iterator iterator) {
        this.iterator = iterator;
    }




    public static void main(String[] args) {
        String xmlPath = "C:\\Users\\cx141\\Desktop\\20180330数据文件\\港股通\\cx\\cashsecurityclosemd_20180330.xml";
        File file = new File(xmlPath);

        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(file);
            Element root = document.getRootElement();
            Iterator iterator = root.elementIterator("Security");
            Element foo;
            // 遍历各个 Security 元素
            while (iterator.hasNext()) {
                foo = (Element) iterator.next();
                // 获取 Security 元素 下的 NODE
                Iterator<Node> it = foo.nodeIterator();
                // 遍历 各个 NODE
                int cnt = 1;
                while (it.hasNext()) {
                    Node node = it.next();
                    if (node instanceof Element) {
                        Element ee = (Element) node;
                        System.out.println("column" + cnt + ": " + ee.getText());
                        cnt++;
                    }
                }

//                System.out.println("----------");
//                System.out.println("SecurityID:"+foo.elementText("SecurityID"));
//                System.out.println("SecurityIDSource:"+foo.elementText("SecurityIDSource"));
//                System.out.println("Symbol:"+foo.elementText("Symbol"));
//                System.out.println("EnglishName:"+foo.elementText("EnglishName"));
//                System.out.println("SecurityType:"+foo.elementText("SecurityType"));
//                System.out.println("PrevClosePx:"+foo.elementText("PrevClosePx"));
//                System.out.println("==============================\n");
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

}
