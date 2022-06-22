package nz.co.afor.view;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.springframework.stereotype.Component;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by Matt Belcher on 10/01/2016.
 */
@Component
public class AmazoneView {

    public SelenideElement getSearchField() {
        return $(By.id("twotabsearchtextbox"));
    }

    public SelenideElement getSearchButton() {
        return $(By.id("nav-search-submit-button"));
    }

    public SelenideElement getMinValueFilter() {
        return $(By.id("low-price"));
    }

    public SelenideElement getMaxValueFilter() {
        return $(By.id("high-price"));
    }

    public SelenideElement getApplyFilterButton() {
        return $(By.id("a-autoid-1"));
    }

    public SelenideElement getSearchResultView() {
        return $x("//*[@data-component-type='s-search-results']");
    }

    public SelenideElement getFirstResult() {
        return $x("//*[@id='search']//div[@data-index='1']//a");
    }

    public SelenideElement getProductTitle() {
        return $(By.id("productTitle"));
    }


    public void search(String searchString) {
        getSearchButton().should(exist);
        getSearchField().click();
        getSearchField().should(be(focused));
        getSearchField().setValue(searchString);
        getSearchButton().click();
    }

    public void applyValueFilter(double minValue, double maxValue) {
        getMinValueFilter().should(exist);
        getMinValueFilter().click();
        getMinValueFilter().should(be(focused));
        getMinValueFilter().setValue(minValue + "");

        getMaxValueFilter().should(exist);
        getMaxValueFilter().click();
        getMaxValueFilter().should(be(focused));
        getMaxValueFilter().setValue(maxValue + "");

        getApplyFilterButton().should(exist);
        getApplyFilterButton().scrollIntoView(true).click();
    }

    public void selectFirstResult() {
        getFirstResult().should(exist);
        getFirstResult().click();
    }

    public void resultListVerification() {
        getSearchResultView().should(exist);
    }

    public void resultVerification(String expectedText) {
        getProductTitle().should(exist);
        assertTrue(getProductTitle().text().contains(expectedText));
    }
}
