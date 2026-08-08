# app-sm

## Overview
A small Java Maven project containing end-to-end tests implemented with Cucumber (feature files) and Selenium WebDriver. Tests are executed via a JUnit Cucumber runner (com.sm.runner.TestRunner). The main test automation responsibilities live under src/test (features, step definitions, runner), and Selenium support utilities are under src/main/java/com/sm/core and com/sm/utils.

## High-level architecture
- Feature files: src/test/resources/features/*.feature (Cucumber scenarios)
- Step definitions: com.sm.steps (src/test/java/com/sm/steps)
- Test runner: com.sm.runner.TestRunner (runs all feature files using Cucumber+JUnit)
- Hooks: com.sm.hooks.Hooks (BeforeAll/AfterAll lifecycle; redirects stdout/stderr to log.txt)
- WebDriver factory: com.sm.core.WebDriverFactory (ChromeOptions configuration, download path)
- Utilities: com.sm.utils (MP3 helpers, fuzzy matcher, file handling)

## Prerequisites
- Java JDK 11+ (install and set JAVA_HOME)
- Apache Maven 3.6+
- Google Chrome installed (matching ChromeDriver)
- Chromedriver on PATH (or use WebDriverManager — recommended for convenience)

Optional helper (recommended): add WebDriverManager (io.github.bonigarcia:webdrivermanager) to pom to auto-manage driver binaries.

## Build & test commands
- Run full test suite (default):
  mvn test

- Run the JUnit/Cucumber runner class only:
  mvn -Dtest=com.sm.runner.TestRunner test

- Run scenarios by Cucumber tag (example using @smoke):
  mvn -Dcucumber.filter.tags="@smoke" test

- Run a single feature file (example):
  mvn -Dcucumber.options="--glue com.sm.steps src/test/resources/features/login.feature" test

- Run a single scenario by name (example):
  mvn -Dcucumber.options="--name \"valid login\"" test

Notes:
- The project uses the Cucumber JUnit runner. Use the TestRunner class when invoking tests from IDEs or CI.
- If Maven cannot find Chromedriver, either install Chromedriver and add it to PATH or add WebDriverManager to the project.

## Headless / CI guidance
These tests control a real Chrome browser. For CI or headless runs:
- Prefer WebDriverManager and add a headless option in WebDriverFactory (example: options.addArguments("--headless=new")).
- On GitHub Actions use a runner with a display (ubuntu-latest) and either run Chrome headless or use xvfb if needed.

Example GitHub Actions snippet (conceptual):
- uses: actions/checkout@v4
- name: Set up JDK
  uses: actions/setup-java@v4
  with: java-version: '11'
- name: Run tests
  run: mvn -B test
(Adjust to install Chrome / chromedriver or use WebDriverManager.)

## Logging & reports
- Hooks redirect stdout/stderr to `log.txt` in the repo root by default (see com.sm.hooks.Hooks).
- Cucumber HTML report: `target/cucumber-reports.html` (TestRunner plugin configured to write HTML report).
- If debugging tests locally, temporarily comment out the System.setOut/System.setErr redirection in Hooks to see console logs in real time.

## Key conventions and project notes
- Feature files live in `src/test/resources/features` — keep human-readable scenario text there and map steps to `com.sm.steps`.
- The WebDriver download path is set to `user.dir\Downloads\MultipleLists` by default; adjust `WebDriverFactory.DOWNLOAD_LOC` if needed.
- Tests assume ChromeDriver is usable via PATH. The project currently constructs ChromeDriver directly — adding WebDriverManager removes the manual Chromedriver requirement.
- Hooks use @BeforeAll/@AfterAll from io.cucumber.java; long-running initialization happens once per test run.

## Troubleshooting
- Browser fails to start: ensure Chromedriver version matches installed Chrome or add WebDriverManager to pom.
- Tests hang: check `log.txt` for redirected stdout/stderr and ensure driver.quit() ran (Hooks.tearDown).
- Reports not generated: ensure TestRunner plugin config is active and `target/` is writable.

## Changing behavior
- To enable headless runs quickly, modify `WebDriverFactory.initializeDriver()` to add `options.addArguments("--headless=new")` when an env var (e.g., HEADLESS=true) is present.
- To stop redirection of console output during development, remove or comment out the System.setOut/System.setErr lines in `com.sm.hooks.Hooks`.

## Where to look next
- Features: src/test/resources/features
- Step defs: src/test/java/com/sm/steps
- Driver setup: src/main/java/com/sm/core/WebDriverFactory.java
- Hooks: src/main/java/com/sm/hooks/Hooks.java

If you'd like, I can also:
- Add a WebDriverManager dependency to pom.xml and a conditional headless flag in WebDriverFactory
- Add a GitHub Actions workflow example in .github/workflows to run tests on push

---
