package com.smu.energydatatradingapp.utils;

import lombok.NoArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class provides methods that support reading and processing data from Excel files
 * @version 1.0 13 Sep 2021
 * @author Chen Jian Yu
 */
@NoArgsConstructor
public class ExcelUtils {
    private final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * This method will capitalize every word in a sentence
     * @param s phrase or sentence
     * @return String with every word capitalize
     */
    public String capitalizeFirstLetter(String s) {
        String[] str = s.trim().split("\\s+");
        for (int i=0; i<str.length; i++) {
            str[i] = str[i].substring(0,1).toUpperCase() + str[i].substring(1).toLowerCase();
        }
        return String.join(" ", str);
    }

    /**
     * This method gets all the files in the folder
     * @param countryFolder the folder for the country
     * @return List of files in the folder
     */
    public File[] getFilesList(String countryFolder) {
        String projRootDir = System.getProperty("user.dir");
        File folder = new File(projRootDir + "/src/main/data/" + countryFolder);
        return folder.listFiles();
    }

    /**
     * This method read the sheet by sheet index in an Excel file
     * @param filePath relative file directory
     * @param sheetIndex sheet index (start from 0)
     * @return Excel sheet in an Excel file/workbook
     * @throws IOException thrown when sheet is not found
     */
    public XSSFSheet getSheetByIndex(String filePath, int sheetIndex) {
        XSSFWorkbook workbook = null;
        
        try {
            FileInputStream fileInput = new FileInputStream(filePath);
            workbook = new XSSFWorkbook(fileInput);
        } catch (IOException e) {
            LOGGER.error("Invalid file type or file name");
        }

        return workbook.getSheetAt(sheetIndex);
    }
}
