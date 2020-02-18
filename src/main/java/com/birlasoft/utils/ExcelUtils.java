package com.birlasoft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
	static Logger LOGGER = Logger.getLogger(ExcelUtils.class);

	public static boolean createExcel(String excelPath, String... sheetNames) throws IOException {
		File excelFile = new File(excelPath);
		FileOutputStream fos = null;
		FileInputStream fis = null;

		if ((excelFile.getParentFile() != null) && (!excelFile.getParentFile().exists())) {
			excelFile.getParentFile().mkdirs();
		}

		fis = (excelFile.exists()) ? new FileInputStream(excelFile) : null;
		Workbook excelBook;

		if (excelFile.getName().toLowerCase().endsWith(".xlsx")) {
			excelBook = (excelFile.exists()) ? new XSSFWorkbook(fis) : new XSSFWorkbook();
		} else {
			excelBook = (excelFile.exists()) ? new HSSFWorkbook(fis) : new HSSFWorkbook();
		}

		if (fis != null)
			fis.close();

		if (sheetNames != null && sheetNames.length >= 1) {
			for (String sheet : sheetNames) {
				if (excelFile.exists()) {
					if (!isWorkbookContainSheet(excelBook, sheet)) {
						excelBook.createSheet(sheet);
					}
				} else {
					excelBook.createSheet(sheet);
				}
			}
		} else {
			excelBook.createSheet("Sheet1");
		}

		fos = new FileOutputStream(excelFile);
		excelBook.write(fos);
		excelBook.close();
		fos.close();
		return true;
	}

	private static boolean isWorkbookContainSheet(Workbook workBook, String sheetName) {
		return workBook.getSheetIndex(sheetName) != -1;
	}

	public static boolean createSheetsInExcel(String excelPath, String... sheetNames) throws IOException {
		return (sheetNames != null && sheetNames.length >= 1) ? createExcel(excelPath, sheetNames) : false;
	}

	public static Object getCellValue(String excelPath, String sheetName, int rowPosition, int colPosition)
			throws IOException {
		Object value = null;
		Workbook excelBook = getExcelWorkbook(excelPath);
		Sheet sheet = excelBook.getSheet(sheetName);

		Cell cell;

		try {
			cell = sheet.getRow(rowPosition).getCell(colPosition, MissingCellPolicy.CREATE_NULL_AS_BLANK);
		} catch (NullPointerException e) {
			LOGGER.error("Invalid row " + rowPosition + " and column " + colPosition, e);
			excelBook.close();
			return null;
		}

		value = getCellValue(cell);
		excelBook.close();

		return value;
	}

	public static Object getCellValue(String excelPath, String sheetName, String columnName, String filterCondition,
			boolean... strictCompareFlag) throws IOException {
		boolean strictCompare = (strictCompareFlag != null && strictCompareFlag.length >= 1) ? strictCompareFlag[0]
				: false;

		Workbook workBook = getExcelWorkbook(excelPath);
		Sheet sheet = workBook.getSheet(sheetName);

		Object value = getCellValue(excelPath, sheetName, getRowIndex(sheet, filterCondition, strictCompare),
				getColumnIndex(sheet, columnName));
		workBook.close();
		return value;
	}

	private static int getColumnIndex(Sheet sheet, Object columnName) {
		Row row = sheet.getRow(0);
		for (Cell cell : row) {
			if (columnName.equals(getCellValue(cell))) {
				return cell.getColumnIndex();
			}
		}
		return -1;
	}

	public static int getColumnIndex(String excelPath, String sheetName, Object columnName) throws IOException {
		File excelFile = new File(excelPath);
		Workbook excelBook;
		Sheet sheet;

		if (!excelFile.exists()) {
			return -1;
		}

		FileInputStream fis = new FileInputStream(excelPath);

		if (excelFile.getName().toLowerCase().endsWith(".xlsx")) {
			excelBook = new XSSFWorkbook(fis);
		} else {
			excelBook = new HSSFWorkbook(fis);
		}

		fis.close();

		sheet = excelBook.getSheet(sheetName);

		int colIndex = getColumnIndex(sheet, columnName);
		excelBook.close();
		return colIndex;
	}

	private static Object getCellValue(Cell cell) {
		Object value = null;
		CellValue cellValue = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator().evaluate(cell);

		if (cellValue != null) {
			switch (cellValue.getCellTypeEnum()) {
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					value = cell.getDateCellValue();
				} else {
					value = cell.getNumericCellValue();
				}
				break;
			case BLANK:
			case _NONE:
				value = " ";
				break;
			case BOOLEAN:
				value = cell.getBooleanCellValue();
				break;
			case ERROR:
			default:
				break;
			}
		} else {
			value = " ";
		}

		return value;
	}

	public static int getRowIndex(Sheet sheet, String filterCondition, boolean strictCompare) {
		String[] conditions = filterCondition.split(Constants.CONDITION_SEPARATOR);
		LinkedHashMap<String, String> fullConditions = new LinkedHashMap<String, String>();

		for (String condition : conditions) {
			fullConditions.put(condition.split(Constants.CONDITIONVALUE_SEPARATOR)[0],
					condition.split(Constants.CONDITIONVALUE_SEPARATOR)[1]);
		}

		int[] columnIndices = new int[fullConditions.size()];
		Set<String> columnNames = fullConditions.keySet();

		for (String columnName : columnNames) {
			columnIndices = ArrayUtils.add(columnIndices, getColumnIndex(sheet, columnName));
			columnIndices = ArrayUtils.remove(columnIndices, 0);
		}

		for (Row row : sheet) {
			LinkedHashMap<String, String> newHashMap = new LinkedHashMap<>();

			for (int index : columnIndices) {
				newHashMap.put(String.valueOf(getCellValue(sheet.getRow(0).getCell(index))),
						String.valueOf(getCellValue(row.getCell(index))));
			}

			if (strictCompare) {
				if (newHashMap.equals(fullConditions))
					return row.getRowNum();
			} else {
				if (compareHashMapLoosely(newHashMap, fullConditions))
					return row.getRowNum();
			}
		}
		return -1;
	}

	private static Workbook getExcelWorkbook(String excelPath) throws IOException {
		File excelFile = new File(excelPath);

		if (!excelFile.exists())
			throw new FileNotFoundException("No such file exists at - " + excelFile.getCanonicalPath());

		Workbook workBook;
		FileInputStream fis = new FileInputStream(excelFile);

		if (excelFile.getName().toLowerCase().endsWith(".xlsx")) {
			workBook = new XSSFWorkbook(fis);
		} else {
			workBook = new HSSFWorkbook(fis);
		}

		fis.close();

		return workBook;
	}

	private static boolean compareHashMapLoosely(LinkedHashMap<String, String> one, LinkedHashMap<String, String> two) {
		Set<String> keySetOne = one.keySet();
		Set<String> keySetTwo = two.keySet();

		if (!CollectionUtils.isEqualCollection(keySetOne, keySetTwo))
			return false;
		for (String col1 : keySetOne) {
			for (String col2 : keySetTwo) {
				if (col1.equalsIgnoreCase(col2)) {
					// Numeric Logic
					if (NumberUtils.isNumber(one.get(col1)) && NumberUtils.isNumber(two.get(col2))) {
						if (Double.valueOf(one.get(col1)).doubleValue() != Double.valueOf(two.get(col2)).doubleValue())
							return false;
					}
					// Ignore case and trim
					else if (StringUtils.isAlphanumeric(one.get(col1)) && StringUtils.isAlphanumeric(two.get(col2))) {
						if (!one.get(col1).trim().equalsIgnoreCase(two.get(col2).trim())) {
							return false;
						}
					}
					// TODO - Logic to be added for dateformat comparisons
				}
			}
		}
		return true;
	}

	public static void setCellValue(String excelPath, String sheetName, int rowNum, int columnNum, Object valueToSet)
			throws IOException {
		Workbook workBook = getExcelWorkbook(excelPath);
		Sheet sheet = workBook.getSheet(sheetName);
		Cell cell = null;

		try {
			if (sheet.getRow(rowNum) == null)
				cell = sheet.createRow(rowNum).createCell(columnNum);
			else if (sheet.getRow(rowNum).getCell(columnNum) == null) {
				cell = sheet.getRow(rowNum).createCell(columnNum);
			} else {
				cell = sheet.getRow(rowNum).getCell(columnNum);
			}
		} catch (NullPointerException e) {
			LOGGER.error("Invalid row " + rowNum + " and column " + columnNum, e);
			workBook.close();
			return;
		}

		if (valueToSet != null) {
			switch (valueToSet.getClass().getSimpleName().toUpperCase()) {
			case "INTEGER":
			case "DOUBLE":
			case "FLOAT":
			case "SHORT":
			case "BYTE":
			case "LONG":
				cell.setCellType(CellType.NUMERIC);
				cell.setCellValue(Double.parseDouble(String.valueOf(valueToSet)));
				break;
			case "DATE":
				CellStyle cellStyle = workBook.createCellStyle();
				CreationHelper createHelper = workBook.getCreationHelper();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MMM/yyyy HH:mm:ss.ms"));
				cell.setCellValue((Date) valueToSet);
				cell.setCellStyle(cellStyle);
				break;
			case "BOOLEAN":
				cell.setCellValue(Boolean.parseBoolean(String.valueOf(valueToSet)));
				cell.setCellType(CellType.BOOLEAN);
				break;
			case "STRING":
			case "OBJECT":
			default:
				cell.setCellValue(String.valueOf(valueToSet));

				if (valueToSet.toString().toLowerCase().startsWith("=")) {
					cell.setCellType(CellType.FORMULA);
				} else {
					cell.setCellType(CellType.STRING);
				}
				break;
			}
		}

		FileOutputStream fos = new FileOutputStream(excelPath);
		workBook.write(fos);
		workBook.close();
		fos.close();
	}

	public static HashMap<Integer, Object> getExcelData(String excelPath, String sheetName,
			boolean... columnHeaderPresent) throws IOException {
		HashMap<Integer, Object> data = new HashMap<>();
		boolean headersPresent = (columnHeaderPresent != null && columnHeaderPresent.length >= 1)
				? (columnHeaderPresent[0]) : false;
		Workbook workBook = getExcelWorkbook(excelPath);
		Sheet sheet = workBook.getSheet(sheetName);
		ArrayList<String> columnNames = new ArrayList<>();

		for (Row row : sheet) {
			String values = "";
			for (Cell cell : row) {
				if (row.getRowNum() == 0) {
					values += String.valueOf(getCellValue(cell)) + Constants.COLUMNVALUE_SEPARATOR;

					if (headersPresent) {
						columnNames.add(String.valueOf(getCellValue(cell)));
					}
				} else {
					String columnPrefix = "";

					if (headersPresent) {
						columnPrefix = columnNames.get(cell.getColumnIndex()) + Constants.COLUMN_SEPARATOR;
					}

					values += columnPrefix + String.valueOf(getCellValue(cell)) + Constants.COLUMNVALUE_SEPARATOR;
				}
			}
			values = values.substring(0, (values.length() - Constants.COLUMNVALUE_SEPARATOR.length()));
			data.put(row.getRowNum(), values);
		}

		if (headersPresent) {
			data.remove(0);
		} else {
			HashMap<Integer, Object> newData = new HashMap<>();
			for (int x = 0; x < data.size(); x++) {
				newData.put((x + 1), data.get(x));
			}
			data = newData;
		}

		workBook.close();
		return data;
	}

	public static HashMap<Integer, Object> getExcelData(String excelPath, String sheetName, String filterConditions,
			boolean... strictCompareFlag) throws IOException {
		HashMap<Integer, Object> data = new HashMap<>();
		boolean strictCompare = (strictCompareFlag != null && strictCompareFlag.length >= 1) ? strictCompareFlag[0]
				: false;
		Workbook workbook = getExcelWorkbook(excelPath);
		Sheet sheet = workbook.getSheet(sheetName);
		int cnt = 1;
		int[] targetRowIndices = getRowIndices(sheet, filterConditions, strictCompare);

		if (targetRowIndices.length == 1 && targetRowIndices[0] == -1) {
			return data;
		} else {
			HashMap<Integer, Object> newData = getExcelData(excelPath, sheetName, false);

			for (int i : targetRowIndices) {
				data.put(cnt++, newData.get(i));
			}
		}

		workbook.close();
		return data;
	}

	public static HashMap<Integer, Object> getExcelData(String excelPath, String sheetName, String filterConditions,
			String columnNames, boolean... strictCompareFlag) throws IOException {
		HashMap<Integer, Object> data = new HashMap<>();
		boolean strictCompare = (strictCompareFlag != null && strictCompareFlag.length >= 1) ? strictCompareFlag[0]
				: false;
		Workbook workbook = getExcelWorkbook(excelPath);
		Sheet sheet = workbook.getSheet(sheetName);
		int cnt = 1;
		int[] targetRowIndices = getRowIndices(sheet, filterConditions, strictCompare);

		if (targetRowIndices.length == 1 && targetRowIndices[0] == -1) {
			return data;
		} else {
			HashMap<Integer, Object> newData = getExcelColumnsData(excelPath, sheetName, columnNames);

			for (int i : targetRowIndices) {
				data.put(cnt++, newData.get(i));
			}
		}
		workbook.close();
		return data;
	}

	public static List<HashMap<Integer, Object>> getExcelData(String excelPath) throws IOException {
		List<HashMap<Integer, Object>> data = new ArrayList<HashMap<Integer, Object>>();
		Workbook workBook = getExcelWorkbook(excelPath);

		for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
			data.add(getExcelData(excelPath, workBook.getSheetAt(i).getSheetName()));
		}

		workBook.close();
		return data;
	}

	public static HashMap<Integer, Object> getExcelColumnsData(String excelPath, String sheetName, String columnNames)
			throws IOException {
		HashMap<Integer, Object> data = new HashMap<>();
		Workbook workBook = getExcelWorkbook(excelPath);
		Sheet sheet = workBook.getSheet(sheetName);

		String[] arrColumns = columnNames.split(Constants.CONDITION_SEPARATOR);

		for (Row row : sheet) {
			String values = "";

			for (String string : arrColumns) {
				int colIndex = getColumnIndex(sheet, string);
				Cell cell = row.getCell(colIndex);

				if (row.getRowNum() != 0) {
					values += String.valueOf(getCellValue(cell)) + Constants.COLUMNVALUE_SEPARATOR;
				}
			}
			values = values.substring(0, (values.length() - Constants.COLUMNVALUE_SEPARATOR.length()));
			data.put(row.getRowNum(), values);
		}

		workBook.close();
		return data;
	}

	private static int[] getRowIndices(Sheet sheet, String filterCondition, boolean strictCompare) {
		ArrayList<Integer> list = new ArrayList<>();
		String[] conditions = filterCondition.split(Constants.CONDITION_SEPARATOR);
		LinkedHashMap<String, String> fullConditions = new LinkedHashMap<String, String>();

		for (String condition : conditions) {
			fullConditions.put(condition.split(Constants.CONDITIONVALUE_SEPARATOR)[0],
					condition.split(Constants.CONDITIONVALUE_SEPARATOR)[1]);
		}

		int[] columnIndices = new int[fullConditions.size()];
		Set<String> columnNames = fullConditions.keySet();

		for (String columnName : columnNames) {
			columnIndices = ArrayUtils.add(columnIndices, getColumnIndex(sheet, columnName));
			columnIndices = ArrayUtils.remove(columnIndices, 0);
		}

		for (Row row : sheet) {
			LinkedHashMap<String, String> newHashMap = new LinkedHashMap<>();

			for (int index : columnIndices) {
				newHashMap.put(String.valueOf(getCellValue(sheet.getRow(0).getCell(index))),
						String.valueOf(getCellValue(row.getCell(index))));
			}

			if (strictCompare) {
				if (newHashMap.equals(fullConditions))
					list.add(row.getRowNum());
			} else {
				if (compareHashMapLoosely(newHashMap, fullConditions))
					list.add(row.getRowNum());
			}
		}

		if (list.size() == 0)
			list.add(-1);

		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}

	public static ArrayList<Object> getEntireColumnData(String excelPath, String sheetName, int columnIndex)
			throws IOException {
		ArrayList<Object> data = new ArrayList<>();
		Sheet sheet = getSheetOfWorkbook(excelPath, sheetName);
		for (Row row : sheet) {
			data.add(getCellValue(row.getCell(columnIndex)));
		}
		return data;
	}

	public static ArrayList<Object> getEntireColumnData(String excelPath, String sheetName, String columnName)
			throws IOException {
		ArrayList<Object> data = new ArrayList<>();
		data.addAll(getEntireColumnData(excelPath, sheetName,
				getColumnIndex(getSheetOfWorkbook(excelPath, sheetName), columnName)));
		data.remove(0);
		return data;
	}

	private static Sheet getSheetOfWorkbook(String excelPath, String sheetName) throws IOException {
		return getExcelWorkbook(excelPath).getSheet(sheetName);
	}

	private static Connection getConnection(String driver, String connString)
			throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		return DriverManager.getConnection(connString);
	}

	public static HashMap<Integer, Object> queryExcel(String excelPath, String query, boolean... includeColumnName)
			throws ClassNotFoundException, SQLException {
		String connString = "jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};DBQ=" + excelPath;
		boolean blnColInclude = (includeColumnName != null && includeColumnName.length >= 1) ? includeColumnName[0]
				: false;

		Connection conn = getConnection("sun.jdbc.odbc.JdbcOdbcDriver", connString);
		Statement statement = null;
		ResultSet result = null;

		HashMap<Integer, Object> dataSet = new HashMap<>();
		int rowCounter = 1;

		try {
			statement = conn.createStatement();
			result = statement.executeQuery(query);

			while (result.next()) {
				int columnsCount = result.getMetaData().getColumnCount();
				String data = "";

				for (int i = 1; i <= columnsCount; i++) {
					String cellValue = String.valueOf(result.getObject(i));
					String colPrefix = "";

					if (blnColInclude) {
						colPrefix = result.getMetaData().getColumnName(i) + Constants.COLUMN_SEPARATOR;
					}

					if (cellValue == null) {
						data += colPrefix + " " + Constants.COLUMNVALUE_SEPARATOR;
					} else {
						data += colPrefix + cellValue + Constants.COLUMNVALUE_SEPARATOR;
					}
				}
				data = data.substring(0, data.length() - Constants.COLUMNVALUE_SEPARATOR.length());
				dataSet.put(rowCounter++, data);
			}
		} finally {
			result.close();
			statement.close();
			conn.close();
		}
		return dataSet;
	}

	public static String createExcelQuery(String sheetName, String queryColumns, String filter) {
		return "Select " + queryColumns + " from [" + sheetName + "$] where " + filter;
	}

	public static String createExcelQuery(String sheetName, String queryColumns) {
		return "Select " + queryColumns + " from [" + sheetName + "$]";
	}
}