package nz.co.afor.example.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.java8.En;
import nz.co.afor.view.AmazoneView;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Selenide.open;

public class AmazonSteps implements En {

    @Autowired
    AmazoneView amazoneView;


    @Given("I am on the Amazon home page")
    public void iAmOnTheAmazonHomePage() {
        open("/");
    }

    @When("I search for {string} in Amazon Product Search")
    public void iSearchFor(String searchText) {
        amazoneView.search(searchText);
    }

    @And("I Set the Price filters {double} as MinValue and {double} as MaxValue")
    public void iSetThePriceFiltersAsMinValueAndAsMaxValue(double minValue, double maxValue) {
        amazoneView.applyValueFilter(minValue, maxValue);
    }

    @Then("I should see search results")
    public void iShouldSeeSearchResults() {
        amazoneView.resultListVerification();
    }

    @When("I click on the first product")
    public void iClickOnTheFirstProduct() {
        amazoneView.selectFirstResult();
    }

    @Then("I should see the {string} page")
    public void iShouldSeeThePage(String expectedProductTitle) {
        amazoneView.resultVerification(expectedProductTitle);
    }
}
