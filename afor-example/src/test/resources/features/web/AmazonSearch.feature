Feature: Search on Amazon

  As a customer
  I want to be able to search on Amazon
  So that I can find information in areas of interest

  @Amazon
  Scenario: Search for an example piece of text
    Given I am on the Amazon home page
    When I search for "iphone 11 case" in Amazon Product Search
    And I Set the Price filters 100.0 as MinValue and 1000.0 as MaxValue
    Then I should see search results
    When I click on the first product
    Then I should see the "Apple" page