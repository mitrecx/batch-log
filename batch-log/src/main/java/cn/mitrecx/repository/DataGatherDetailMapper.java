package cn.mitrecx.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.mitrecx.domain.entity.FileMappingDetailsEntity;
import cn.mitrecx.domain.entity.FileMappingEntity;

/**
 * @author cx
 * @time 2019年7月18日, 下午4:35:21
 * 
 */
@Mapper
public interface DataGatherDetailMapper {
    /**
     * 插入 数据读取结果详情表T_DATA_GATHER_DETAIL
     * 
     * @param paramMap
     * @return
     */
    int insertDataGatherDetailMapper(Map<String, Object> paramMap);

    /**
     * 获取文件编码
     */
    @Select("select encoding_type from test_file_mapping where mapping_id=#{mappingId}")
    String getEncoding(String mappingId);

    /**
     * 获取文件 解析规则
     */
    @Select("select parse_rule from test_file_mapping where mapping_id=#{mappingId}")
    String getParseRule(String mappingId);

    /**
     * 获取 文件列与表字段 映射配置
     */
    List<FileMappingDetailsEntity> getFileMappingDetails(@Param("mappingId") String mappingId);
    
    /**
     * 文件与表 映射配置 
     */
    FileMappingEntity getFileMapping(@Param("mappingId") String mappingId);

    /**
     * 获取 文件与表 映射配置--头尾标识
     */
    Map<String, String> getFileMappingHeaderTailer(String mappingId);

    /**
     * 获取 (中间表)表名
     */
    @Select("select mapping_table_name from test_file_mapping where mapping_id=#{mapping_id}")
    String getTableName(String mappingId);

    /**
     * 获取 (中间表)表数据总数
     */
    @Select("select count(1) from ${tableName}")
    int getCount(@Param("tableName") String tableName);

    /**
     * 获取文件类型 <br>
     * 1-DBF <br>
     * 2-txt/csv/tsv <br>
     * 3-xml <br>
     * 4-xls/xlsx
     */
    @Select("select file_type from test_file_mapping where mapping_id = #{mappingId} ")
    String getFileType(@Param("mappingId") String mappingId);

    /**
     * 文件数据 插入中间表--TargetTable
     */
    int insertTargetTable(@Param("tableName1") String tableName, @Param("mapField") Map<String, Object> map);

    /**
     * 文件数据 插入中间表--批量插入
     */
    int insertTargetTable2(@Param("tableName1") String tableName, @Param("list") List<Map<String, Object>> list);

    /**
     * 开始插入中间表 之前, 先删除同文件同时间导入的数据(防止插入重复数据)
     */
    int deleteTargetTable(@Param("tableName") String tableName);
    
    /**
     * 获取 删除字段标识 的个数
     */
    @Select("select count(1) from test_file_mapping_details where mapping_id = #{mappingId} and delete_mark='Y' ")
    int getDeleteColumnCount(@Param("mappingId") String mappingId);
   
    /**
     * 根据 test_file_mapping_details 中的配置删除 中间表数据(单字段)
     */
    int deleteTargetTableByConfig(@Param("tableName") String tableName, @Param("tableDeleteMap") Map<String, Set<String>> tableDeleteMap);
    
    /**
     * 根据 test_file_mapping_details 中的配置删除 中间表数据(多字段)
     */
    int deleteTargetTableByConfigMulti(@Param("tableName") String tableName, @Param("list") List<Map<String, String>> list);
    
}
