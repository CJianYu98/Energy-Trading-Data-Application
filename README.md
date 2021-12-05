# Energy-Trading-Data-Application
Built on: 
- Java Version: 16
- Spring Boot 2.5.4

## Set up  
(1) Clone Energy-Trading-Data-Application repository onto your local machine  

(2) Make sure that your machine has JDK version 16 installed on it. If not, download JDK 16 from the official Java Oracle website (https://docs.oracle.com/en/java/javase/16/install/installation-jdk-microsoft-windows-platforms.html#GUID-DAF345BA-B3E7-4CF2-B87A-B6662D691840)

(3) Start WAMP / MAMP and navigate to phpMyAdmin Menu  

(4) Under the User Accounts tab, Create a new user with the following credentials  
- Username: is442oop  
- Password: is442OOPp@ssword  

(5) Once you have created a user, create a new database schema with the following details  
- Database schema name: energy_data_trading

(6) Download chromedriver for your Chrome version from this website (https://chromedriver.chromium.org/downloads)

(7) Navigate to the driver folder at "/src/main/java/com/smu/energydatatradingapp/utils/crawler/driver" and remove any existing files in the folder. Copy your chromedriver executable into the folder.

### Choose either:
### (a) Import database
(8) Navigate to "Import" tab

(9) Upload "deploy.sql" file located in "/sql" folder to import

### (b) Pre-load database using web crawler
(8) Navigate to file "/src/main/java/com/smu/energydatatradingapp/service/DataUpdateService.java"

(9) Un-comment line 72

## Running the application (For Mac users)
(1) Open "Terminal" on Mac

(2) Navigate to root directory of Energy-Trading-Data-Application

(3) Run the application with this command: sh compile_and_run.sh

## Running the application (For Windows users)
(1) Navigate to src/main/java/com/smu/energydatatradingapp/utils/crawler

(2) In IndoCrawler.java file, make the following edits:
- Comment out line 30 & 31 
- Un-comment line 32 & 33

(3) In the TWCrawler java file, make the same edits:
- Comment out line 27 & 28
- Un-comment line 29 & 30

(4) Check if 'JAVA_HOME' environment variable is set. The environment variable should be pointing to your Java installation directory. You may follow this link to check and set the environment variable (https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html)

(5) Navigate to the root directory of Energy-Trading-Data-Application

(6) Run the application by double-clicking on compile_and_run.bat
