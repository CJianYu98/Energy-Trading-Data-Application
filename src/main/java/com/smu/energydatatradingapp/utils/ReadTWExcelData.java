package com.smu.energydatatradingapp.utils;

import com.smu.energydatatradingapp.model.TWConsumption;
import com.smu.energydatatradingapp.model.TWConversion;
import com.smu.energydatatradingapp.model.TWSupply;
import com.smu.energydatatradingapp.service.TWConsumptionService;
import com.smu.energydatatradingapp.service.TWConversionService;
import com.smu.energydatatradingapp.service.TWSupplyService;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import java.io.File;
import java.io.IOException;

/**
 * This class provides methods which read the Excel files which consists of Indonesian energy product export and
 * import data.
 * @version 1.0 12 Sep 2021
 * @author Ian Koh
 */
@Getter
@Setter
public class ReadTWExcelData {
    private final Logger LOGGER = LoggerFactory.getLogger(ReadTWExcelData.class);

    List<TWSupply> twSupplyList = new ArrayList<>();
    List<TWConsumption> twConsumptionList = new ArrayList<>();
    List<TWConversion> twConversionList = new ArrayList<>();

    // Excel
    private final ExcelUtils excelUtils = new ExcelUtils();
    private int batchSizeToInsert = 200;

    // To keep track of batch size
    private int consumptionBatchSizeCount = 0;
    private int conversionBatchSizeCount = 0;
    private int supplyBatchSizeCount = 0;

    private int sheetNumber;
    private int rowIndex;
    private int numberOfSheets = 13;
    private int numberOfColumns;

    // General
    private int monthValue;
    private int yearValue;
    private String product;
    private String specificProduct;
    private double volume;

    // Consumption
    private String sector;
    private String subSector;

    // Supply
    private String supplyType;

    // Conversion
    private String conversionType;

    // Constants
    public static Map<Integer, String> twProductCategoryMap;
    public static Map<Integer, String> twProductValueMap;
    public static Map<String, String> monthTranslationValueMap;
    public static Map<Integer, Double> twVolumeConversionMap;
    public static Map<Integer, String> typeMap;

    static{
        // Value * conversion rate / 1000 / Number of days in the month
        twVolumeConversionMap = new HashMap<>();
        twVolumeConversionMap.put(0,6.2898/1000);
        twVolumeConversionMap.put(1,0.541*11.600/1000);
        twVolumeConversionMap.put(2,0.541*11.600/1000);
        twVolumeConversionMap.put(3,0.753*8.350/1000);
        twVolumeConversionMap.put(4,9.0/1000);
        twVolumeConversionMap.put(5,0.753*8.350/1000);
        twVolumeConversionMap.put(6,0.753*8.350/1000);
        twVolumeConversionMap.put(7,0.753*8.350/1000);
        twVolumeConversionMap.put(8,0.753*8.350/1000);
        twVolumeConversionMap.put(9,0.753*8.350/1000);
        twVolumeConversionMap.put(10,0.798*7.88/1000);
        twVolumeConversionMap.put(11,0.843*7.460/1000);
        twVolumeConversionMap.put(12,0.991*6.350/1000);
    }

    static {
        // Identify product category through index
        twProductCategoryMap = new HashMap<>();
        twProductCategoryMap.put(0, "Crude Oil");
        twProductCategoryMap.put(1, "LPG");
        twProductCategoryMap.put(2, "LPG");
        twProductCategoryMap.put(3, "Gasoline");
        twProductCategoryMap.put(4, "Naphtha");
        twProductCategoryMap.put(5, "Gasoline");
        twProductCategoryMap.put(6, "Gasoline");
        twProductCategoryMap.put(7, "Gasoline");
        twProductCategoryMap.put(8, "Gasoline");
        twProductCategoryMap.put(9, "Jet Fuel");
        twProductCategoryMap.put(10, "Jet Fuel");
        twProductCategoryMap.put(11, "Diesel Fuel");
        twProductCategoryMap.put(12, "Fuel Oil");
    }

