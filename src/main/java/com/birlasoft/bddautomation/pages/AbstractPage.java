package com.birlasoft.bddautomation.pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.birlasoft.bddautomation.stepdefinition.TestScripts;
import com.birlasoft.utils.UIUtils;

public abstract class AbstractPage implements BasePage {
	public WebDriver driver;
	static Logger LOGGER = Logger.getLogger(AbstractPage.class);

	public AbstractPage(WebDriver driver) {
		this.driver = driver;
	}

	public boolean isElementExists(String screenName, String elementKey) {
		LOGGER.info("verifing the presence of " + elementKey + " in " + screenName + " page..");
		return UIUtils.isObjectExist(driver, TestScripts.getObjRep().getLocator(screenName, elementKey));
	}

	public boolean isElementEnabled(String screenName, String elementKey) {
		WebElement element = null;

		try {
			element = UIUtils.funcFindElement(driver, TestScripts.getObjRep().getLocator(screenName, elementKey));
		} catch (Exception e) {
		}

		return element.isEnabled();
	}

	public abstract boolean isPageOpen();

	public abstract boolean validateUI();
}