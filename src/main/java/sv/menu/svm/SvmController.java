package sv.menu.svm;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.service.MenuStorageService;
import sv.menu.svm.service.ScraperService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SvmController {
    private final ScraperService scraper;
    private final MenuStorageService menuStorageService;

    @GetMapping("/menus")
    public List<Menu> getWeeklyMenu() {
        // 4. Refactor this to be a scheduled task
        List<Menu> menus = menuStorageService.getWeeklyMenu(LocalDate.now());
        if (menus.isEmpty()) {
            menus = scraper.scrapeSvMenu();
            menuStorageService.saveMenu(menus);
        }
        return menus;
    }


}
