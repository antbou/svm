package sv.menu.svm.repository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import sv.menu.svm.domain.Menu;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class MenuRepository {
    MenuRepositoryInterface menuRepository;

    public List<Menu> saveMenu(List<Menu> menus) {
        log.info("Started scraping menus. Found {} menus.", menus.size());
        List<Menu> savedMenus = List.of();
        if (!menus.isEmpty()) {
            log.info("Saving scraped menus to storage : {}", menus);
            try {
                savedMenus = menuRepository.saveAll(menus);
                log.info("Menus saved successfully.");
            } catch (DuplicateKeyException e) {
                log.warn("Duplicate key error while saving menus: {}", e.getMessage());
            }
        }
        return savedMenus;
    }

    @Cacheable(value = "weeklyMenu", key = "#date")
    public List<Menu> getWeeklyMenu(LocalDate date) {
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(DayOfWeek.FRIDAY.getValue() - DayOfWeek.MONDAY.getValue());
        return menuRepository.findByDateBetween(startOfWeek, endOfWeek);
    }
}
