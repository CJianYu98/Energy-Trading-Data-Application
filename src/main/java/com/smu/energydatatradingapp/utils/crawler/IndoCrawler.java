package com.smu.energydatatradingapp.utils.crawler;

import com.smu.energydatatradingapp.constant.IndoProductHSCode;
import com.smu.energydatatradingapp.exception.CrawlerErrorException;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@NoArgsConstructor
public class IndoCrawler {
    // web driver constants
    private final String property = "webdriver.chrome.driver";
    private final String website = "https://www.bps.go.id/exim";
    private final String driverPath = "src/main/java/com/smu/energydatatradingapp/utils/crawler/driver/chromedriver";         // for mac
    private final String indoDataFolderPath = System.getProperty("user.dir") + "/src/main/data/indonesia/";                   // for mac
//    private final String driverPath = "src/main/java/com/smu/energydatatradingapp/utils/crawler/driver/chromedriver.exe";   // for windows
//    private final String indoDataFolderPath = System.getProperty("user.dir") + "\\src\\main\\data\\indonesia\\";            // for windows

    private final int waitTime = 3;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // general variables
    private ArrayList<String> years;
    private int month;
    private ArrayList<String> tabs;

    // file names
    private final String exportValueFileName = "indo_fileno_export_value.xls";
    private final String exportWeightFileName = "indo_fileno_export_weight.xls";
    private final String importValueFileName = "indo_fileno_import_value.xls";
    private final String importWeightFileName = "indo_fileno_import_weight.xls";

