package com.birlasoft.framework;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import com.birlasoft.utils.Constants;
import com.birlasoft.utils.ExcelUtils;

public class ObjectRepository {
	static Logger LOGGER = Logger.getLogger(ObjectRepository.class);

	private static HashMap<String, String> elementMap;

	public ObjectRepository(String orPath) throws IOException {
		String elementCollection = new File(orPath).getCanonicalPath();

		LOGGER.info("======Started populating element collection map======");

		long startTime = System.currentTimeMillis();

		elementMap = new HashMap<String, String>();

		HashMap<Integer, Object> queryResult = ExcelUtils.getExcelData(elementCollection, "UIElements", false);

		for (int counter = 2; counter <= queryResult.size(); counter++) {
			String[] objInfo = String.valueOf(queryResult.get(counter)).split(Constants.COLUMNVALUE_SEPARATOR);

			String screenName = objInfo[0].trim().toUpperCase();
			String elementName = objInfo[1].trim().toUpperCase();
			String locatorType = objInfo[2].trim().toUpperCase();
			String locatorValue = objInfo[3].trim();

			if (!elementMap.containsKey(screenName + ":=" + elementName)) {
				elementMap.put(screenName + ":=" + elementName, locatorType + "##" + locatorValue);
			}
		}

		long finishTime = System.currentTimeMillis();

		LOGGER.info(
				"======Time taken to populate object repository in millis is " + (finishTime - startTime) + "======");
		LOGGER.info("======Number of object in Object Map is " + (elementMap.size() - 1) + "======");
	}

	public String[] getObject(String screenName, String fieldName) {
		String[] returnObject = new String[2];
		String elementKey = screenName.toUpperCase() + ":=" + fieldName.toUpperCase();

		if (elementMap.containsKey(elementKey)) {
			returnObject[0] = (elementMap.get(elementKey).split("##"))[0];
			returnObject[1] = (elementMap.get(elementKey).split("##"))[1];
		} else {
			return null;
		}

		return returnObject;
	}

	public By getLocator(String screenName, String fieldName) {
		By byObject = null;

		String[] getObject = getObject(screenName, fieldName);

		if (getObject != null) {
			switch (getObject[0]) {
			case "ID":
				byObject = By.id(getObject[1]);
				break;
			case "NAME":
				byObject = By.name(getObject[1]);
				break;
			case "XPATH":
				byObject = By.xpath(getObject[1]);
				break;
			case "CLASS":
			case "CLASSNAME":
				byObject = By.className(getObject[1]);
				break;
			case "TAG":
			case "TAGNAME":
				byObject = By.tagName(getObject[1]);
				break;
			case "LINK":
			case "LINKTEXT":
				byObject = By.linkText(getObject[1]);
				break;
			case "PARTIALLINKTEXT":
				byObject = By.partialLinkText(getObject[1]);
				break;
			case "CSS":
			case "CSSSELECTOR":
				byObject = By.cssSelector(getObject[1]);
				break;
			default:
				break;
			}
		}
		return byObject;
	}
}