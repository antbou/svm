package sv.menu.svm.configuration;

import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaywrightConfiguration {
    @Bean
    public Playwright playwright() {
        return Playwright.create();
    }
}
