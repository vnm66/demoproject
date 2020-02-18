package com.birlasoft.bddautomation.pages;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;

import com.birlasoft.bddautomation.stepdefinition.TestScripts;
import com.birlasoft.utils.UIUtils;

public class Common extends AbstractPage {
	static Logger LOGGER = Logger.getLogger(Common.class);

	public Common(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isPageOpen() {
		return UIUtils.isObjectExist(driver, TestScripts.getObjRep().getLocator("Common", "txtSearch"));
	}

	@Override
	public boolean validateUI() {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean switchWindow(WebDriver driver) throws InterruptedException {
		UIUtils.waitForPageLoad(driver, 10);
		Thread.sleep(5000);
		Set<String> windows = driver.getWindowHandles();

		for (String window : windows) {
			if (!window.equals(TestScripts.getMainWindowHandle())) {
				driver.switchTo().window(window);
				UIUtils.waitForPageLoad(driver, 10);
				break;
			}
		}
		LOGGER.info(windows.size() + " windows are opened.");
		return (windows.size() > 1); /* returns no. of windows */
	}

	public boolean uploadFile(String file) throws Exception {
		StringSelection selection = new StringSelection(file);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);

		LOGGER.info("file path is copied");

		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);

		Thread.sleep(2000);

		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);

		LOGGER.info("file path is entered");
		Thread.sleep(2000);
		return true;
	}

	public boolean clickSearchMenu(String screen) throws Exception {
		UIUtils.waitForPageLoad(driver, 10);
		Thread.sleep(2000);
		
		try {
			switch (screen.toUpperCase()) {
			case "FLIGHT":
				UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "linkFlight"), driver);
				break;
			case "HOTEL":
				UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "linkHotel"), driver);
				break;
			case "CAR":
				UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "linkCar"), driver);
				break;
			case "CRUISE":
				UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "linkCruise"), driver);
				break;
			case "VACATION":
				UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "linkVacation"), driver);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			LOGGER.error("Error in naviating to Search Menu");
			driver.close();
			return false;
		}
		return true;
	}

	public void clickMenuTab(String menuTab, String... subMenu) throws Exception {
		LOGGER.info("Clicking to " + menuTab + " Menu........");
		//UIUtils.waitForPageLoad(driver, 10);
		Thread.sleep(4000);
		
		Actions action = new Actions(driver);
		action.moveToElement(UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", menuTab))).build().perform();
		
		for (int i = 0; i < subMenu.length; i++) {
			if(i != subMenu.length - 1) {
				action.moveToElement(UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", subMenu[i]))).build().perform();
			} else {
				action.moveToElement(UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", subMenu[i]))).click().build().perform();
			}
		}
	}

	public void search(String data) throws Exception {
		LOGGER.info("Entering " + data + " to search box");
		UIUtils.inputText(driver, TestScripts.getObjRep().getLocator("Common", "txtSearch"), data);
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		LOGGER.info(data + " search processed");
	}

	public BasePage flightSearch(String tripType, String origin, String destination, boolean flexDate, String departDate, String returnDate, String tripClass) throws Exception {
		if(!tripType.equalsIgnoreCase("RoundTrip")) {
			UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "radioOneWay"), driver);
		}
		
		UIUtils.inputText(driver, TestScripts.getObjRep().getLocator("Common", "txtOrigin"), origin);
		Thread.sleep(2000);
		UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtOrigin")).sendKeys(Keys.ARROW_DOWN);
		Thread.sleep(2000);
		UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtOrigin")).sendKeys(Keys.ENTER);
		Thread.sleep(2000);
				
		UIUtils.inputText(driver, TestScripts.getObjRep().getLocator("Common", "txtDestination"), destination);
		Thread.sleep(3000);
		UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtDestination")).sendKeys(Keys.ARROW_DOWN);
		Thread.sleep(2000);
		UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtDestination")).sendKeys(Keys.ENTER);
		Thread.sleep(2000);
		
		//UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtDestination")).sendKeys(Keys.TAB);
		//Thread.sleep(2000);
		
		if(flexDate) {
			UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "chkFlexDate"), driver);
			//new Robot().keyPress(KeyEvent.VK_SPACE);
		} else {
			UIUtils.inputText(driver, TestScripts.getObjRep().getLocator("Common", "txtDepartDate"), departDate);
			Thread.sleep(2000);
			
			UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtDepartDate")).sendKeys(Keys.ESCAPE);
			Thread.sleep(2000);
			
			
			UIUtils.inputText(driver, TestScripts.getObjRep().getLocator("Common", "txtReturnDate"), returnDate);
			Thread.sleep(2000);
			
			UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator("Common", "txtReturnDate")).sendKeys(Keys.ESCAPE);
		}
		
		Thread.sleep(4000);
			
		UIUtils.selectValue(driver, TestScripts.getObjRep().getLocator("Common", "selectType"), "text", tripClass);
		Thread.sleep(2000);
		
		UIUtils.clickElement(TestScripts.getObjRep().getLocator("Common", "btnSearch"), driver);
		
		return PageFactory.initElements(driver, FlightSearchResults.class);
	}
	
	public void waitForProgressDone() {
		try {
			UIUtils.waitUntilElementNotExists(driver, TestScripts.getObjRep().getLocator("Common", "divProgressBar"), 20);
		} catch(Exception e) {
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}
}