    static {
        // Identify specific product through index
        twProductValueMap = new HashMap<>();
        twProductValueMap.put(0, "Crude Oil");
        twProductValueMap.put(1, "Liquefied Petroleum Gas");
        twProductValueMap.put(2, "Propane Mixed Gas");
        twProductValueMap.put(3, "Natural Gasoline");
        twProductValueMap.put(4, "Naphtha");
        twProductValueMap.put(5, "Motor Gasoline");
        twProductValueMap.put(6, "Unleaded Gasoline");
        twProductValueMap.put(7, "Aviation Gasoline");
        twProductValueMap.put(8, "Aviation Fuel Gasoline Type");
        twProductValueMap.put(9, "Aviation Fuel Kerosene Type");
        twProductValueMap.put(10, "Kerosene");
        twProductValueMap.put(11, "Diesel Fuel");
        twProductValueMap.put(12, "Fuel Oil");
    }

    static {
        // Identify type through index
        typeMap = new HashMap<>();
        typeMap.put(3, "Self Produced");
        typeMap.put(4, "Import");
        typeMap.put(5, "Export");
        typeMap.put(6, "International Shipping");
        typeMap.put(7, "International Air Freight");
        typeMap.put(8, "Inventory Changes");
        typeMap.put(9, "Total Primary Energy Supply");
        typeMap.put(10, "Conversion Between Products (Transfer Out)");
        typeMap.put(11, "Transformation Input");
        typeMap.put(21, "Transformation Output");
        typeMap.put(22, "Conversion Between Products (Transfer In)");
        typeMap.put(23, "Statistical Difference");
        typeMap.put(25, "Coal Industry");
        typeMap.put(26, "Coal Products");
        typeMap.put(27, "Blast furnace workshop");
        typeMap.put(28, "Oil and gas mining");
        typeMap.put(29, "Oil refinery");
        typeMap.put(30, "Power Plant");
        typeMap.put(31, "Electricity for pumping water");
        typeMap.put(32, "Cogeneration Plant");
        typeMap.put(33, "Gas Fuel Supply Industry");
        typeMap.put(35, "Mining and soil extraction industry");
        typeMap.put(36, "Food, Beverage and Tobacco industry");
        typeMap.put(37, "Textile Garment and Apparel Industry");
        typeMap.put(38, "Leather and fur industry");
        typeMap.put(39, "Wood, bamboo and furniture");
        typeMap.put(40, "Pulp, paper and paper products");
        typeMap.put(41, "Printing Industry");
        typeMap.put(42, "Chemical Material Manufacturing");
        typeMap.put(50, "Chemical Manufacturing");
        typeMap.put(51, "Rubber products Manufacturing");
        typeMap.put(52, "Plastic products manufacturing");
        typeMap.put(53, "Non metallic mineral products");
        typeMap.put(58, "Metal basic industry");
        typeMap.put(62, "Metal products manufacturing");
        typeMap.put(63, "Machinery and equipment");
        typeMap.put(64, "Computer communications");
        typeMap.put(66, "Transportation tool manufacturing");
        typeMap.put(67, "Precision optical medical equipment");
        typeMap.put(68, "Other industrial products manufacturing");
        typeMap.put(69, "Water supply and pollution removal");
        typeMap.put(70, "Construction industry");
        typeMap.put(71, "Others");
        typeMap.put(73, "Domestic aviation");
        typeMap.put(74, "Highway");
        typeMap.put(75, "Railway");
        typeMap.put(76, "Pipeline transportation");
        typeMap.put(77, "Domestic water transport");
        typeMap.put(78, "Others");
        typeMap.put(80, "Animal husbandry");
        typeMap.put(81, "Fishery");
        typeMap.put(83, "Wholesale and retail");
        typeMap.put(84, "Accommodation and catering");
        typeMap.put(85, "Transportation service industry");
        typeMap.put(86, "Warehousing industry");
        typeMap.put(87, "Communications Industry");
        typeMap.put(88, "Financial Insurance and real estate");
        typeMap.put(89, "Business services");
        typeMap.put(90, "Social service and personal service");
        typeMap.put(91, "Public administration");
        typeMap.put(92, "Others");
        typeMap.put(93, "Residential sector");
        typeMap.put(95, "Industry transformation");
        typeMap.put(97, "Transport sector");
        typeMap.put(98, "Others");
    }

