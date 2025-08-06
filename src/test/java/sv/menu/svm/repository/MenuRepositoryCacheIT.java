package sv.menu.svm.repository;

import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sv.menu.svm.domain.Category;
import sv.menu.svm.domain.Menu;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sv.menu.svm.domain.TypeCategory.JARDIN;
import static sv.menu.svm.domain.TypeCategory.MENU;

@EnableCaching
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
class MenuRepositoryCacheIT {

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    CacheManager cacheManager;

    @MockitoBean
    MenuRepositoryInterface menuRepositoryInterface;
    LocalDate date1 = LocalDate.of(2024, 6, 10);
    LocalDate date2 = LocalDate.of(2024, 6, 17);
    List<Menu> menus1 = List.of(
            Menu.builder()
                    .id("1")
                    .date(date1)
                    .isHoliday(false)
                    .categories(List.of(
                            Category.builder()
                                    .type(JARDIN)
                                    .title("Salad")
                                    .description("Fresh salad")
                                    .priceInt("2.50")
                                    .priceExt("3.00")
                                    .build()
                    ))
                    .build()
    );
    List<Menu> menus2 = List.of(
            Menu.builder()
                    .id("2")
                    .date(date2)
                    .isHoliday(true)
                    .categories(List.of(
                            Category.builder()
                                    .type(MENU)
                                    .title("Steak")
                                    .description("Grilled steak")
                                    .priceInt("8.00")
                                    .priceExt("10.00")
                                    .build()
                    ))
                    .build()
    );

    @MockitoBean
    Playwright playwright;

    @BeforeEach
    void setUp() {
        requireNonNull(cacheManager.getCache("weeklyMenu")).clear();
        when(menuRepositoryInterface.findByDateBetween(any(), any()))
                .thenReturn(menus1)
                .thenReturn(menus2);
    }

    @Test
    void testCacheKeyDifferentDates() {
        // Given
        when(menuRepositoryInterface.findByDateBetween(any(), any()))
                .thenReturn(menus1)
                .thenReturn(menus2);

        // When
        List<Menu> result1 = menuRepository.getWeeklyMenu(date1);
        List<Menu> result2 = menuRepository.getWeeklyMenu(date2);

        // Then
        assertThat(result1).isEqualTo(menus1);
        assertThat(result2).isEqualTo(menus2);
        verify(menuRepositoryInterface, times(2)).findByDateBetween(any(), any());
    }

    @Test
    void testCacheHitForSameDate() {
        // Given
        when(menuRepositoryInterface.findByDateBetween(any(), any()))
                .thenReturn(menus1);

        // When
        List<Menu> result1 = menuRepository.getWeeklyMenu(date1);
        List<Menu> result2 = menuRepository.getWeeklyMenu(date1);

        // Then
        assertThat(result1).isEqualTo(menus1);
        assertThat(result2).isEqualTo(menus1);
        verify(menuRepositoryInterface, times(1)).findByDateBetween(any(), any());
    }
}

