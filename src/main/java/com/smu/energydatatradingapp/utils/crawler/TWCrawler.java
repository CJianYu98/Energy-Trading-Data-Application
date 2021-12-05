package com.smu.energydatatradingapp.utils.crawler;

import com.smu.energydatatradingapp.constant.TWXPath;
import com.smu.energydatatradingapp.exception.CrawlerErrorException;
import lombok.NoArgsConstructor;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

@NoArgsConstructor
public class TWCrawler {
    // web driver constants
    private final String property = "webdriver.chrome.driver";
    private final String website = "http://www.esist.org.tw/Database/Search?PageId=0";
    private final String driverPath = "src/main/java/com/smu/energydatatradingapp/utils/crawler/driver/chromedriver";         // for mac
    private final String twDataFolderPath = System.getProperty("user.dir") + "/src/main/data/taiwan/";                        // for mac
//    private final String driverPath = "src/main/java/com/smu/energydatatradingapp/utils/crawler/driver/chromedriver.exe";   // for windows
//    private final String twDataFolderPath = System.getProperty("user.dir") + "\\src\\main\\data\\taiwan\\";                 // for windows
    private final int waitTime = 3;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // general variables
    private String date;
    private ArrayList<String> tabs;

    // file name
    private final String fileName = "tw_energy_data.xlsx";

    public void crawl(String date) {
        // set start date to crawl from
        this.date = date;

        // set up web crawler
        setupCrawler();

        try {
            // delete all files in taiwan folder
            deleteFolder(twDataFolderPath);

            // go to taiwan website on browser
            driver.get(website);

            // select the year format to be Gregorian calendar
            selectGregorianYearFormat();

            // select the latest month
            selectStartDate();

            // check relevant products checkboxes
            checkRelevantProducts();

            // check "select all" under supply
            checkAllSupply();

            // check "select all" under consumption
            checkAllConsumption();

            // check "select all" under consumption
            checkAllConversion();

            // submit search form
            submitForm();

            //download file
            downloadExcel();

            // open Chrome downloads in new tab
            newDownloadTab();

            // rename products net value file
            renameFile(twDataFolderPath + fileName);

        } catch (Exception e) {
            throw new CrawlerErrorException("Encountered an error while crawling Taiwan website:" + e.getMessage());
        } finally {
            driver.quit();
        }
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
        chromePrefs.put("download.default_directory", twDataFolderPath);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        driver = new ChromeDriver(options);

        // initiate WebDriverWait
        wait = new WebDriverWait(driver, waitTime);

        // initiate JavascriptExecutor
        js = (JavascriptExecutor) driver;
    }

    /**
     * This method selects Gregorian calendar as the year format
     */
    private void selectGregorianYearFormat() {
        Select selectYearType = new Select(driver.findElement(By.id("yearType")));
        selectYearType.selectByIndex(0);
    }

    /**
     * This method selects the start date to crawl data from
     */
    private void selectStartDate() {
        Select selectStartDate = new Select(driver.findElement(By.id("ddl_s_m")));
        if (date == "current") {
            selectStartDate.selectByIndex(selectStartDate.getOptions().size() - 1);
        } else {
            selectStartDate.selectByVisibleText(date);
        }
    }

    /**
     * This method checks the checkboxes of relevant products
     */
    private void checkRelevantProducts() {
        WebElement crudeCB = driver.findElement(By.xpath(TWXPath.crudeOil.getProductXPath()));
        js.executeScript("arguments[0].click()", crudeCB);
        WebElement liquefiedCB = driver.findElement(By.xpath(TWXPath.liquefiedCB.getProductXPath()));
        js.executeScript("arguments[0].click()", liquefiedCB);
        WebElement propaneCB = driver.findElement(By.xpath(TWXPath.propaneCB.getProductXPath()));
        js.executeScript("arguments[0].click()", propaneCB);
        WebElement naturalGasCB = driver.findElement(By.xpath(TWXPath.naturalGasCB.getProductXPath()));
        js.executeScript("arguments[0].click()", naturalGasCB);
        WebElement napthaCB = driver.findElement(By.xpath(TWXPath.napthaCB.getProductXPath()));
        js.executeScript("arguments[0].click()", napthaCB);
        WebElement motorGasCB = driver.findElement(By.xpath(TWXPath.motorGasCB.getProductXPath()));
        js.executeScript("arguments[0].click()", motorGasCB);
        WebElement unleadedGasCB = driver.findElement(By.xpath(TWXPath.unleadedGasCB.getProductXPath()));
        js.executeScript("arguments[0].click()", unleadedGasCB);
        WebElement aviationGasCB = driver.findElement(By.xpath(TWXPath.aviationGasCB.getProductXPath()));
        js.executeScript("arguments[0].click()", aviationGasCB);
        WebElement aviationFuelGCB = driver.findElement(By.xpath(TWXPath.aviationFuelGCB.getProductXPath()));
        js.executeScript("arguments[0].click()", aviationFuelGCB);
        WebElement aviationFuelKCB = driver.findElement(By.xpath(TWXPath.aviationFuelKCB.getProductXPath()));
        js.executeScript("arguments[0].click()", aviationFuelKCB);
        WebElement keroseneCB = driver.findElement(By.xpath(TWXPath.keroseneCB.getProductXPath()));
        js.executeScript("arguments[0].click()", keroseneCB);
        WebElement dieselCB = driver.findElement(By.xpath(TWXPath.dieselCB.getProductXPath()));
        js.executeScript("arguments[0].click()", dieselCB);
        WebElement fuelCB = driver.findElement(By.xpath(TWXPath.fuelCB.getProductXPath()));
        js.executeScript("arguments[0].click()", fuelCB);
    }

    /**
     * This method checks "select all" checkbox under supply section
     */
    private void checkAllSupply() {
        WebElement supplyCB = driver.findElement(By.xpath(TWXPath.supplyCB.getProductXPath()));
        js.executeScript("arguments[0].click()", supplyCB);
    }

    /**
     * This method checks "select all" checkbox under consumption section
     */
    private void checkAllConsumption() {
        WebElement consumptionCB = driver.findElement(By.xpath(TWXPath.consumptionCB.getProductXPath()));
        js.executeScript("arguments[0].click()", consumptionCB);
    }

    /**
     * This method checks "select all" checkbox under consumption section
     */
    public void checkAllConversion() {
        WebElement conversionCB = driver.findElement(By.xpath(TWXPath.conversionCB.getProductXPath()));
        js.executeScript("arguments[0].click()", conversionCB);
    }

    /**
     * This method submits the search form
     */
    private void submitForm() {
        WebElement submitBtn = driver.findElement(By.xpath(TWXPath.submitBtn.getProductXPath()));
        js.executeScript("arguments[0].click()", submitBtn);
    }

    /**
     * This method downloads the search results into an excel file
     * @throws InterruptedException Thrown when the thread is sleeping and is interrupted
     */
    private void downloadExcel() throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(TWXPath.downloadBtn.getProductXPath())));
        driver.findElement(By.xpath(TWXPath.downloadBtn.getProductXPath())).click();
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
     * This method is used to rename the downloaded file to the file name specified in the parameter
     * @param newFileName Name for renaming downloaded file
     */
    private void renameFile(String newFileName) {
        // get the name of the most recent downloaded file
        String oldFileName = (String) js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList downloads-item').shadowRoot.querySelector('div#content #file-link').text");

        // rename downloaded file
        new File(twDataFolderPath + oldFileName).renameTo(new File(newFileName));
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
        Files.deleteIfExists(file.toPath());
    }
}
