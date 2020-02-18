package com.birlasoft.utils;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.security.UserAndPassword;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UIUtils {
	static Logger LOGGER = Logger.getLogger(UIUtils.class);
	private static Config config;

	public static ExpectedCondition<Boolean> waitForPageLoad;

	private static final String JQUERY_ACTIVE_CONNECTIONS_QUERY = "return $.active == 0;";

	static {
		config = new Config("Framework\\Test_Config\\config.properties");

		waitForPageLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return executeScript(driver, "return document.readyState").equals("complete");
				} catch (Exception e) {
					return false;
				}
			}
		};
	}

	public static final ExpectedCondition<Boolean> EXPECT_DOC_READY_STATE = new ExpectedCondition<Boolean>() {
		String script = "if (typeof window != 'undefined') { return document.readyState;} else { return 'notready';}";

		@Override
		public Boolean apply(WebDriver driver) {
			try {
				String result = String.valueOf(executeScript(driver, script));
				return (result.equals("complete") || result.equals("interactive"));
			} catch (Exception e) {
				return false;
			}
		}
	};

	public static final ExpectedCondition<Boolean> EXPECT_NO_SPINNERS = new ExpectedCondition<Boolean>() {
		@Override
		public Boolean apply(WebDriver driver) {
			Boolean loaded = true;
			try {
				List<WebElement> spinners = driver.findElements(Constants.BYSPINNER);

				for (WebElement spinner : spinners) {
					if (spinner.isDisplayed()) {
						loaded = false;
						break;
					}
				}
			} catch (Exception e) {
				return false;
			}
			return loaded;
		}
	};

	public static Config getConfig() {
		return config;
	}

	private static DesiredCapabilities getBrowserCapabilities(BrowserTypes browserType, String driverPath)
			throws Exception {
		DesiredCapabilities dc = null;

		switch (browserType) {
		case FIREFOX:
			if (StringUtils.isNotBlank(driverPath)) {
				System.setProperty("webdriver.gecko.driver", driverPath);
			} else {
				System.setProperty("webdriver.gecko.driver", config.getPropertyValue("GeckoDriverPath"));
			}
			dc = DesiredCapabilities.firefox();
			dc.setBrowserName(BrowserType.FIREFOX);
			break;
		case CHROME:
			if (StringUtils.isNotBlank(driverPath)) {
				System.setProperty("webdriver.chrome.driver", driverPath);
			} else {
				System.setProperty("webdriver.chrome.driver", config.getPropertyValue("ChromeDriverPath"));
			}
			dc = DesiredCapabilities.chrome();
			ChromeOptions options = new ChromeOptions();
			options.addArguments("test-type");
			options.addArguments("disable-infobars");
			options.addArguments("--disable-notifications");
			dc.setBrowserName(BrowserType.CHROME);
			dc.setCapability(ChromeOptions.CAPABILITY, options);
			break;
		case IE:
			if (StringUtils.isNotBlank(driverPath)) {
				System.setProperty("webdriver.ie.driver", driverPath);
			} else {
				System.setProperty("webdriver.ie.driver", config.getPropertyValue("InternetExplorerDriverPath"));
			}
			dc = DesiredCapabilities.internetExplorer();
			dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			dc.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			dc.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
			dc.setCapability("ignoreProtectedModeSettings", true);
			dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			dc.setBrowserName(BrowserType.IE);
			break;
		default:
			break;
		}
		return dc;
	}

	private static DesiredCapabilities getBrowserCapabilities(BrowserTypes browserType, String driverPath,
			String platform) throws Exception {
		DesiredCapabilities dc = getBrowserCapabilities(browserType, driverPath);

		switch (platform.toUpperCase()) {
		case "XP":
			dc.setPlatform(Platform.XP);
			break;
		case "WINDOWS":
			dc.setPlatform(Platform.WINDOWS);
			break;
		case "VISTA":
			dc.setPlatform(Platform.VISTA);
			break;
		case "WIN8":
			dc.setPlatform(Platform.WIN8);
			break;
		case "WIN8_1":
			dc.setPlatform(Platform.WIN8_1);
			break;
		case "WIN10":
			dc.setPlatform(Platform.WIN10);
			break;
		case "LINUX":
			dc.setPlatform(Platform.LINUX);
			break;
		case "MAC":
			dc.setPlatform(Platform.MAC);
			break;
		case "ANDROID":
			dc.setPlatform(Platform.ANDROID);
			break;
		default:
			break;
		}
		return dc;
	}

	public static WebDriver createDriverInstance(BrowserTypes browserType, String driverPath) throws Exception {
		DesiredCapabilities dc = getBrowserCapabilities(browserType, driverPath);
		WebDriver driver = null;

		switch (browserType) {
		case FIREFOX:
			driver = new FirefoxDriver(dc);
			break;
		case CHROME:
			driver = new ChromeDriver(dc);
			break;
		case IE:
			driver = new InternetExplorerDriver(dc);
			break;
		case HTMLUNIT:
			driver = new HtmlUnitDriver(dc);
			break;
		default:
			break;
		}

		driver.manage().timeouts().setScriptTimeout(Long.parseLong(config.getPropertyValue("ScriptTimeoutSeconds")),
				TimeUnit.SECONDS);
		return driver;
	}

	public static WebDriver createDriverInstance(BrowserTypes browserType, String driverPath, String gridURL)
			throws Exception {
		WebDriver driver = new RemoteWebDriver(new URL(gridURL), getBrowserCapabilities(browserType, driverPath));
		driver.manage().timeouts().setScriptTimeout(Long.parseLong(config.getPropertyValue("ScriptTimeoutSeconds")),
				TimeUnit.SECONDS);
		return driver;
	}

	public static WebDriver createDriverinstance(BrowserTypes browserType, String driverPath, String gridURL,
			String platform) throws Exception {
		WebDriver driver = new RemoteWebDriver(new URL(gridURL),
				getBrowserCapabilities(browserType, driverPath, platform));
		driver.manage().timeouts().setScriptTimeout(Long.parseLong(config.getPropertyValue("ScriptTimeoutSeconds")),
				TimeUnit.SECONDS);
		return driver;
	}

	public static Object executeScript(WebDriver driver, String script, Object... args) {
		return ((JavascriptExecutor) (driver)).executeScript(script, args);
	}

	public static Object executeAsyncScript(WebDriver driver, String script, Object... args) {
		return ((JavascriptExecutor) (driver)).executeAsyncScript(script, args);
	}

	public static boolean isObjectExist(WebDriver driver, By by) {
		return (driver.findElements(by).size() > 0);
	}

	public static By getLocatorObject(String locatorType, String locatorValue) {
		By by = null;

		switch (locatorType.toUpperCase()) {
		case "XPATH":
			by = By.xpath(locatorValue);
			break;
		case "ID":
			by = By.id(locatorValue);
			break;
		case "NAME":
			by = By.name(locatorValue);
			break;
		case "TAGNAME":
		case "TAG":
			by = By.tagName(locatorValue);
			break;
		case "CLASSNAME":
		case "CLASS":
			by = By.className(locatorValue);
			break;
		case "CSSSELECTOR":
		case "CSS":
			by = By.cssSelector(locatorValue);
			break;
		case "LINKTEXT":
		case "LINK":
			by = By.linkText(locatorValue);
			break;
		case "PARTIALLINKTEXT":
			by = By.partialLinkText(locatorValue);
			break;
		default:
			LOGGER.error("Unsupported locator");
			break;
		}

		return by;
	}

	public static By getLocatorObject(String locator) {
		return getLocatorObject(locator.split(config.getPropertyValue("LocatorValueSeparator"))[0],
				locator.split(config.getPropertyValue("LocatorValueSeparator"))[1]);
	}

	public static WebElement funcFindElement(WebDriver driver, By by) throws Exception {
		return funcFindElement(driver, by, Integer.valueOf(config.getPropertyValue("AVGWAITTIME")));
	}

	public static WebElement funcFindElement(WebDriver driver, By by, int waitTime) {
		/*
		 * return getFluentWait(driver).until(new
		 * ExpectedCondition<WebElement>() {
		 * 
		 * @Override public WebElement apply(WebDriver driver) { return
		 * driver.findElement(by); } });
		 */
		return new WebDriverWait(driver, waitTime).until(ExpectedConditions.visibilityOfElementLocated(by));
	}

	public static void highLightElement(WebDriver driver, WebElement element) {
		executeScript(driver, "arguments[0].setAttribute('style', 'border: 2px solid blue;');", element);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}

		executeScript(driver, "arguments[0].setAttribute('style', 'border: 3px solid blue;');", element);
	}

	// TODO
	public static void hoverElement(WebDriver driver, WebElement element) {
	}

	public static boolean isAlertPresent(WebDriver driver) {
		boolean result = false;

		try {
			driver.switchTo().alert();
			result = true;
			driver.switchTo().defaultContent();
		} catch (NoAlertPresentException e) {
		}

		return result;
	}

	public static Alert getAlert(WebDriver driver) {
		return getAlert(driver, Integer.valueOf(config.getPropertyValue("AVGWAITTIME")));
	}

	public static void handleAlert() {
		try {
			Robot robot = new Robot();

			robot.keyPress(KeyEvent.VK_TAB);
			Thread.sleep(200);
			robot.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(200);

			robot.keyPress(KeyEvent.VK_TAB);
			Thread.sleep(200);
			robot.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(200);

			robot.keyPress(KeyEvent.VK_ENTER);
			Thread.sleep(200);
			robot.keyRelease(KeyEvent.VK_ENTER);

			Thread.sleep(5000);
		} catch (Exception e) {
			LOGGER.error("Unable to handle Robot class", e);
		}
	}

	public static Alert getAlert(WebDriver driver, int waitTime) {
		return new WebDriverWait(driver, waitTime).until(ExpectedConditions.alertIsPresent());
	}

	public static void alertAccept(WebDriver driver) throws Exception {
		getAlert(driver).accept();
		driver.switchTo().defaultContent();
	}

	public static void alertDismiss(WebDriver driver) throws Exception {
		getAlert(driver).dismiss();
		driver.switchTo().defaultContent();
	}

	public static void alertAccept(WebDriver driver, int waitTime) {
		getAlert(driver, waitTime).accept();
		driver.switchTo().defaultContent();
	}

	public static void alertDismiss(WebDriver driver, int waitTime) {
		getAlert(driver, waitTime).dismiss();
		driver.switchTo().defaultContent();
	}

	public static void authenticateAlert(WebDriver driver, String username, String password) throws Exception {
		authenticateAlert(driver, username, password, Integer.valueOf(config.getPropertyValue("AVGWAITTIME")));
	}

	public static void authenticateAlert(WebDriver driver, String username, String password, int waitTime) {
		getAlert(driver, waitTime).authenticateUsing(new UserAndPassword(username, password));
	}

	public static void setUserAndPasswordAlert(WebDriver driver, String username, String password, int waitTime) {
		getAlert(driver, waitTime).setCredentials(new UserAndPassword(username, password));
	}

	public static void setUserAndPasswordAlert(WebDriver driver, String username, String password) throws Exception {
		setUserAndPasswordAlert(driver, username, password, Integer.valueOf(config.getPropertyValue("AVGWAITTIME")));
	}

	public static void takeScreenshot(WebDriver driver, String filePath) throws IOException {
		File file = new File(filePath);

		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		FileUtils.moveFile(((TakesScreenshot) (driver)).getScreenshotAs(OutputType.FILE), file);
	}

	public static void closeAllPopUps(WebDriver driver) {
		String mainWinHanlde = driver.getWindowHandle();

		// Closing all but the main window
		for (String winHandle : driver.getWindowHandles()) {
			driver.switchTo().window(winHandle);
			if (!winHandle.equalsIgnoreCase(mainWinHanlde)) {
				driver.close();
			}
		}

		// Focusing back to main Window
		driver.switchTo().window(mainWinHanlde);
	}

	public static void selectValue(WebDriver driver, By by, String optionText) {
		optionText = optionText.trim();

		if (optionText.toLowerCase().startsWith("index=")) {
			selectValue(driver, by, "index", optionText.replaceAll("index=", ""));
		} else if (optionText.toLowerCase().startsWith("text=")) {
			selectValue(driver, by, "text", optionText.replaceAll("text=", ""));
		} else if (optionText.toLowerCase().startsWith("containstext=")) {
			selectValue(driver, by, "containstext", optionText.replaceAll("containstext=", ""));
		} else if (optionText.startsWith("value=")) {
			selectValue(driver, by, "value", optionText.replaceAll("value=", ""));
		} else {
			new Select(driver.findElement(by)).selectByVisibleText(optionText);
		}
	}

	public static void selectValue(WebDriver driver, By by, String selectBy, String option) {
		Select select = new Select(driver.findElement(by));

		switch (selectBy.toLowerCase()) {
		case "index":
			select.selectByIndex(Integer.valueOf(option));
			break;
		case "text":
			select.selectByVisibleText(option);
			break;
		case "value":
			select.selectByValue(option);
			break;
		case "containstext":
			int indexNum = 1;
			for (WebElement element : select.getOptions()) {
				if (element.getText().toLowerCase().contains(option.toLowerCase())) {
					select.selectByIndex(indexNum);
					break;
				}
				indexNum++;
			}
			break;
		default:
			break;
		}
	}

	public static void scrollWindow(WebDriver driver, String direction) {
		if (direction.equalsIgnoreCase("Up")) {
			executeScript(driver, "scroll(250, 0)");
		} else {
			executeScript(driver, "scroll(0, 250)");
		}
	}

	public static FluentWait<WebDriver> getFluentWait(WebDriver driver, Integer... waitTimes) {
		int maxWaitTime, minWaitTime;

		if (waitTimes != null) {
			maxWaitTime = waitTimes[0];
		} else {
			maxWaitTime = Integer.valueOf(config.getPropertyValue("MAXWAITTIME"));
		}

		if (waitTimes.length > 1) {
			minWaitTime = waitTimes[1];
		} else {
			minWaitTime = Integer.valueOf(config.getPropertyValue("MINWAITTIME"));
		}

		return new FluentWait<>(driver).withTimeout(maxWaitTime, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class)
				.pollingEvery(minWaitTime, TimeUnit.SECONDS);
	}

	public static boolean waitForPageLoad(WebDriver driver, int waitTime) {
		return new WebDriverWait(driver, waitTime).until(waitForPageLoad);
	}

	public static boolean waitForPageLoad(WebDriver driver) throws Exception {
		return waitForPageLoad(driver, Integer.valueOf(config.getPropertyValue("AVGWAITTIME")));
	}

	@SafeVarargs
	public static boolean waitForPageLoad(WebDriver driver, int waitTime, ExpectedCondition<Boolean>... conditions) {
		boolean isLoaded = false;

		try {
			waitUntilAjaxRequestCompletes(driver);
			Wait<WebDriver> wWait = getFluentWait(driver);

			for (ExpectedCondition<Boolean> condition : conditions) {
				isLoaded = wWait.until(condition);
				if (!isLoaded) {
					// Stop checking on first condition returning false
					break;
				}
			}
		} catch (Exception e) {
		}
		return isLoaded;
	}

	public static void waitUntilElementExists(WebDriver driver, By by, Integer... waitTimes) {
		final Wait<WebDriver> wWait = getFluentWait(driver, waitTimes);
		try {
			wWait.until(new ExpectedCondition<WebElement>() {
				@Override
				public WebElement apply(WebDriver driver) {
					return driver.findElement(by);
				}
			});
			wWait.until(ExpectedConditions.visibilityOfElementLocated(by));
		} catch (Exception e) {
		}
	}

	public static void waitUntilElementNotExists(WebDriver driver, By by, Integer... waitTimes) {
		try {
			getFluentWait(driver, waitTimes).until(
					ExpectedConditions.or(ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(by)),
							ExpectedConditions.invisibilityOfElementLocated(by)));
		} catch (Exception e) {
		}
	}

	private static void waitUntilAjaxRequestCompletes(WebDriver driver, Integer... waitTimes) {
		final Wait<WebDriver> wWait = getFluentWait(driver, waitTimes);

		wWait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return (Boolean) executeScript(driver, JQUERY_ACTIVE_CONNECTIONS_QUERY);
			}
		});
	}

	public static boolean dynamicWait(WebDriver driver, By by, int waitTime) {
		for (int i = 1; i < waitTime; i++) {
			try {
				return driver.findElement(by).isDisplayed() == true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	// Used to click on element
	public static void clickElement(By by, WebDriver driver) throws Exception {
		WebElement element = funcFindElement(driver, by);
		new Actions(driver).moveToElement(element).build().perform();
		element.click();
	}

	public static void clickElementJScript(WebDriver driver, By by) {
		WebElement element = null;

		try {
			element = funcFindElement(driver, by);
		} catch (Exception e) {
			LOGGER.error("Element located by " + by + " not found.", e);
		}

		clickElementJScript(driver, element);
	}

	public static void clickElementJScript(WebDriver driver, WebElement element) {
		//executeScript(driver, "arguments[0].scrollIntoView(true);", element);
		executeScript(driver, "arguments[0].click();", element);
	}

	public static void inputValue(WebElement element, String data) {
		element.clear();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		for (char chr : data.toCharArray()) {
			element.sendKeys(new StringBuilder(chr));
		}
	}

	public static void inputText(WebElement element, String data) {
		element.clear();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		element.sendKeys(data);
	}

	public static void inputText(WebDriver driver, By by, String data) throws Exception {
		WebElement element = funcFindElement(driver, by);
		element.clear();
		Thread.sleep(500);
		element.sendKeys(data);
	}

	public static String getText(WebElement element) {
		String text = element.getText();

		if (StringUtils.isBlank(text)) {
			text = element.getAttribute("value");
		}

		return text;
	}

	public static String generateAbsoluteXPath(WebElement childElement, String current) {
		String childTag = childElement.getTagName();

		if ("html".equals(childTag)) {
			return "/html" + current;
		}

		WebElement parentElement = childElement.findElement(By.xpath(".."));
		List<WebElement> childElements = parentElement.findElements(By.xpath("*"));

		int count = 0;

		for (WebElement webElement : childElements) {
			if (webElement.getTagName().equals(childTag)) {
				count++;
			}

			if (childElement.equals(webElement)) {
				return generateAbsoluteXPath(parentElement, "/" + childTag + "[" + count + "]" + current);
			}
		}
		return null;
	}
	
	public static void pressEscape() throws Exception {
		Robot robot=new Robot();
		robot.keyPress(KeyEvent.VK_ESCAPE);
		robot.keyRelease(KeyEvent.VK_ESCAPE);
	}

}