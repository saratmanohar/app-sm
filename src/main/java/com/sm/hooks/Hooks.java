package com.sm.hooks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.sm.core.WebDriverFactory;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class Hooks {

	@BeforeAll
	public static void initialize() throws FileNotFoundException {
		PrintStream fileOut = new PrintStream(new FileOutputStream("log.txt", false));
        // Redirect standard output and error streams
        System.setOut(fileOut);
        System.setErr(fileOut);
		WebDriverFactory.initializeDriver();
	}

	@AfterAll
	public static void tearDown() {
		WebDriverFactory.quitDriver();
	}
}
