package cn.mitrecx.processor;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cn.mitrecx.domain.DictEntity;

public class BuyBackItemProcessor implements ItemProcessor<String, Map<String, String>> {
    
    // 交易方向-逆回购
    final static String BACK_SIDE = "1";
    // 交易方向-正回购
    final static String FORWARD_SIDE = "4";
    // private static final Logger log = LoggerFactory.getLogger(OriginLogtemProcessor.class);
    // 字典映射
    private List<DictEntity> buybackDicts;
    // 业务日期
    private String date;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BuyBackItemProcessor() {
    };

    public BuyBackItemProcessor(List<DictEntity> buybackDicts, String date) {
        this.buybackDicts = buybackDicts;
        this.date = date;
    }

    @Override
    public Map<String, String> process(final String lineInfo) throws Exception {
        Map<String, String> map = generateMap(lineInfo, buybackDicts);
        // 质押式/买断式回购 信息转换
        map = parseBuybackConvert(map);
        // 解析 质押式/买断式 回购 中 正回购和逆回购 信息
        map = parseBuyback(map, lineInfo);
        // 解析 质押式/买断式 回购 中 对手方信息 和 资产代码
        map = parseBuybackCounter(map, lineInfo);

        map.put("YWRQ", date);

        // 文件读取时间
        map.put("FILEDATE", sdf.format(new Date()));
        // 文件名称 TODO
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
     * 质押式/买断式回购 信息转换(不包含重复组)
     * 
     * @param map
     * @return
     */
    private Map<String, String> parseBuybackConvert(Map<String, String> map) {
        // 交易方向
        if ("4".equals(map.get("54"))) {
            map.put("54", "S");
        } else if ("1".equals(map.get("54"))) {
            map.put("54", "B");
        }

        // 市场标识(10176)=质押式回购(9), 买断式回购(10)
        if ("9".equals(map.get("10176"))) {
            // 回购代码
            String tempFZQDM = "000" + map.get("10316");
            map.put("FZQDM", "Y02" + tempFZQDM.substring(tempFZQDM.length() - 3, tempFZQDM.length()));
            // 回购类型 FHGLX
            map.put("FHGLX", "0");
            // 是否买断回购 FMDHG
            map.put("FMDHG", "0");
            // 交易金额 FJYJE
            map.put("FJYJE", map.get("10312"));
            // 应计利息 FYJLX
            map.put("FYJLX", map.get("10002"));
            // 结算费
            if (map.get("711") == null) {
                map.put("FSXF", "");
            } else if (map.get("711").trim().equals("1")) {
                map.put("FSXF", "120");
            } else {
                map.put("FSXF", "200");
            }
        } else if ("10".equals(map.get("10176"))) {
            // 回购代码
            String tempFZQDM = "000" + map.get("10316");
            map.put("FZQDM", "Y05" + tempFZQDM.substring(tempFZQDM.length() - 3, tempFZQDM.length()));
            // 回购类型 FHGLX
            map.put("FHGLX", "1");
            // 是否买断回购 FMDHG
            map.put("FMDHG", "1");
            // 交易金额 FJYJE
            map.put("FJYJE", map.get("119"));
            // 应计利息 FYJLX
            map.put("FYJLX", new BigDecimal(map.get("10289")).subtract(new BigDecimal(map.get("119"))).toString());
            // 结算费
            map.put("FSXF", "200");
        }

        // 交收状态
        if ("F".equals(map.get("150"))) {
            map.put("150", "1");
        }

        // 首次结算方式
        if ("0".equals(map.get("919"))) {
            map.put("919", "券款对付");
        } else if ("4".equals(map.get("919"))) {
            map.put("919", "见券付款");
        } else if ("5".equals(map.get("919"))) {
            map.put("919", "见款付券");
        }

        // 到期结算方式
        if ("0".equals(map.get("10045"))) {
            map.put("10045", "券款对付");
        } else if ("4".equals(map.get("10045"))) {
            map.put("10045", "见券付款");
        } else if ("5".equals(map.get("10045"))) {
            map.put("10045", "见款付券");
        }
        return map;
    }

    /**
     * 解析 质押式/买断式 回购 中 正回购和逆回购 信息<br>
     * 
     * @param map 日志字段映射
     * @param lineInfo 日志行信息
     * @return
     */
    private Map<String, String> parseBuyback(Map<String, String> map, String lineInfo) {
        // 正回购和逆回购 重复组匹配
        String reBuyBack = "448=.*?((?=448=.*?" + (char) 0x01 + ")|$)";
        Pattern patternBuyBack = Pattern.compile(reBuyBack);
        Matcher matcherBuyBack = patternBuyBack.matcher(lineInfo);
        // 正回购或逆回购 组内匹配item
        String reBuyBackItem = "523=.*?" + (char) 0x01 + "803=.*?" + (char) 0x01;
        Pattern patternBuyBackItem = Pattern.compile(reBuyBackItem);
        // item 分组匹配
        String reBuyBackDetail = "523=(.*?)" + (char) 0x01 + "803=(.*?)" + (char) 0x01;
        Pattern patternBuyBackDetail = Pattern.compile(reBuyBackDetail);
        while (matcherBuyBack.find()) {
            String buyBackItem = matcherBuyBack.group();
            Matcher matcherBuyBackItem = patternBuyBackItem.matcher(buyBackItem);

            // 席位
            String reSeat = ".*(448=)(.*?)" + (char) 0x01 + "(452=)(.*?)" + (char) 0x01 + ".*";
            Pattern patternSeat = Pattern.compile(reSeat);
            Matcher matcherSeat = patternSeat.matcher(buyBackItem);

            // 如果 重复组是 正回购的重复组
            if (buyBackItem.indexOf("452=120") > 0) {
                if (matcherSeat.find()) {
                    // 正回购席位
                    map.put("FSID", matcherSeat.group(2));
                }
                while (matcherBuyBackItem.find()) {
                    String partySub = matcherBuyBackItem.group();
                    // System.out.println(partySub);
                    Matcher matcherBuyBackDetail = patternBuyBackDetail.matcher(partySub);
                    if (matcherBuyBackDetail.find()) {
                        // System.out.println(matcherBuyBackDetail.group(2));
                        if (matcherBuyBackDetail.group(2).equals("110")) {
                            // 资金开户行 FSKHH
                            map.put("FSKHH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("23")) {
                            // 资金账户户名FSZHM
                            map.put("FSZHM", matcherBuyBackDetail.group(1));
                            if (FORWARD_SIDE.equals(map.get("54")) || "S".equals(map.get("54"))) {
                                map.put("fundName", matcherBuyBackDetail.group(1));
                            }
                        } else if (matcherBuyBackDetail.group(2).equals("15")) {
                            // 资金账号FSZH
                            map.put("FSZH", matcherBuyBackDetail.group(1));
                            if (FORWARD_SIDE.equals(map.get("54")) || "S".equals(map.get("54"))) {
                                map.put("fundID", matcherBuyBackDetail.group(1)); // 资产代码
                            }
                        } else if (matcherBuyBackDetail.group(2).equals("112")) {
                            // 资金开户行关联行号FSKHHHH
                            map.put("FSKHHHH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("10")) {
                            // 托管账号FSZQZH
                            map.put("FSZQZH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("22")) {
                            // 托管账户户名FSTGZH
                            map.put("FSTGZH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("111")) {
                            // 托管机构FSTGH
                            map.put("FSTGH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("101")) {
                            // 交易员名称FSJYYNAME
                            map.put("FSJYYNAME", matcherBuyBackDetail.group(1));
                        }
                    }
                }
            }
            // 如果 重复组是 逆回购的重复组
            if (buyBackItem.indexOf("452=119") > 0) {
                if (matcherSeat.find()) {
                    // 逆回购席位 FBID
                    map.put("FBID", matcherSeat.group(2));
                }
                while (matcherBuyBackItem.find()) {
                    String partySub = matcherBuyBackItem.group();
                    Matcher matcherBuyBackDetail = patternBuyBackDetail.matcher(partySub);
                    if (matcherBuyBackDetail.find()) {
                        if (matcherBuyBackDetail.group(2).equals("110")) {
                            // 资金开户行 FBKHH
                            map.put("FBKHH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("23")) {
                            // 资金账户户名FBZHM
                            map.put("FBZHM", matcherBuyBackDetail.group(1));
                            // (交易方向54 为 )逆回购1
                            if (BACK_SIDE.equals(map.get("54")) || "B".equals(map.get("54"))) {
                                map.put("fundName", matcherBuyBackDetail.group(1));
                            }
                        } else if (matcherBuyBackDetail.group(2).equals("15")) {
                            // 资金账号FBZH
                            map.put("FBZH", matcherBuyBackDetail.group(1));
                            // (交易方向54 为 )逆回购1
                            if (BACK_SIDE.equals(map.get("54")) || "B".equals(map.get("54"))) {
                                map.put("fundID", matcherBuyBackDetail.group(1)); // 资产代码
                            }
                        } else if (matcherBuyBackDetail.group(2).equals("112")) {
                            // 资金开户行关联行号FBKHHHH
                            map.put("FBKHHHH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("10")) {
                            // 托管账号FBZQZH
                            map.put("FBZQZH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("22")) {
                            // 托管账户户名FBTGZH
                            map.put("FBTGZH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("111")) {
                            // 托管机构FBTGH
                            map.put("FBTGH", matcherBuyBackDetail.group(1));
                        } else if (matcherBuyBackDetail.group(2).equals("101")) {
                            // 交易员名称FBJYYNAME
                            map.put("FBJYYNAME", matcherBuyBackDetail.group(1));
                        }
                    }
                }
            }

        }
        return map;
    }

    /**
     * 解析 质押式/买断式 回购 对手方信息 和 资产代码
     * 
     * @param map 日志字段映射
     * @param lineInfo 日志行信息
     * @return
     */
    private Map<String, String> parseBuybackCounter(Map<String, String> map, String lineInfo) {
        // 对手方
        String counterParty = "";
        // 资产代码(会员代码)
        // String fundID = "";
        String rePartyGroup = "(?<=448=).*?" + (char) 0x01 + "452=\\d+";
        Pattern patternPartyGroup = Pattern.compile(rePartyGroup);
        Matcher matcherPartyGroup = patternPartyGroup.matcher(lineInfo);
        // 交易方向54 为 逆回购1
        if (BACK_SIDE.equals(map.get("54"))) {
            // 对手方 为 逆回购方, 取 PartyRole452=120(正回购方) 的 PartyID448值
            while (matcherPartyGroup.find()) {
                if (matcherPartyGroup.group().indexOf("120") > 0) {
                    // 对手方
                    counterParty = matcherPartyGroup.group();
                    counterParty = counterParty.substring(0, counterParty.indexOf((char) 0x01));
                }

            }
        } else {
            // 对手方 为 正回购方, 取 PartyRole452=119(逆回购方) 的 PartyID448值
            while (matcherPartyGroup.find()) {
                if (matcherPartyGroup.group().indexOf("119") > 0) {
                    // 对手方
                    counterParty = matcherPartyGroup.group();
                    counterParty = counterParty.substring(0, counterParty.indexOf((char) 0x01));
                }

            }
        }
        map.put("448", counterParty);

        return map;
    }

}
