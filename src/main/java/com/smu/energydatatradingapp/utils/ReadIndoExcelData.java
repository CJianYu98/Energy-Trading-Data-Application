package com.smu.energydatatradingapp.utils;

import com.smu.energydatatradingapp.constant.MonthIndoWordToNumTranslation;
import com.smu.energydatatradingapp.model.IndoData;
import com.smu.energydatatradingapp.service.IndoDataService;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods which read the Excel files which consists of Indonesian energy product export and
 * import data.
 * @version 1.0 12 Sep 2021
 * @author Chen Jian Yu
 */
@Getter
@Setter
public class ReadIndoExcelData {
    private final Logger LOGGER = LoggerFactory.getLogger(ReadIndoExcelData.class);

    private final ExcelUtils excelUtils = new ExcelUtils();

    // instantiate variables for data insertion purpose
    List<IndoData> indoDataList = new ArrayList<>();
    private int batchSizeToInsert = 200;
    private int batchSizeCount = 0;

    // instantiate variables to store attribute values for a record
    private int yearValue;
    private String countryValue;
    private String harborValue;
    private String productValue;
    private String productCategoryValue;
    private int monthValue;
    private int hsCodeValue;
    private double usdValue;
    private double weightInKg;
    private double weightInMetricTon;

    /**
     * This method reads the data in Indonesian data Excel files and inserts the data into database.
     * @param xlsxFiles List of Excel files to be read
     * @param indoDataService IndoDataService object for inserting data to database
     */
    public void readAndInsertIndoData(File[] xlsxFiles, IndoDataService indoDataService) {
        try {
            // Looping through all the relevant Excel files
            for (int fileNo=0; fileNo<xlsxFiles.length; fileNo+=4) {
                XSSFSheet exportValueSheet = excelUtils.getSheetByIndex(xlsxFiles[fileNo].getPath(), 0);
                XSSFSheet exportWeightSheet = excelUtils.getSheetByIndex(xlsxFiles[fileNo+1].getPath(), 0);
                XSSFSheet importValueSheet = excelUtils.getSheetByIndex(xlsxFiles[fileNo+2].getPath(), 0);
                XSSFSheet importWeightSheet = excelUtils.getSheetByIndex(xlsxFiles[fileNo+3].getPath(), 0);

                // number of columns and rows in the Indo export data Excel sheets
                int exportSheetColumns = exportValueSheet.getRow(1).getPhysicalNumberOfCells();
                int exportSheetRows = exportValueSheet.getPhysicalNumberOfRows();

                // number of columns and rows in the Indo import data Excel sheets
                int importSheetColumns = importValueSheet.getRow(1).getPhysicalNumberOfCells();
                int importSheetRows = importValueSheet.getPhysicalNumberOfRows();

                readIndoData(exportValueSheet, exportWeightSheet, exportSheetColumns, exportSheetRows, 1, indoDataService);
                readIndoData(importValueSheet, importWeightSheet, importSheetColumns, importSheetRows, 0, indoDataService);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method goes through each Indonesian data Excel file and retrieve relevant values to create an IndoData
     * record. Data will be inserted into the database in batches.
     * @param valueSheet Excel sheet that contains data in USD unit
     * @param weightSheet Excel sheet that contains data in weight(KG) unit
     * @param numColumns Number of columns in the Excel sheet
     * @param numRows Number of rows in the Excel sheet
     * @param isExport Whether the Excel sheets contains import or export data
     * @param indoDataService IndoDataService object for inserting data to database
     */
    private void readIndoData(XSSFSheet valueSheet,
                              XSSFSheet weightSheet,
                              int numColumns,
                              int numRows,
                              int isExport,
                              IndoDataService indoDataService) {
       try {
           // Iterate through all the rows starting from row index 4 and to the second last row
           for (int rowNo = 4; rowNo < numRows-1; rowNo++) {
               yearValue = readYearValue(valueSheet, rowNo);

               // Iterate through all the columns starting from column index 3 and to the second last column
               for (int colNo = 3; colNo < numColumns-1; colNo++) {
                   countryValue = readCountryValue(valueSheet, colNo);
                   harborValue = readHarborValue(valueSheet, colNo);
                   usdValue = readUsdValue(valueSheet, rowNo, colNo);
                   weightInKg = readWeightInKgValue(weightSheet, rowNo, colNo);

                   // read other corresponding values if value & weight cells are not empty
                   if ((usdValue != 0.0) && (weightInKg != 0.0)) {
                       monthValue = readMonthValue(valueSheet, colNo);
                       productValue = readProductValue(valueSheet, rowNo);
                       productCategoryValue = readProductCategoryValue(productValue);
                       hsCodeValue = readHsCodeValue(valueSheet, rowNo);
                       weightInMetricTon = weightInKg * 0.001;

                       // creating a new IndoExport object
                       IndoData indoData = new IndoData(
                               hsCodeValue, yearValue,
                               monthValue, productValue,
                               productCategoryValue, countryValue,
                               harborValue, usdValue,
                               weightInKg, weightInMetricTon, isExport);

                       // add IndoData to data batch List
                       indoDataList.add(indoData);

                       // increment batch size by 1 after adding data record into indoExportList
                       batchSizeCount++;

                       // if batch size reaches limit, insert data to database
                       if (batchSizeCount == batchSizeToInsert) {insertData(indoDataService);}
                   }
               }
           }
           // insert remaining data to database
           if (batchSizeCount > 0) {insertData(indoDataService);}
       } catch (Exception e) {
           LOGGER.error(e.getMessage());
       }
    }

    /**
     * This method inserts the IndoData records into the database in batches.
     * List containing IndoData objects and batch size will reset after each insertion.
     * @param indoDataService IndoDataService object for inserting data to database
     */
    private void insertData(IndoDataService indoDataService) {
        indoDataService.createBatchIndoData(indoDataList);

        // clear indoExportList and reset batch size count after inserting current batch of data
        indoDataList.clear();
        batchSizeCount = 0;
    }

    /**
     * This method reads the HS Code value in the cell
     * @param sheet Excel sheet
     * @param row Row number
     * @return HS Code for the energy product
     */
    private int readHsCodeValue(XSSFSheet sheet, int row) {
        return Integer.parseInt(sheet
                .getRow(row)
                .getCell(1)
                .getStringCellValue()
                .substring(1, 9));
    }

    /**
     * This method reads the year value in the cell
     * @param sheet Excel sheet
     * @param row Row number
     * @return Year which energy product is exported/imported
     */
    private int readYearValue(XSSFSheet sheet, int row) {
        Cell yearCell = sheet.getRow(row).getCell(0);
        if (yearCell.getCellType() == CellType.NUMERIC) {
            setYearValue((int) yearCell.getNumericCellValue());
        }
        return yearValue;
    }

    /**
     * This method reads the country name value in the cell.
     * String value is capitalized.
     * @param sheet Excel sheet
     * @param col Column number
     * @return Country which energy product is exported to/imported from
     */
    private String readCountryValue(XSSFSheet sheet, int col) {
        Cell countryCell = sheet.getRow(0).getCell(col);
        if (countryCell.getCellType() == CellType.STRING) {
            setCountryValue(excelUtils.capitalizeFirstLetter(countryCell.getStringCellValue()));
        }
        return countryValue;
    }

    /**
     * This method reads the harbor name value in the cell. String value is capitalized.
     * @param sheet Excel sheet
     * @param col Column number
     * @return Harbor which the energy product is exported from/imported to
     */
    private String readHarborValue(XSSFSheet sheet, int col) {
        Cell harborCell = sheet.getRow(1).getCell(col);
        if (harborCell.getCellType() == CellType.STRING) {
            setHarborValue(excelUtils.capitalizeFirstLetter(harborCell.getStringCellValue()));
        }
        return harborValue;
    }

    /**
     * This method reads the product name value in the cell. String value is capitalized.
     * @param sheet Excel sheet
     * @param row Row number
     * @return Energy product name
     */
    private String readProductValue(XSSFSheet sheet, int row) {
        return excelUtils.capitalizeFirstLetter(sheet
                    .getRow(row)
                    .getCell(1)
                    .getStringCellValue()
                    .substring(11)
                    .strip());
    }

    /**
     * This method gets the energy product category from the energy product name based on keyword search on the
     * product name
     * @param productValue Energy product name
     * @return Energy product category
     */
    private String readProductCategoryValue(String productValue) {
        if (productValue.toLowerCase().contains("crude petroleum")) {
            return "Crude Oil";
        } else if (productValue.toLowerCase().contains("condensates")) {
            return "Condensates";
        } else if (productValue.toLowerCase().contains("ron")) {
            return "Gasoline";
        } else if (productValue.toLowerCase().contains("naphtha")) {
            return "Naphtha";
        } else if (productValue.toLowerCase().contains("diesel")) {
            return "Diesel";
        } else if (productValue.toLowerCase().contains("fuel oils")) {
            return "Fuel Oil";
        } else {
            return "Jet Fuel";
        }
    }

    /**
     * This method reads the month value in the cell
     * Month value in Indonesian will be translated to English
     * @param sheet Excel sheet
     * @param col Column number
     * @return Month value in English
     */
    private int readMonthValue(XSSFSheet sheet, int col) {
        String monthInIndo = sheet
                .getRow(2)
                .getCell(col)
                .getStringCellValue()
                .substring(5);
        return MonthIndoWordToNumTranslation.valueOf(monthInIndo).getMonthNum();
    }

    /**
     * This method reads the net USD value exported/imported if value exists
     * @param sheet Excel sheet
     * @param row Row number
     * @param col Column number
     * @return USD value or 0 if value does not exist
     */
    private double readUsdValue(XSSFSheet sheet, int row, int col) {
        Cell usdCell = sheet.getRow(row).getCell(col);
        if (usdCell.getCellType() != CellType.BLANK) {
            return usdCell.getNumericCellValue();
        }
        return 0;
    }

    /**
     * This method reads the net weight value in KG units exported/imported if value exists
     * @param sheet Excel sheet
     * @param row Row number
     * @param col Column number
     * @return Weight value in KG units or 0 if value does not exist
     */
    private double readWeightInKgValue(XSSFSheet sheet, int row, int col) {
        Cell weightCell = sheet.getRow(row).getCell(col);
        if (weightCell.getCellType() != CellType.BLANK) {
            return weightCell.getNumericCellValue();
        }
        return 0;
    }
}
