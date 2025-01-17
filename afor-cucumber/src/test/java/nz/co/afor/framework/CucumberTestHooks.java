package nz.co.afor.framework;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import nz.co.afor.reports.HTML;

import java.io.IOException;

import static java.lang.String.format;

public class CucumberTestHooks {
    @After
    public void after(Scenario scenario) throws IOException {
        if (scenario.getSourceTagNames().contains("@attach")) {
            scenario.attach(HTML.class.getResourceAsStream("formatter/aforLogoLargeGradient.png").readAllBytes(), "png", scenario.getId());
        }
        if (scenario.getSourceTagNames().contains("@log")) {
            scenario.log(format("log for scenario '%s'", scenario.getId()));
        }
    }
}
