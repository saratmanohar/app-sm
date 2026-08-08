package com.sm.core;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverFactory {
    private static WebDriver driver;
    private static String DOWNLOAD_LOC = System.getProperty("user.dir") + "\\Downloads\\MultipleLists";

    private WebDriverFactory() {
        // Private constructor to prevent direct instantiation
    }

    public static void initializeDriver() {

        ChromeOptions options = new ChromeOptions();
        // 1. Hide the navigator.webdriver property
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // 2. Additional anti-detection arguments
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

        // 3. Optional: Set a custom download folder so the browser doesn't prompt
        System.out.println("DOWNLOAD PATH: "+DOWNLOAD_LOC);
        
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DOWNLOAD_LOC);
        prefs.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize(); // Example configuration
    }
    
    public static WebDriver getDriver() {
        return driver;
    }
    
    public static String getDownloadPath() {
        return DOWNLOAD_LOC;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null; // Reset for potential future use or to signal closure
        }
    }
}