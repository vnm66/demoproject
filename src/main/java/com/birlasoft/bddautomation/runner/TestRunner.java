package com.birlasoft.bddautomation.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(features = "src/test/resources/",
glue = {"com.birlasoft.bddautomation.stepdefinition" },
plugin = {"pretty", "html:target/test-report","json:target/cucumber-report.json", },
tags = {"@FlightSearchScenario, @SearchValidation"})

public class TestRunner extends AbstractTestNGCucumberTests {

}