package com.birlasoft.bddautomation.pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.birlasoft.bddautomation.stepdefinition.TestScripts;
import com.birlasoft.utils.UIUtils;

public class FlightSearchResults extends AbstractPage {
	static Logger LOGGER = Logger.getLogger(Common.class);

	public FlightSearchResults(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isPageOpen() {
		return UIUtils.isObjectExist(driver, TestScripts.getObjRep().getLocator("FlightSearchResults", "lblSelectYourDepartures"));
	}

	@Override
	public boolean validateUI() {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getnumberOfSearchResults() {
		return driver.findElements(TestScripts.getObjRep().getLocator("FlightSearchResults", "collEconomySelect")).size();
	}	
}