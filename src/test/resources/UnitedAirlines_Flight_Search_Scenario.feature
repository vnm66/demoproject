Feature: UnitedAirlines Flight Search Scenario
@FlightSearchScenario
Scenario: United Flight Search Scenario
	Given user is launching the browser & navigates to app URL
	When user click on Flight Search menu
	Then user validate Flight Search form
	When user search flights for criteria
		|RoundTrip|Delhi, IN (DEL)|Tampa, FL, US (TPA)|false|Nov 27, 2017|Dec 9, 2017|Economy|
	Then user lands up in Search Result Page
	Then user should see atleast one search result
	
    