package cn.mitrecx.processor;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.ItemProcessor;

import cn.mitrecx.domain.DictEntity;

public class BondTradeItemProcessor implements ItemProcessor<String, Map<String, String>> {
    // 交易方向-逆回购
    final static String BACK_SIDE = "1";
    // 交易方向-正回购
    final static String FORWARD_SIDE = "4";
    // private static final Logger log = LoggerFactory.getLogger(OriginLogtemProcessor.class);
    // 字典映射
    private List<DictEntity> bondDicts;
    // 业务日期
    private String date;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BondTradeItemProcessor() {
    };

    public BondTradeItemProcessor(List<DictEntity> bondDicts, String date) {
        this.bondDicts = bondDicts;
        this.date = date;
    }

    @Override
    public Map<String, String> process(final String lineInfo) throws Exception {
        Map<String, String> map = generateMap(lineInfo, bondDicts);
        // 买入方/卖出方信息
        map = parseBondParties(map, lineInfo);
        // 解析 现券买卖市场 通用数据
        map = parseBondCommons(map, lineInfo);
        map.put("FDATE", map.get("75") + map.get("10318"));
        map.put("YWRQ", date);
        // 文件读取时间
        map.put("FILEDATE", sdf.format(new Date()));
        // 文件名称
        map.put("FILENAME", "xxx");
        return map;
    }

