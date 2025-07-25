package sv.menu.svm.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.service.MenuStorageService;
import sv.menu.svm.service.ScraperService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuScraperScheduler {
    private final ScraperService scraperService;
    private final MenuStorageService menuStorageService;

    @Scheduled(cron = "0 0 0/8 * * ?") // Runs every 8 hours
    public void scrapeAndStoreMenus() {
        List<Menu> menus = scraperService.scrapeSvMenu();
        log.info("Started scraping menus. Found {} menus.", menus.size());
        if (!menus.isEmpty()) {
            log.info("Saving scraped menus to storage : {}", menus);
            menuStorageService.saveMenu(menus);
        }
    }
}
