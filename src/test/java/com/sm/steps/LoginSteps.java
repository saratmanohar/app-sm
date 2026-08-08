package com.sm.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import com.sm.pages.DownloadMP3;
import com.sm.pages.LoginPage;

import static org.junit.Assert.assertTrue;

public class LoginSteps {

	private LoginPage login;
	public LoginSteps() {
		login = new LoginPage();
	}

    @Given("the user is on the login page")
    public void userIsOnTheLoginPage() {
        login.userIsOnTheLoginPage();
    }

    @When("the user enters username {string} and password {string}")
    public void userEntersValidCredentials(String username, String password) {
        login.userEntersValidCredentials(username, password);
    }

    @When("clicks the login button")
    public void clicksTheLoginButton() {
    	login.clicksTheLoginButton();
    }

    @Then("the user should be redirected to the homepage")
    public void userShouldBeRedirectedToTheHomePage() {
        assertTrue(login.isHomePageDisplayed());
    }
    
    @Then("MP3 Download Step")
    public void downloadMP3() throws Exception {
       new DownloadMP3().downloadMP3();
    }
    
}
