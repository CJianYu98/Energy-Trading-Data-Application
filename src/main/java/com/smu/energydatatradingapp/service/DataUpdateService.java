package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.exception.*;
import com.smu.energydatatradingapp.utils.ExcelUtils;
import com.smu.energydatatradingapp.utils.HtmlToXlsxUtils;
import com.smu.energydatatradingapp.utils.ReadIndoExcelData;
import com.smu.energydatatradingapp.utils.ReadTWExcelData;
import com.smu.energydatatradingapp.utils.crawler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * This DataUpdateService class will handle the updating of the database
 */
@Component
@EnableScheduling
public class DataUpdateService {

    @Autowired
    private IndoDataService indoDataService;

    @Autowired
    private TWSupplyService twSupplyService;

    @Autowired
    private TWConsumptionService twConsumptionService;

    @Autowired
    private TWConversionService twConversionService;

    private final Logger LOGGER = LoggerFactory.getLogger(DataUpdateService.class);

    private final int year = 2017;

    /**
     * This method is scheduled to update database with most recent month data at 12am on the 15th day of every month.
     */
    @Scheduled(cron = "0 0 0 28 * ?", zone = "Asia/Singapore")
    private void update() {

        LOGGER.info(">>>>> Requested update in DataUpdaterController");

        // Get date parameters
        ArrayList<String> years = new ArrayList<String>();
        years.add("" + Calendar.getInstance().get(Calendar.YEAR));
        int month = Calendar.getInstance().get(Calendar.MONTH)-2;
        String date = "current";

        // Crawl and insert data into database
        try {
            crawlAndInsertData(years, month, date);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method pre-loads the database with historical data from a given start year till current date.
     */
//    @EventListener(ApplicationReadyEvent.class)
    private void preLoad() {

        LOGGER.info(">>>>> Requested pre-load in DataUpdaterController");

        // Get date parameters
        ArrayList<String> years = new ArrayList<String>();
        for (int i = year; i <= Calendar.getInstance().get(Calendar.YEAR); i++) {
            years.add("" + i);
        }
        int month = 0;
        String date = "" + year + "年01月";

        // Crawl and insert data into database
        try {
            crawlAndInsertData(years, month, date);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method crawls the websites and inserts data into the database.
     * <p> It first crawls the Indonesia and Taiwan website to download the most recent month data for the relevant
     * products.
     * It then converts html files to Excel xlsx file format.
     * Relevant Excel files will then be read and processed to retrieve relevant data in the Excel sheet.
     * Cells in the Excel sheet will be iterated through so to extract the necessary information in that row header
     * and column header.
     * These data will then be inserted to the database in batches of 100 to improve the overall performance while
     * inserting data.
     * @throws IOException Thrown when reader failed to read the Excel sheet
     * @throws CrawlerErrorException Thrown when the crawler failed to crawl
     */
    private void crawlAndInsertData(ArrayList<String> years, int month, String date) throws IOException, CrawlerErrorException {
        // Crawl Indonesia website
        LOGGER.info("----- Started crawling Indonesian data");

        IndoCrawler indoCrawler = new IndoCrawler();
        indoCrawler.crawl(years, month);
        LOGGER.info("----- Completed crawling Indonesian data");

        // Converting html files to xlsx file format
        HtmlToXlsxUtils htmlToXlsxUtils = new HtmlToXlsxUtils();
        htmlToXlsxUtils.htmlToXlsx("indonesia");
        LOGGER.info("----- Converted html files to xlsx file format");

        // Retrieving Indonesia export and import files
        ExcelUtils excelUtils = new ExcelUtils();
        File[] listOfIndoFiles = excelUtils.getFilesList("indonesia");
        File[] xlsxIndoDataFiles = Arrays.stream(listOfIndoFiles)
                .filter(f -> f.getName().endsWith(".xlsx")
                        && f.isFile())
                .sorted()
                .toArray(File[]::new);
        LOGGER.info("----- Read in xlsx files for Indonesia data");

        // Read data from Indonesia Excel sheets and insert into database
        ReadIndoExcelData readIndoExcelData = new ReadIndoExcelData();
        readIndoExcelData.readAndInsertIndoData(xlsxIndoDataFiles, indoDataService);
        LOGGER.info("----- Completed insertion of Indonesian data");

        // Crawl Taiwan website
        LOGGER.info("----- Started crawling Taiwan data");
        TWCrawler twCrawler = new TWCrawler();
        twCrawler.crawl(date);
        LOGGER.info("----- Completed crawling Taiwan data");

        // Retrieving Taiwan supply and consumption files
        File[] twEnergyDataFiles = excelUtils.getFilesList("taiwan");

        // Read data from Taiwan Excel sheet and insert into database
        ReadTWExcelData readTWExcelData = new ReadTWExcelData();
        readTWExcelData.readAndInsertTWSupplyData(twEnergyDataFiles[0], twSupplyService);
        readTWExcelData.readAndInsertTWConsumptionData(twEnergyDataFiles[0], twConsumptionService);
        readTWExcelData.readAndInsertTWConversionData(twEnergyDataFiles[0], twConversionService);
        LOGGER.info("----- Completed insertion of Taiwan data");
    }
}