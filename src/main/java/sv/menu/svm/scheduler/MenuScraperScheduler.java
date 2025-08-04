package sv.menu.svm.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;
import sv.menu.svm.service.ScraperService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("job")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class MenuScraperScheduler implements CommandLineRunner {
    ScraperService scraperService;
    MenuRepository menurepository;

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line runner for scraping menus");
        scrapeAndStoreMenus();
        log.info("EXECUTED : command line runner for scraping menus successfully");
        System.exit(0);
    }

    // @Scheduled(cron = "0 0 6 ? * MON")
    private void scrapeAndStoreMenus() {
        List<Menu> menus = menurepository.saveMenu(scraperService.scrapeSvMenu());
        log.info("Scheduled scraping completed. {} menus saved.", menus.size());
    }
}
