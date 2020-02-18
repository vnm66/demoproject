Feature: United Search Validations
@SearchValidation
Scenario: United Search Validations
	Given user is navigating to app URL
	When user click on Flight Search menu
	Then user validate Flight Search form
	When user click on Hotel Search menu
	Then user validate Hotel Search form
	When user click on Car Search menu
	Then user validate Car Search form