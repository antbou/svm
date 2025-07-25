package sv.menu.svm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;
import sv.menu.svm.scheduler.MenuScraperScheduler;
import sv.menu.svm.service.ScraperService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SvmController {
    private final ScraperService scraper;
    private final MenuRepository menuStorageService;
    private final MenuScraperScheduler scraperService;

    @GetMapping("/menus")
    public List<Menu> getWeeklyMenu() {
        scraperService.scrapeAndStoreMenus();
        // 4. Refactor this to be a scheduled task
        List<Menu> menus = menuStorageService.getWeeklyMenu(LocalDate.now());
//        if (menus.isEmpty()) {
//        menus = scraper.scrapeSvMenu();
//        menuStorageService.saveMenu(menus);
//        }
        return menus;
    }


}
