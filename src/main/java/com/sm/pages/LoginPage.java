package com.sm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.sm.core.WebDriverFactory;

public class LoginPage {

	private WebDriver driver = WebDriverFactory.getDriver();

    public void userIsOnTheLoginPage() {
        driver.get("https://www.saucedemo.com/"); // Example login page
    }

    public void userEntersValidCredentials(String username, String password) {
        driver.findElement(By.id("user-name")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
    }

    public void clicksTheLoginButton() {
        driver.findElement(By.id("login-button")).click();
    }

    public boolean isHomePageDisplayed() {
        return driver.getCurrentUrl().contains("inventory.html");
    }
}
