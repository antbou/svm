package sv.menu.svm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;
import sv.menu.svm.scheduler.MenuScraperScheduler;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SvmController {
    private final MenuRepository menuRepository;
    private final MenuScraperScheduler menuScraperScheduler;

    @GetMapping("/menus")
    public List<Menu> getWeeklyMenu() {
        return menuRepository.getWeeklyMenu(LocalDate.now());
    }

    @PostMapping("/admin/scrape")
    public ResponseEntity<String> scrapeSvMenu() {
        log.info("Admin endpoint called to scrape menus.");
        menuScraperScheduler.scrapeAndStoreMenus();
        return ResponseEntity.ok("Menu scraping initiated successfully.");
    }
}