    /**
     * This method is used to web crawl the indonesia website to download all related products' export and import data
     * @param years ArrayList of years to crawl data for
     * @param month Month to crawl data for
     */
    public void crawl(ArrayList<String> years, int month) {
        // set date parameters
        this.years = years;
        this.month = month;

        // set up web crawler
        setupCrawler();

        try {
            // delete all files in indonesia folder
            deleteFolder(indoDataFolderPath);

            // go to indonesia website on browser
            driver.get(website);

            // close pop-up
            closePopup();

            // group the products into groups of 15 max
            ArrayList<ArrayList> hsCodeGroups = groupHSCodes();

            for (int i = 0; i < hsCodeGroups.size(); i++) {
                // name files by hs code groups
                String replacedExportValueFileName = StringUtils.replace(exportValueFileName, "no", String.valueOf(i+1));
                String replacedExportWeightFileName = StringUtils.replace(exportWeightFileName, "no", String.valueOf(i+1));
                String replacedImportValueFileName = StringUtils.replace(importValueFileName, "no", String.valueOf(i+1));
                String replacedImportWeightFileName = StringUtils.replace(importWeightFileName, "no", String.valueOf(i+1));

                // select export radio input
                driver.findElement(By.id("radios_0")).click();

                // crawl and download export value and weight data
                crawlPage(hsCodeGroups.get(i), replacedExportValueFileName, replacedExportWeightFileName);

                // refresh the page
                driver.navigate().refresh();
                Thread.sleep(1000);

                // select import radio input
                driver.findElement(By.id("radios_1")).click();

                // crawl and download import value and weight data
                crawlPage(hsCodeGroups.get(i), replacedImportValueFileName, replacedImportWeightFileName);

                // refresh the page
                driver.navigate().refresh();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new CrawlerErrorException("Encountered an error while crawling Indonesia website:" + e.getMessage());
        }
        finally {
            // stop the web crawler
            driver.quit();
        }
    }

    /**
     * This method is used to crawl the indonesia webpage to download export/import net value and weight data for the products specified in the parameter
     * @param hsCodes Array of HS codes of products
     * @param newValueFileName Name of downloaded net value file
     * @param newWeightFileName Name of downloaded net weight file
     * @throws InterruptedException Thrown when the thread is sleeping and is interrupted
     */
    private void crawlPage(ArrayList<String> hsCodes, String newValueFileName, String newWeightFileName) throws InterruptedException {
        // filling up the form - input "By" method with "Full HS"
        fillByInputWithFullHSCode();

        // filling up the form - input year
        fillYearInput();

        // filling up the form - input "Kode HS" of products
        fillHSCodeInput(hsCodes);

        // scroll page to find month input
        scrollPage(driver.findElement(By.id("s2id_autogen7")));

        // filling up the form - input month
        fillMonthInput();

        // scroll page to find process button
        scrollPage(driver.findElement(By.id("btn-proses")));

        // click on "Proses" button
        process();

        // scroll page to find process button
        scrollPage(driver.findElement(By.id("output")));

        if (dataTableExist()) {
            // scroll page to find process button
            scrollPage(driver.findElement(By.id("btnexport")));

            // download products net value file
            exportExcel();

            // open Chrome downloads in new tab
            newDownloadTab();

            // rename products net value file
            renameFile(indoDataFolderPath + newValueFileName);

            // switch back to indonesia website
            driver.switchTo().window(tabs.get(0));

            // scroll page to find metric dropdown
            scrollPage(driver.findElement(By.cssSelector("select.pvtAggregator")));

            // change to net weight metric
            selectWeightMetric();

            // scroll page to find process button
            scrollPage(driver.findElement(By.id("btnexport")));

            // download products net weight file
            exportExcel();

            // switch back to chrome downloads tab
            driver.switchTo().window(tabs.get(1));
            Thread.sleep(1000);

            // rename products net weight file
            renameFile(indoDataFolderPath + newWeightFileName);

            // switch back to indonesia website
            driver.switchTo().window(tabs.get(0));
        }

        // scroll page to top
        js.executeScript("window.scrollTo(0, 0);", "");
    }

    /**
     * This method sets the driver path and default download path then instantiates a ChromeDriver, WebDriverWait and JavascriptExecutor
     */
    private void setupCrawler() {
        // set web driver to be chromedriver
        System.setProperty(property, driverPath);

        // initiate WebDriver with set download default directory to indonesia folder
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", indoDataFolderPath);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        driver = new ChromeDriver(options);

        // initiate WebDriverWait
        wait = new WebDriverWait(driver, waitTime);

        // initiate JavascriptExecutor
        js = (JavascriptExecutor) driver;
    }

    /**
     * This method groups the product HS codes into groups of 15 max
     * @return Arraylist containing ArrayLists which are groups of HS codes
     */
    private ArrayList<ArrayList> groupHSCodes() {
        // to keep count of number of HS codes
        int numCodes = 0;

        // instantiate 2 ArrayList:
        // hsCodeGroups - store the groups of HS codes
        // hsCodeGroup - 1 group which stores max 15 HS codes
        ArrayList<ArrayList> hsCodeGroups = new ArrayList();
        ArrayList<String> hsCodeGroup = new ArrayList<String>();

        // loop through all product HS codes:
        // add each code into the group and keep count of them
        // once a group has 15 codes, add it to hsCodeGroups and create a new group
        for (IndoProductHSCode product : IndoProductHSCode.values()) {
            for (String hsCode : product.getHsCodes()) {
                hsCodeGroup.add(hsCode);
                numCodes++;
                if (numCodes % 15 == 0) {
                    hsCodeGroups.add(hsCodeGroup);
                    hsCodeGroup = new ArrayList<String>();
                }
            }
        }
        // check for HS codes that have not been added to hsCodeGroups
        if (numCodes % 15 != 1) {
            hsCodeGroups.add(hsCodeGroup);
        }
        return hsCodeGroups;
    }

    /**
     * This method closes the popup on the landing webpage
     */
    private void closePopup() {
        if (driver.findElements(By.className("fancybox-close")).size() == 1) {
//        wait.until(presenceOfElementLocated(By.className("fancybox-close")));
            driver.findElement(By.className("fancybox-close")).click();
        }
    }

    /**
     * This method fills the "By" input to indicate that the crawler wants to search for products by full HS codes
     */
    private void fillByInputWithFullHSCode() {
        driver.findElement(By.id("s2id_autogen1")).click();
        driver.findElements(By.className("select2-result")).get(0).click();
        wait.until(presenceOfElementLocated(By.id("s2id_autogen2")));
        driver.findElement(By.id("s2id_autogen2")).click();
        driver.findElements(By.className("select2-result")).get(1).click();
    }

    /**
     * This method fills the "Year" input with private variable years
     */
    private void fillYearInput() {
        wait.until(presenceOfElementLocated(By.id("s2id_autogen3")));
        for (int i = 0; i < years.size(); i++) {
            driver.findElement(By.id("s2id_autogen3")).click();
            driver.findElement(By.id("s2id_autogen3")).sendKeys(years.get(i));
            wait.until(presenceOfElementLocated(By.className("select2-result")));
            driver.findElements(By.className("select2-result")).get(0).click();
        }
    }
    /**
     * This method fills the "Month" input with private variable month
     */
    private void fillMonthInput() {
        if (month != 0) {
            wait.until(presenceOfElementLocated(By.id("s2id_autogen7")));
            driver.findElement(By.id("s2id_autogen7")).click();
            wait.until(presenceOfElementLocated(By.className("select2-result")));
            driver.findElements(By.className("select2-result")).get(month).click();
        }
    }

    /**
     * This method fills the "HS Code" input with all the product HS codes in the parameter hsCodes
     * @param hsCodes HS codes of products
     */
    private void fillHSCodeInput(ArrayList<String> hsCodes) {
        wait.until(presenceOfElementLocated(By.id("s2id_autogen4")));
        for (String hsCode : hsCodes) {
            scrollPage(driver.findElement(By.id("s2id_autogen5")));
            driver.findElement(By.id("s2id_autogen4")).click();
            driver.findElement(By.id("s2id_autogen4")).sendKeys(hsCode);
            wait.until(presenceOfElementLocated(By.className("select2-result")));
            driver.findElements(By.className("select2-result")).get(0).click();
        }
    }

    /**
     * Check whether the website has data matching the search inputs
     * @return Boolean - true if there is matching data else false
     * @throws InterruptedException Thrown when the thread is sleeping and is interrupted
     */
    private boolean dataTableExist() throws InterruptedException {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("output")));
        Thread.sleep(2000);
        return (driver.findElements(By.cssSelector("select.pvtRenderer")).size() == 1);
    }

    /**
     * This method clicks the process button on the website to search for data
     */
    private void process() {
        wait.until(presenceOfElementLocated(By.id("btn-proses")));
        driver.findElement(By.id("btn-proses")).click();
    }

    /**
     * This method exports the data into an excel file
     * @throws InterruptedException Thrown when the thread is sleeping and is interrupted
     */
    private void exportExcel() throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnexport")));
        driver.findElement(By.id("btnexport")).click();
        Thread.sleep(1500);
    }

    /**
     * This method opens a new tab in Chrome and navigates to Chrome downloads page
     * @throws InterruptedException Thrown when the thread is sleeping and is interrupted
     */
    private void newDownloadTab() throws InterruptedException {
        // open new tab in chrome
        js.executeScript("window.open()");
        tabs = new ArrayList<String>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));

        // navigate to chrome downloads
        driver.get("chrome://downloads");
        Thread.sleep(1000);
    }

    /**
     * This method changes the metric to weight for the data table on the website
     * @throws InterruptedException Thrown when the thread is sleeping and is interrupted
     */
    private void selectWeightMetric() throws InterruptedException {
        Select selectExportMetric = new Select(driver.findElement(By.cssSelector("select.pvtAggregator")));
        selectExportMetric.selectByIndex(1);
        Thread.sleep(1000);
    }

    /**
     * This method is used to rename the downloaded file to the file name specified in the parameter
     * @param newFileName Name for renaming downloaded file
     */
    private void renameFile(String newFileName) {
        // get the name of the most recent downloaded file
        String oldFileName = (String) js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList downloads-item').shadowRoot.querySelector('div#content #file-link').text");

        // rename downloaded file
        new File(indoDataFolderPath + oldFileName).renameTo(new File(newFileName));
    }

    /**
     * This method deletes all files in a specified folder
     * @param folderPath path of folder to be deleted
     * @throws IOException Thrown when the deleteFile method experiences a failure
     */
    private void deleteFolder(String folderPath) throws IOException {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file: listOfFiles) {
            deleteFile(file);
        }
    }

    /**
     * This method is used to delete the file with the path specified in the parameter if it exists
     * @param file file to be deleted
     * @throws IOException Thrown when the delete file operation experiences a failure
     */
    private void deleteFile(File file) throws IOException {
        // delete file if already exist
        Files.deleteIfExists(file.toPath());
    }

    /**
     * This method scroll the page until the desired element is in view
     * @param element Web element to view
     */
    private void scrollPage(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }
}
