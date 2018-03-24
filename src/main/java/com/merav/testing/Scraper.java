package com.merav.testing;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;


public class Scraper {

    private final static String GITHUB_URL = new String("https://github.com/");
    private final static String SEARCHED_STRING = new String("selenium");
    private final static int NUMBER_OF_PAGES = 5;
    private final static int NUMBER_OF_RESULTS_PER_PAGE = 10;

    private final static Logger LOGGER = Logger.getLogger(Scraper.class);

    private static WebDriver driver = null;

    private static List<WebElement> titles = null;
    private static List<String> allTitlesStrings = null;
    private static List<String> allDescriptionStrings = null;
    private static List<String> allTagsStrings = null;
    private static List<String> allTimesStrings = null;
    private static List<String> allLanguagesStrings = null;
    private static List<String> allStarsStrings = null;

    private static long start=0, finish=0, totalTime=0;

    public static void main( String[] args ) {

        Scraper scraper = new Scraper();
        scraper.getDataFromWebSiteAndStoreInDB();
    }

    public void getDataFromWebSiteAndStoreInDB(){
        titles = new ArrayList<>();

        driver  = new ChromeDriver();
        driver.get(GITHUB_URL);

        //How long it took the run the search query
        start = System.currentTimeMillis();

        //search for the desired string
        WebElement search = driver.findElement(By.className("form-control"));
        search.sendKeys(SEARCHED_STRING);
        search.sendKeys(Keys.ENTER);

        finish = System.currentTimeMillis();
        totalTime = finish - start;
        System.out.println("Total Time for search query in ms - "+totalTime);

        allTitlesStrings = new ArrayList<>();
        allDescriptionStrings = new ArrayList<>();
        allTagsStrings = new ArrayList<>();
        allTimesStrings = new ArrayList<>();
        allLanguagesStrings = new ArrayList<>();
        allStarsStrings = new ArrayList<>();

        //Page1
        getAllDataFromAPage();
        //verify each URL is valid (non 404), I don't fail the application if it is failing (hence it's verify and not assert)
        verifyTitlesURL();

        //moving to pages 2-5
        //https://github.com/search?p=2&q=selenium&type=Repositories&utf8=%E2%9C%93
        for(int page=2 ; page <= NUMBER_OF_PAGES ; page++){
            System.out.println("Page number "+page);
            System.out.println("***************");
            StringBuilder urlByPageBuilder = new StringBuilder();
            urlByPageBuilder.append("https://github.com/search?p=");
            urlByPageBuilder.append(page);
            urlByPageBuilder.append("&q=selenium&type=Repositories&utf8=%E2%9C%93");

            String urlByPage =urlByPageBuilder.toString();

            start = System.currentTimeMillis();
            driver.navigate().to(urlByPage);
            WebElement firstResultInThePage = driver.findElement(By.xpath("/html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[1]/div[1]/h3/a"));

            finish = System.currentTimeMillis();
            totalTime = finish - start;
            System.out.println("Total Time for page "+page+" to load in ms - "+totalTime);

            getAllDataFromAPage();
            verifyTitlesURL();
        }
        writeDataInDB();
        reportPerformanceMeasurements();

        driver.close();
        driver.quit();
    }
    private void waitForPageLoad() {

        Wait<WebDriver> wait = new WebDriverWait(driver, 30);
        wait.until(new Function<WebDriver, Boolean>() {
            public Boolean apply(WebDriver driver) {
                System.out.println("Current Window State       : "
                        + String.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState")));
                return String
                        .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                        .equals("complete");
            }
        });
    }
    private static void getAllDataFromAPage(){

        getTitles();
        getDescription();
        getTags();
        getTimes();
        getLanguages();
        getStars();
    }

    private static void getStars(){
        String xpathOne = "html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[";
        String xpathTwo = "]/div[3]/a";
        List<WebElement> webElementList;

        for(int row = 1; row<= NUMBER_OF_RESULTS_PER_PAGE; row++){
            String xpath = xpathOne+row+xpathTwo;
            webElementList = driver.findElements(By.xpath(xpath));
            for(WebElement webElement : webElementList){
                if((webElement.getText() != "NULL")||(webElement.getText() != "null")) {
                    allStarsStrings.add(webElement.getText()); }
                else {
                    allStarsStrings.add("");}
            }
        }
    }
    private static void getLanguages(){
        ///html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[..]/div[2]

        String xpathOne = "html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[";
        String xpathTwo = "]/div[2]";
        List<WebElement> webElementList;

        for(int row = 1; row<= NUMBER_OF_RESULTS_PER_PAGE; row++){
            String xpath = xpathOne+row+xpathTwo;
            webElementList = driver.findElements(By.xpath(xpath));
            for(WebElement webElement : webElementList){
                if((webElement.getText() != "NULL")||(webElement.getText() != "null")) {
                    allLanguagesStrings.add(webElement.getText()); }
                else {
                    allLanguagesStrings.add("");}
            }
        }
    }
    private static void getTimes(){
        ///html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[...]/div[1]/div/p
        String partOneXpath="html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[";
        String partTwoXpath="]/div[1]/div/p";

        List<WebElement> webElementList;
        for(int row = 1; row<= NUMBER_OF_RESULTS_PER_PAGE; row++){
            String xpath=partOneXpath+row+partTwoXpath;
            webElementList = driver.findElements(By.xpath(xpath));
            if(webElementList.size() > 0){//it has license information in addition to the time or issues to solve
                for(WebElement webElement : webElementList){
                    if(webElement.getText().startsWith("Updated")) {
                        allTimesStrings.add(webElement.getText());
                    }
                }
            }
            else  { //nothing
                allTimesStrings.add("");
            }
        }
    }
    private static void getTitles(){
        //html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[..]/div[1]/h3/a
        //TODO -change to xpath
        titles = driver.findElements(By.className("v-align-middle"));
        for(WebElement title : titles){
                allTitlesStrings.add(title.getText());
              //  System.out.println("Title "+title.getText());
        }
    }
    private static void getDescription(){
        //xpath = "/html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[..]/div[1]/p"

        String xpathOne = "/html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[";
        String xpathTwo = "]/div[1]/p";
        List<WebElement> webElementList;

        for(int row = 1; row<= NUMBER_OF_RESULTS_PER_PAGE; row++){
            String xpath = xpathOne+row+xpathTwo;
            webElementList = driver.findElements(By.xpath(xpath));
            if(webElementList.size()>0) {//I got an answer
                for(WebElement webElement : webElementList){
                    if((webElement.getText() != "")||(webElement.getText() != "null")) {
                        allDescriptionStrings.add(webElement.getText()); }
                }
            }
            else{
                allDescriptionStrings.add("");}
            }
    }
    private static void getTags(){
        String partOneXpath="/html/body/div[4]/div[1]/div/div[1]/div[2]/div/ul/div[";
        String partTwoXpath="]/div[1]/div[1]/a[..]";
        StringBuilder tagsResultsBuilder = null;
        List<WebElement> webElementList;
        for(int row = 1; row<= NUMBER_OF_RESULTS_PER_PAGE; row++){
            tagsResultsBuilder = new StringBuilder();
            String xpath=partOneXpath+row+partTwoXpath;
            webElementList = driver.findElements(By.xpath(xpath));
            for(WebElement webElement : webElementList){
                if((webElement.getText() != "NULL")||(webElement.getText() != "null")) {
                    tagsResultsBuilder.append(webElement.getText());
                    tagsResultsBuilder.append(" ");
                   // LOGGER.info("tags " + webElement.getText());
                }
            }
            allTagsStrings.add(tagsResultsBuilder.toString());
        }
    }
    private static void writeDataInDB(){

        WriteResultsToDB writeResultsToDB = new WriteResultsToDB();
        writeResultsToDB.writeToMySQL();
        System.out.println("**preaparing data to write ***");

        System.out.println("number of titles "+allTitlesStrings.size());
        for(String str: allTitlesStrings){
            System.out.println("Title "+str);
        }

        System.out.println("number of descrpition "+allDescriptionStrings.size());
        for(String str: allDescriptionStrings){
    //        System.out.println("Description "+str);
        }

        System.out.println("number of tags "+allTagsStrings.size());
        for(String str: allTagsStrings){
     //       System.out.println("Tags "+str);
        }

        System.out.println("number of times "+allTimesStrings.size());
        for(String str: allTimesStrings){
      //      System.out.println("times "+str);
        }

        System.out.println("number of langs "+allLanguagesStrings.size());
        for(String str: allLanguagesStrings){
     //       System.out.println("langs "+str);
        }

        System.out.println("number of stars "+allStarsStrings.size());
        for(String str: allStarsStrings) {
            //       System.out.println("stars "+str);
        }
    }


    private static void verifyTitlesURL() {
        Iterator<WebElement> it = titles.iterator();
        String url = "";
        HttpURLConnection huc = null;
        int respCode = 200;

        while (it.hasNext()) {

            url = it.next().getAttribute("href");

            if ((url == null)||(url.length() == 0)) { //null or empty
                LOGGER.error("URL is not initialized correctly or empty");
                continue;
            }

            try {
                huc = (HttpURLConnection) (new URL(url).openConnection());

                huc.setRequestMethod("HEAD");

                huc.connect();

                respCode = huc.getResponseCode();

                if (respCode >= 400) {
                    System.out.println(url + " is a broken link");
                } else {
                    System.out.println(url + " is a valid link");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }

    public static void reportPerformanceMeasurements(){

    }

}
