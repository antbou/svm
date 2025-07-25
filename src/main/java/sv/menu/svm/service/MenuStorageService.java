package sv.menu.svm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuStorageService {
    private final MenuRepository menuRepository;

    public void saveMenu(List<Menu> menus) {
        menuRepository.saveAll(menus);
    }

    public List<Menu> getWeeklyMenu(LocalDate date) {
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return menuRepository.findByDateBetween(startOfWeek, endOfWeek);
    }
}
