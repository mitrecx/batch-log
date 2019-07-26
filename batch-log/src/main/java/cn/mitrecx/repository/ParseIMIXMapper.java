package cn.mitrecx.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import cn.mitrecx.domain.DictEntity;

/**
 * @author cx
 * @time 2019年5月30日, 上午9:57:43
 * 
 */
@Mapper
public interface ParseIMIXMapper {
    @Select({ "select t.dict_code as \"dictCode\", t.dict_unit as \"dictUnit\", t.unit_desc as \"unitDesc\" from t_dict t where t.dict_code = #{dictCode}" })
    List<DictEntity> getDict(String dictCode);

    @Select({"select count(1) from T_VALEXT_BANK_BUYBACKTRADE where YWRQ=#{date}"})
    int getCountBuyBack(String date);
    @Select({"select count(1) from T_VALEXT_BANK_BONDTRADE where YWRQ=#{date}"})
    int getCountBondTrade(String date);
    
    @Select({"delete from T_VALEXT_BANK_BUYBACKTRADE where YWRQ=#{date}"})
    void deleteBuyback(String date);
    @Select({"delete from T_VALEXT_BANK_BONDTRADE where YWRQ=#{date}"})
    void deleteBond(String date);
    
    /**
     * 插入 银行间回购交易表时
     * 
     * @param 
     * @return
     */
    int insertIntoBuyback(List<Map<String, String>> mitreTests);

    int insertIntoBondTrade(List<Map<String, String>> mitreTests);
}