    /**
     * This method goes through the Taiwan Excel file and reads the supply data in each Excel file.
     * Data will be inserted in batches into the database.
     * @param xlsxFile of Excel files to be read
     * @param twSupplyService TWSupplyService object
     * @throws IOException Thrown when Excel sheet is not found
     */
    public void readAndInsertTWSupplyData(File xlsxFile, TWSupplyService twSupplyService) throws IOException {
        try {
            // Looping through the Excel sheets
            for (int sheetNo = 0; sheetNo < numberOfSheets; sheetNo++) {
                XSSFSheet taiwanDataSheet = excelUtils.getSheetByIndex(xlsxFile.getPath(), sheetNo);
                product = twProductCategoryMap.get(sheetNo);
                specificProduct = twProductValueMap.get(sheetNo);
                numberOfColumns = taiwanDataSheet.getRow(1).getPhysicalNumberOfCells();

                for (int col = 1; col < numberOfColumns; col++) {
                    yearValue = readYearValue(taiwanDataSheet, col);
                    monthValue = readMonthValue(taiwanDataSheet, col);

                    // Relevant Rows
                    List neededRowsList = Arrays.asList(3, 4, 5, 6, 7, 8, 9);
                    for (int i = 0; i < neededRowsList.size(); i++) {
                        int currentRowNum = (int) neededRowsList.get(i);
                        XSSFRow currentRow = taiwanDataSheet.getRow((Integer) neededRowsList.get(i) - 1);
                        supplyType = typeMap.get(currentRowNum);
                        Double conversionRate = twVolumeConversionMap.get(sheetNo);

                        // Check for "-" cell
                        if (String.valueOf(currentRow.getCell(col)).equals("-")){
                            volume = 0.0;
                        }
                        else {
                            // Volume * conversion rate / numberOfDays
                            volume = Double.parseDouble(String.valueOf(currentRow.getCell(col))) * conversionRate / numberOfDays(monthValue);
                        }

                        // Creating a new TWSupply Object
                        TWSupply twSupply = new TWSupply(
                                yearValue,
                                monthValue,
                                product,
                                specificProduct,
                                supplyType,
                                volume);

                        // Add twSupply to batch List
                        twSupplyList.add(twSupply);

                        // Increment batch size by 1 after adding data record into twConsumptionList
                        supplyBatchSizeCount++;

                        // If batch size reaches limit, insert data to database
                        if (supplyBatchSizeCount == batchSizeToInsert) {
                            insertSupplyData(twSupplyService);
                        }
                    }
                }
            }
            if(supplyBatchSizeCount>0){
                insertSupplyData(twSupplyService);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method goes through the Taiwan Excel file and reads the conversion data in each Excel file.
     * Data will be inserted in batches into the database.
     * @param xlsxFile of Excel files to be read
     * @param twConversionService TWConversionService object
     * @throws IOException Thrown when Excel sheet is not found
     */
    public void readAndInsertTWConversionData(File xlsxFile, TWConversionService twConversionService) throws IOException {
        try {
            // Looping through the Excel sheets
            for (int sheetNo = 0; sheetNo < numberOfSheets; sheetNo++) {
                XSSFSheet taiwanDataSheet = excelUtils.getSheetByIndex(xlsxFile.getPath(), sheetNo);
                product = twProductCategoryMap.get(sheetNo);
                specificProduct = twProductValueMap.get(sheetNo);
                numberOfColumns = taiwanDataSheet.getRow(1).getPhysicalNumberOfCells();

                for (int col = 1; col < numberOfColumns; col++) {
                    yearValue = readYearValue(taiwanDataSheet, col);
                    monthValue = readMonthValue(taiwanDataSheet, col);

                    // Irrelevant Rows
                    List rowsNotNeeded = Arrays.asList(12,13,14,15,16,17,18,19,20);
                    for (int rowNo = 10; rowNo < 24; rowNo++) {

                        // Skip rows that are not needed
                        if (rowsNotNeeded.contains(rowNo)) {
                            continue;
                        }

                        conversionType = typeMap.get(rowNo);
                        XSSFRow currentRow = taiwanDataSheet.getRow(rowNo - 1);
                        Double conversionRate = twVolumeConversionMap.get(sheetNo);

                        // Check for "-" cell
                        if (String.valueOf(currentRow.getCell(col)).equals("-")){
                            volume = 0.0;
                        }
                        else {
                            // Volume * conversion rate / numberOfDays
                            volume = Double.parseDouble(String.valueOf(currentRow.getCell(col))) * conversionRate / numberOfDays(monthValue);
                        }

                        // Creating a new TWConversion Object
                        TWConversion twConversion = new TWConversion(
                                yearValue,
                                monthValue,
                                product,
                                specificProduct,
                                conversionType,
                                volume);

                        if (conversionType == null) {
                            System.out.println(rowNo);
                        }

                        // Add twConversion to batch List
                        twConversionList.add(twConversion);

                        // Increment batch size by 1 after adding data record into twConsumptionList
                        conversionBatchSizeCount++;

                        // If batch size reaches limit, insert data to database
                        if (conversionBatchSizeCount == batchSizeToInsert) {
                            insertConversionData(twConversionService);
                        }
                    }
                }
            }
            if (conversionBatchSizeCount>0){
                insertConversionData(twConversionService);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method goes through the Taiwan Excel file and reads the consumption data in each Excel file.
     * Data will be inserted in batches into the database.
     * @param xlsxFile of Excel files to be read
     * @param twConsumptionService TWConsumptionService object
     * @throws IOException Thrown when Excel sheet is not found
     */
    public void readAndInsertTWConsumptionData(File xlsxFile, TWConsumptionService twConsumptionService) throws IOException {
        try {
            // Looping through the Excel sheets
            for (int sheetNo = 0; sheetNo < numberOfSheets; sheetNo++) {
                XSSFSheet taiwanDataSheet = excelUtils.getSheetByIndex(xlsxFile.getPath(), sheetNo);
                product = twProductCategoryMap.get(sheetNo);
                specificProduct = twProductValueMap.get(sheetNo);
                numberOfColumns = taiwanDataSheet.getRow(1).getPhysicalNumberOfCells();

                for (int col = 1; col < numberOfColumns; col++) {
                    yearValue = readYearValue(taiwanDataSheet, col);
                    monthValue = readMonthValue(taiwanDataSheet, col);

                    List rowsNotNeeded = Arrays.asList(34, 43, 44, 45, 46, 47, 48, 49, 54, 55, 56, 57, 59, 60, 61, 65, 72, 79, 82, 94, 96);
                    for (int rowNo = 25; rowNo <= taiwanDataSheet.getPhysicalNumberOfRows(); rowNo++) {

                        // Skip rows that are not needed
                        if (rowsNotNeeded.contains(rowNo)) {
                            continue;
                        }

                        if (rowNo >= 25 && rowNo <= 33) {
                            sector = "Own Use By Energy Sector";
                        }

                        if (rowNo >= 35 && rowNo <= 71) {
                            sector = "Industrial Sector";
                        }

                        if (rowNo >= 73 && rowNo <= 78) {
                            sector = "Transport Sector";
                        }

                        if (rowNo >= 80 && rowNo <= 81) {
                            sector = "Agricultural Sector";
                        }

                        if (rowNo >= 83 && rowNo <= 92) {
                            sector = "Service Sector";
                        }

                        if (rowNo == 93) {
                            sector = "Residential Sector";
                        }

                        if (rowNo >= 95 && rowNo <= 98) {
                            sector = "Non energy consumption";
                        }

                        subSector = typeMap.get(rowNo);
                        XSSFRow currentRow = taiwanDataSheet.getRow(rowNo - 1);
                        Double conversionRate = twVolumeConversionMap.get(sheetNo);

                        // Check for "-" cell
                        if (String.valueOf(currentRow.getCell(col)).equals("-")){
                            volume = 0.0;
                        }
                        else {
                            // Volume * conversion rate / numberOfDays
                            volume = Double.parseDouble(String.valueOf(currentRow.getCell(col))) * conversionRate / numberOfDays(monthValue);
                        }

                        // Creating a new TWSupply Object
                        TWConsumption twConsumption = new TWConsumption(
                                yearValue,
                                monthValue,
                                product,
                                specificProduct,
                                sector,
                                subSector,
                                volume);

                        if (subSector == null) {
                            System.out.println(rowNo);
                        }

                        // Add twConsumption to batch List
                        twConsumptionList.add(twConsumption);

                        // Increment batch size by 1 after adding data record into twConsumptionList
                        consumptionBatchSizeCount++;

                        // If batch size reaches limit, insert data to database
                        if (consumptionBatchSizeCount == batchSizeToInsert) {
                            insertConsumptionData(twConsumptionService);
                        }
                    }
                }
            }
            if (consumptionBatchSizeCount>0){
                insertConsumptionData(twConsumptionService);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method inserts Taiwan Consumption Data and invokes the Taiwan Consumption Service
     * method createBatchTWConsumption, taking in the Taiwan Consumption List as its parameter.
     * @param twConsumptionService
     */
    private void insertConsumptionData(TWConsumptionService twConsumptionService) {
        twConsumptionService.createBatchTWConsumption(twConsumptionList);

        // Clear twConsumptionList and reset batch size count after inserting current batch of data
        twConsumptionList.clear();
        consumptionBatchSizeCount = 0;
    }

    /**
     * This method inserts Taiwan Conversion Data and invokes the Taiwan Conversion Service
     * method createBatchTWConversion, taking in the Taiwan Conversion List as its parameter.
     * @param twConversionService
     */
    private void insertConversionData(TWConversionService twConversionService) {
        twConversionService.createBatchTWConversion(twConversionList);

        // Clear twConversionList and reset batch size count after inserting current batch of data
        twConversionList.clear();
        conversionBatchSizeCount = 0;
    }

    /**
     * This method inserts Taiwan Supply Data and invokes the Taiwan Supply Service
     * method createBatchTWSupply, taking in the Taiwan Supply List as its parameter.
     * @param twSupplyService
     */
    private void insertSupplyData(TWSupplyService twSupplyService){
        twSupplyService.createBatchTWSupply(twSupplyList);

        // Clear twSupplyList and reset batch size count after inserting current batch of data
        twSupplyList.clear();
        supplyBatchSizeCount = 0;
    }

    /**
     *This method goes through the Taiwan Excel Sheet (Product) and reads the year value for each product
     * @param sheet
     * @param col
     * @return Integer type year value
     */
    private int readYearValue(XSSFSheet sheet, int col) {
        return Integer.parseInt(sheet
                .getRow(1)
                .getCell(col)
                .getStringCellValue()
                .substring(0,4));
    }

    /**
     *This method goes through the Taiwan Excel Sheet (Product) and reads the month value for each product
     * @param sheet
     * @param col
     * @return Month value of type int
     */
    private int readMonthValue(XSSFSheet sheet, int col){
        return Integer.parseInt(sheet.getRow(1)
                .getCell(col)
                .getStringCellValue()
                .substring(5,7));
    }

    /**
     *This method takes in a month value ( 1 --> January, 2 --> February..) and returns the number of days
     * in that particular month
     * @param monthValue
     * @return Number of Days of type int
     */
    private int numberOfDays (int monthValue){
        int numberOfDays;
        if (monthValue%2==0 & monthValue!=2) {
            numberOfDays = 30;
        }
        else if (monthValue%2!=0){
            numberOfDays = 31;
        }

        // February
        else{
            numberOfDays = 28;
        }
        return numberOfDays;
    }
}





















