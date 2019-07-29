package cn.mitrecx.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import cn.com.yusys.yusp.commons.util.DateUtil;

/**
 * @author cx
 * @time 2019年7月26日, 下午4:45:38
 * 
 */
public class ExcelSheetItemReader implements ItemReader<Map<String, String>> {

    Sheet sheet;
    int rowIndex=0;
    int startLine;
    int endLine;
    @Override
    public Map<String, String> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Map<String, String> readResult = new HashMap<String, String>();
        // 没有读取完 一个sheet 的所有行
        if (rowIndex <= sheet.getLastRowNum()) { 
            if (rowIndex >= startLine && rowIndex<endLine) {// 去除头尾操作

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    return readResult;
                }
                for (int cellnum = 0; cellnum < row.getLastCellNum(); cellnum++) {
                    Cell cell = row.getCell(cellnum);
                    String value = "";
                    if (cell != null) {
                        if (cell.getCellTypeEnum() == CellType._NONE) {
                            value = "";
                        } else if (cell.getCellTypeEnum() == CellType.BLANK) {
                            value = "";
                        } else if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
                            value = "";
                        } else if (cell.getCellTypeEnum() == CellType.ERROR) {
                            value = "";
                        } else if (cell.getCellTypeEnum() == CellType.FORMULA) {
                            value = cell.getCellFormula();
                        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                value = DateUtil.format(cell.getDateCellValue(), DateUtil.PATTERN_DATE2);
                            } else {
                                value = Double.toString(cell.getNumericCellValue());
                            }
                        } else if (cell.getCellTypeEnum() == CellType.STRING) {
                            value = cell.getStringCellValue();
                        }
                    }
                    if (value == null) {
                        readResult.put("column" + cellnum, "");
                    } else {
                        readResult.put("column" + cellnum, value.trim());
                    }
                }
                rowIndex++;
                readResult.put("rowNum", rowIndex+"");
                return readResult;
            } else {
                rowIndex++;
                return readResult;
            }
        }
        return null;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

}
