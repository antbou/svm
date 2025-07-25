package sv.menu.svm.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.menu.svm.domain.Menu;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuRepository {
    private final MenuRepositoryInterface menuRepository;

    public void saveMenu(List<Menu> menus) {
        menuRepository.saveAll(menus);
    }

    public List<Menu> getWeeklyMenu(LocalDate date) {
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return menuRepository.findByDateBetween(startOfWeek, endOfWeek);
    }
}