    /**
     * 根据 数据字典dicts 把 lineInfo 数据解析到 map中.<br>
     * map 的 key 来自于 数据字典dicts的key.<br>
     * 
     * @param lineInfo 行信息
     * @param dicts 数据字典
     * @return
     */
    private Map<String, String> generateMap(String lineInfo, List<DictEntity> dicts) {
        Map<String, String> map = new HashMap<String, String>();
        for (DictEntity dict : dicts) {
            String key = dict.getDictUnit();
            String re = "(?<=" + (char) 0x01 + key + "=).*?(?=" + (char) 0x01 + "|(?=$))";
            Pattern pattern = Pattern.compile(re);
            Matcher matcher = pattern.matcher(lineInfo);
            while (matcher.find()) {
                String value = matcher.group();
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 解析 现券买卖市场 买入方/卖出方 信息
     * 
     * @param map
     * @param lineInfo
     * @return
     */
    private Map<String, String> parseBondParties(Map<String, String> map, String lineInfo) {
        // 匹配买入方/卖出方 重复组
        String rePartyInfo = "448=.*?((?=448=)|$)";
        Pattern patternPartyInfo = Pattern.compile(rePartyInfo);
        Matcher matcherPartyInfo = patternPartyInfo.matcher(lineInfo);
        // 匹配买入方/卖出方 重复组 组内信息
        String rePartyInfoItem = "523=.*?" + (char) 0x01 + "803=.*?" + (char) 0x01;
        Pattern patternPartyInfoItem = Pattern.compile(rePartyInfoItem);
        // 匹配买入方/卖出方 重复组 组内信息
        String rePartyInfoItemDetail = "523=(.*?)" + (char) 0x01 + "803=(.*?)" + (char) 0x01;
        Pattern patternPartyInfoItemDetail = Pattern.compile(rePartyInfoItemDetail);

        // 席位
        String reSeat = ".*(448=)(.*?)" + (char) 0x01 + "(452=)(.*?)" + (char) 0x01 + ".*";
        Pattern patternSeat = Pattern.compile(reSeat);

        // 匹配买入方/卖出方
        while (matcherPartyInfo.find()) {
            String partyInfo = matcherPartyInfo.group();

            // 买入方
            if (partyInfo.indexOf("452=119") > 0) {
                Matcher matcherSeat = patternSeat.matcher(partyInfo);
                if (matcherSeat.find()) {
                    // 买入方席位
                    map.put("FSID", matcherSeat.group(2));
                }

                Matcher matcherPartyInfoItem = patternPartyInfoItem.matcher(partyInfo);
                while (matcherPartyInfoItem.find()) {
                    // 买入方 信息
                    String partyInfoItem = matcherPartyInfoItem.group();
                    Matcher matcherPartyInfoItemDetail = patternPartyInfoItemDetail.matcher(partyInfoItem);
                    if (matcherPartyInfoItemDetail.find()) {
                        if (matcherPartyInfoItemDetail.group(2).equals("110")) {
                            // 买入方资金开户行
                            map.put("FSKHH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("23")) {
                            // 买入方资金账户名称
                            map.put("FSZHM", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("15")) {
                            // 买入方资金账号
                            map.put("FSZH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("112")) {
                            // 买入方资金开户行关联行号
                            map.put("FSKHHHH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("10")) {
                            // 买入方托管账号
                            map.put("FSZQZH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("22")) {
                            // 买入方托管账户户名
                            map.put("FSTGZH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("111")) {
                            // 买入方托管机构
                            map.put("FSTGH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("29")) {
                            // TODO 买入交易员名称
                            map.put("FSJYYNAME", matcherPartyInfoItemDetail.group(1));
                        }
                    }
                }
            }
            // 卖出方
            if (partyInfo.indexOf("452=120") > 0) {
                Matcher matcherSeat = patternSeat.matcher(partyInfo);
                if (matcherSeat.find()) {
                    // 卖出方席位
                    map.put("FBID", matcherSeat.group(2));
                }

                Matcher matcherPartyInfoItem = patternPartyInfoItem.matcher(partyInfo);
                while (matcherPartyInfoItem.find()) {
                    // 卖出方 信息
                    String partyInfoItem = matcherPartyInfoItem.group();
                    Matcher matcherPartyInfoItemDetail = patternPartyInfoItemDetail.matcher(partyInfoItem);
                    if (matcherPartyInfoItemDetail.find()) {
                        if (matcherPartyInfoItemDetail.group(2).equals("110")) {
                            // 卖出方资金开户行
                            map.put("FBKHH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("23")) {
                            // 卖出方资金账户名称
                            map.put("FBZHM", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("15")) {
                            // 卖出方资金账号
                            map.put("FBZH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("112")) {
                            // 卖出方资金开户行关联行号
                            map.put("FBKHHHH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("10")) {
                            // 卖出方托管账号
                            map.put("FBZQZH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("22")) {
                            // 卖出方托管账户户名
                            map.put("FBTGZH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("111")) {
                            // 卖出方托管机构
                            map.put("FBTGH", matcherPartyInfoItemDetail.group(1));
                        } else if (matcherPartyInfoItemDetail.group(2).equals("29")) {
                            // TODO 卖出交易员名称
                            map.put("FBJYYNAME", matcherPartyInfoItemDetail.group(1));
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * 解析 现券买卖市场 通用数据
     * 
     * @param map
     * @param lineInfo
     * @return
     */
    private Map<String, String> parseBondCommons(Map<String, String> map, String lineInfo) {
        // 到期收益率
        String reYieldRate = ".*233=Yield2" + (char) 0x01 + "234=(.*?)" + (char) 0x01 + ".*";
        Pattern patternYieldRate = Pattern.compile(reYieldRate);
        Matcher matcherYieldRate = patternYieldRate.matcher(lineInfo);
        if (matcherYieldRate.find()) {
            map.put("yieldRate", matcherYieldRate.group(1));
        }

        // 资产代码(产品代码)-- 通过 本方资金账号 获得
        // 交易方向(54)为 买入(1)
        if ("1".equals(map.get("54"))) {
            map.put("FBS", "B");
            // TODO 资产代码
            map.put("FJJDM", map.get("FSZH"));
            // TODO 资产名称
            map.put("FJJMC", map.get("FSZH"));
        } else if ("4".equals(map.get("54"))) {
            map.put("FBS", "S");
            // TODO 资产代码
            map.put("FJJDM", map.get("FBZH"));
            // TODO 资产名称
            map.put("FJJMC", map.get("FBZH"));
        }

        // 数量FSL
        String totalFaceValue = map.get("32"); // 券面总额
        BigDecimal bigTotalFaceValue = new BigDecimal(totalFaceValue);
        BigDecimal principal = new BigDecimal("100");// 每百元本金额
        if (map.get("10239") == null) {
            map.put("FSL", bigTotalFaceValue.divide(principal, 2, BigDecimal.ROUND_HALF_UP).toString());
        } else {
            principal = new BigDecimal(map.get("10239"));
            map.put("FSL", bigTotalFaceValue.divide(principal, 2, BigDecimal.ROUND_HALF_UP).toString());
        }
        return map;
    }

}
