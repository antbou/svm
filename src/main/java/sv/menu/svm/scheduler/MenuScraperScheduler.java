package sv.menu.svm.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;
import sv.menu.svm.service.ScraperService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuScraperScheduler {
    private final ScraperService scraperService;
    private final MenuRepository menurepository;

    @Scheduled(cron = "0 0 6 ? * MON") // Runs every 8 hours
    public void scrapeAndStoreMenus() {
        List<Menu> menus = scraperService.scrapeSvMenu();
        log.info("Started scraping menus. Found {} menus.", menus.size());
        if (!menus.isEmpty()) {
            log.info("Saving scraped menus to storage : {}", menus);
            try {
                menurepository.saveMenu(menus);
                log.info("Menus saved successfully.");
            } catch (DuplicateKeyException e) {
                log.warn("Duplicate key error while saving menus: {}", e.getMessage());
            }
        }
    }
}
