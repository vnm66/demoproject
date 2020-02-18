package com.birlasoft.utils;

import org.openqa.selenium.By;

public class Constants {
	public static final String CONDITION_SEPARATOR = ";";
	public static final String CONDITIONVALUE_SEPARATOR = "=";
	public static final String COLUMN_SEPARATOR = "=";
	public static final String COLUMNVALUE_SEPARATOR = "::::";
	
	// Change according to spinner in application
	public static final By BYSPINNER = By.xpath("//div[@class='spinner-container']//div[@role='progressbar']");
	
	public static final String REPORT_PATH = "Framework\\Test_Results\\";
}