package sv.menu.svm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SvmController {
    private final MenuRepository menuRepository;

    @GetMapping("/menus")
    public List<Menu> getWeeklyMenu() {
        return menuRepository.getWeeklyMenu(LocalDate.now());
    }
}
