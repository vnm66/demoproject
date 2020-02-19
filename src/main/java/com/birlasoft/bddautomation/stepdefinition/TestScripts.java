package com.birlasoft.bddautomation.stepdefinition;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.birlasoft.bddautomation.pages.Common;
import com.birlasoft.bddautomation.pages.FlightSearchResults;
import com.birlasoft.framework.ObjectRepository;
import com.birlasoft.utils.BrowserTypes;
import com.birlasoft.utils.Config;
import com.birlasoft.utils.ExcelUtils;
import com.birlasoft.utils.UIUtils;
import com.relevantcodes.extentreports.DisplayOrder;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class TestScripts {
	static Logger LOGGER = Logger.getLogger(TestScripts.class);

	private static Common common;
	private static FlightSearchResults flightResults;

	private static Config config;
	private static String configWorkbook;
	private static ObjectRepository objRep;
	private static String appBaseURL;

	private static WebDriver driver;
	private static String mainWindowHandle;
	private static ExtentReports report;
	ExtentTest extentTest;

	static {
		config = new Config("Framework\\Test_Config\\config.properties");

		try {
			configWorkbook = new File("Framework\\Test_config\\Config.xls").getCanonicalPath();
		} catch (IOException e) {
			LOGGER.error("Unable to find Config workbook", e);
		}

		report = new ExtentReports("Reports\\testReport.html", true, DisplayOrder.NEWEST_FIRST);
		PropertyConfigurator.configure("log4j.properties");

		// Object Repository Population
		try {
			objRep = new ObjectRepository("Framework\\OR\\ObjectRepository.xls");
		} catch (IOException e) {
			LOGGER.error("Unable to load OR", e);
		}

		try {
			appBaseURL = String.valueOf(ExcelUtils.getCellValue(configWorkbook, "Config", "Value", "Key=AppBaseURL"))
					.trim();
		} catch (IOException e) {
			LOGGER.error("Unable to set app base URL", e);
		}
	}

	public static WebDriver getDriver() {
		return driver;
	}

	public static String getMainWindowHandle() {
		return mainWindowHandle;
	}

	public static ObjectRepository getObjRep() {
		return objRep;
	}

	public static String getAppBaseURL() {
		return appBaseURL;
	}

	public static Config getConfig() {
		return config;
	}

	public static String getConfigWorkbook() {
		return configWorkbook;
	}

	@Before
	public void suiteSetup() {
		// Instantiate driver
		try {
			Thread.sleep(2000);
			driver = driverInstantiation(config.getPropertyValue("Browser").toUpperCase());

			mainWindowHandle = driver.getWindowHandle();
		} catch (Exception e) {
			LOGGER.error("Unable to set driver. Exception is ", e);
		}
	}

	public static WebDriver driverInstantiation(String browserName) throws Exception {
		WebDriver driver = UIUtils.createDriverInstance(BrowserTypes.valueOf(browserName), "");

		driver.navigate().to(appBaseURL);
		driver.manage().window().maximize();
		UIUtils.waitForPageLoad(driver);

		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_0);
			robot.keyRelease(KeyEvent.VK_CONTROL);
		} catch (AWTException e) {
		}

		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);

		return driver;
	}

	@Given("^user is launching the browser & navigates to app URL$")
	public void launchingUALApp() throws Exception {
		extentTest = report.startTest("Flight Search Test");

		common = PageFactory.initElements(driver, Common.class);
		extentTest.log(LogStatus.INFO, "Launching the Application");

		try {
			Assert.assertTrue(common.isPageOpen());
			extentTest.log(LogStatus.PASS, "Common page is successfully validated");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in loading Common Page");
			throw new Exception("Error in loading Common Page " + e.getStackTrace().toString());
		}
	}

	@Given("^user is navigating to app URL$")
	public void launchingUALApp1() throws Exception {
		extentTest = report.startTest("Search Validation Test");

		common = PageFactory.initElements(driver, Common.class);
		extentTest.log(LogStatus.INFO, "Launching the Application");

		try {
			Assert.assertTrue(common.isPageOpen());
			extentTest.log(LogStatus.PASS, "Common page is successfully validated");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in loading Common Page");
			throw new Exception("Error in loading Common Page " + e.getStackTrace().toString());
		}
	}

	@When("^user click on Flight Search menu$")
	public void clickFlightSearchMenu() throws Exception {
		extentTest.log(LogStatus.INFO, "User clicks on Flight Search Menu");

		try {
			Assert.assertTrue(common.clickSearchMenu("Flight"));
			extentTest.log(LogStatus.PASS, "Successfully clicked on Flight Search Menu");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in clicking Flight Search Menu");
			throw new Exception("Error in clicking Flight Search Menu " + e.getStackTrace().toString());
		}
	}

	@Then("^user validate Flight Search form$")
	public void validateFlightSearchForm() throws Exception {
		extentTest.log(LogStatus.INFO, "validating Flight Search Form");
		try {
			Assert.assertTrue(common.isElementExists("Common", "radioRoundTrip"));
			extentTest.log(LogStatus.PASS, "Round Trip Radio button Exists");

			Assert.assertTrue(common.isElementExists("Common", "radioOneWay"));
			extentTest.log(LogStatus.PASS, "One Way Radio button Exists");

			Assert.assertTrue(common.isElementExists("Common", "btnMultiCity"));
			extentTest.log(LogStatus.PASS, "Multi City button Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtOrigin"));
			extentTest.log(LogStatus.PASS, "Origin text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtDestination"));
			extentTest.log(LogStatus.PASS, "Destination text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "chkFlexDate"));
			extentTest.log(LogStatus.PASS, "Flexi Date check box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtDepartDate"));
			extentTest.log(LogStatus.PASS, "Departure Date text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtReturnDate"));
			extentTest.log(LogStatus.PASS, "Return Date text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "selectType"));
			extentTest.log(LogStatus.PASS, "Flight Class drop down Exists");

			Assert.assertTrue(common.isElementExists("Common", "btnSearch"));
			extentTest.log(LogStatus.PASS, "Search button Exists");

			extentTest.log(LogStatus.PASS, "Flight search form is successfully validated");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in validating Flight Search Form");
			throw new Exception("Error in vaidating Flight Search Page " + e.getStackTrace().toString());
		}
	}

	@When("^user search flights for criteria$")
	public void searchFlights(DataTable formData) throws Exception {
		extentTest.log(LogStatus.INFO, "Search Flights");
		List<List<String>> data = formData.raw();

		for (List<String> searchData : data) {
			try {
				Thread.sleep(4000);
				flightResults = (FlightSearchResults) common.flightSearch(searchData.get(0), searchData.get(1),
						searchData.get(2), Boolean.parseBoolean(searchData.get(3)), searchData.get(4),
						searchData.get(5), searchData.get(6));

				common.waitForProgressDone();

				Assert.assertNotNull(flightResults);
				Thread.sleep(15000);
				extentTest.log(LogStatus.PASS, "Successfully searched flight");
			} catch (Exception e) {
				extentTest.log(LogStatus.FAIL, "Error in searching flight");
				throw new Exception("Error in searching flight " + e.getStackTrace().toString());
			}
		}
	}

	@Then("^user lands up in Search Result Page$")
	public void validateSearchResult() throws Exception {
		extentTest.log(LogStatus.INFO, "Validating the Flight Search Result Page");

		try {
			Assert.assertTrue(flightResults.isPageOpen());
			extentTest.log(LogStatus.PASS, "Flight Search Result page is successfully loaded");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in validating Flight Search Result page");
			throw new Exception("Error in validating Flight Search Result page " + e.getStackTrace().toString());
		}
	}

	@Then("^user should see atleast one search result$")
	public void validateSearchCount() throws Exception {
		extentTest.log(LogStatus.INFO, "Validating the Search Results count");

		try {
			Assert.assertTrue(flightResults.getnumberOfSearchResults() >= 1);
			extentTest.log(LogStatus.PASS, "Atleast one search result appear");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "No search result exist");
			throw new Exception("Error in validating Search Results count " + e.getStackTrace().toString());
		}
	}

	@When("^user click on Hotel Search menu$")
	public void clickHotelSearchMenu() throws Exception {
		extentTest.log(LogStatus.INFO, "User clicks on Hotel Search Menu");

		try {
			Assert.assertTrue(common.clickSearchMenu("Hotel"));
			extentTest.log(LogStatus.PASS, "Successfully clicked on Hotel Search Menu");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in clicking Hotel Search Menu");
			throw new Exception("Error in clicking Hotel Search Menu " + e.getStackTrace().toString());
		}
	}

	@Then("^user validate Hotel Search form$")
	public void validateHotelSearchForm() throws Exception {
		extentTest.log(LogStatus.INFO, "validating Hotel Search Form");
		try {
			Assert.assertTrue(common.isElementExists("Common", "txtHotelDestination"));
			extentTest.log(LogStatus.PASS, "Destination text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtCheckInDate"));
			extentTest.log(LogStatus.PASS, "Check-in Date text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtCheckOutDate"));
			extentTest.log(LogStatus.PASS, "Check-out Date text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "selectRoomCount"));
			extentTest.log(LogStatus.PASS, "Room counter drop-down Exists");

			Assert.assertTrue(common.isElementExists("Common", "btnHotelSearch"));
			extentTest.log(LogStatus.PASS, "Search button Exists");

			extentTest.log(LogStatus.PASS, "Hotel search form is successfully validated");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in validating Hotel Search Form");
			throw new Exception("Error in vaidating Hotel Search Page " + e.getStackTrace().toString());
		}
	}

	@When("^user click on Car Search menu$")
	public void clickCarSearchMenu() throws Exception {
		extentTest.log(LogStatus.INFO, "User clicks on Car Search Menu");

		try {
			Assert.assertTrue(common.clickSearchMenu("Car"));
			extentTest.log(LogStatus.PASS, "Successfully clicked on Car Search Menu");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in clicking Car Search Menu");
			throw new Exception("Error in clicking Car Search Menu " + e.getStackTrace().toString());
		}
	}

	@Then("^user validate Car Search form$")
	public void validateCarSearchForm() throws Exception {
		extentTest.log(LogStatus.INFO, "validating Car Search Form");
		try {
			Assert.assertTrue(common.isElementExists("Common", "txtCarDestination"));
			extentTest.log(LogStatus.PASS, "Destination text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtDropOffLocation"));
			extentTest.log(LogStatus.PASS, "Drop Location text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtPickDate"));
			extentTest.log(LogStatus.PASS, "Pick Date text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "txtDropDate"));
			extentTest.log(LogStatus.PASS, "Drop Date text box Exists");

			Assert.assertTrue(common.isElementExists("Common", "selectPickTime"));
			extentTest.log(LogStatus.PASS, "Pick Time drop-down Exists");

			Assert.assertTrue(common.isElementExists("Common", "selectDropTime"));
			extentTest.log(LogStatus.PASS, "Drop Time drop-down Exists");

			Assert.assertTrue(common.isElementExists("Common", "selectCarType"));
			extentTest.log(LogStatus.PASS, "Car Type drop-down Exists");

			Assert.assertTrue(common.isElementExists("Common", "btnCarSearch"));
			extentTest.log(LogStatus.PASS, "Search button Exists");

			extentTest.log(LogStatus.PASS, "Car search form is successfully validated");
		} catch (Exception e) {
			extentTest.log(LogStatus.FAIL, "Error in validating Car Search Form");
			throw new Exception("Error in vaidating Car Search Page " + e.getStackTrace().toString());
		}
	}

	@After
	public void testTearDown() {
		driver.close();
		report.endTest(extentTest);
	}
}