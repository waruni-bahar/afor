import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Created by Matt Belcher on 12/10/2015.
 */
@RunWith(Cucumber.class)
@ContextConfiguration("classpath:cucumber.xml")
@CucumberContextConfiguration
@CucumberOptions(plugin = {"pretty", "nz.co.afor.reports.HTML:target/afor"})
public class RunTest {
}
