package sv.menu.svm;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.service.ScraperService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SvmController {
    private final ScraperService scraper;

    @GetMapping("/")
    public List<Menu> hello1() {
        // 2. Save data to database
        // 3. Return data to client
        // 4. Refactor this to be a scheduled task

        return scraper.scrapeSvMenu();
    }
}
