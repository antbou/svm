package sv.menu.svm.service;

import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sv.menu.svm.domain.Category;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.domain.TypeCategory;

import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScraperService {
    private final Playwright playwright;

    @Value("${sv.endpoint}")
    private String SV_ENDPOINT;

    private static void extracted(Page page, Integer weekIndex) {
        try {
            log.info("Selecting week index: {}", weekIndex);

            page.waitForSelector("mat-form-field.week-select-form-field",
                    new Page.WaitForSelectorOptions().setTimeout(10000)
            ).click();

            page.waitForTimeout(2000);
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots-sv-menu-week-select" + weekIndex + ".png")));

            List<ElementHandle> options = page.querySelectorAll("mat-option");

            if (options.size() > weekIndex) {
                options.get(weekIndex).click();
            } else {
                log.warn("Week index {} out of bounds, only {} options found", weekIndex, options.size());
            }
            page.waitForTimeout(2000);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("screenshots/render_" + weekIndex + ".png")));


//            page.waitForTimeout(1000);
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots-sv-menu-week-selected" + weekIndex + ".png")));

        } catch (TimeoutError e) {
            log.error("Failed to select week index {}: {}", weekIndex, e.getMessage());
            throw e;
        }
    }

    private void acceptCookies(Page page) {
        try {
            page.waitForSelector("#cookiescript_reject", new Page.WaitForSelectorOptions().setTimeout(10000)).click();
            log.info("Cookies accepted successfully.");
            page.waitForTimeout(5000);
        } catch (PlaywrightException e) {
            log.error("Failed to accept cookies: {}", e.getMessage());
            throw new RuntimeException("Failed to accept cookies", e);
        }
    }

    private Browser launchBrowser() {
        return playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(500)
//                        .setArgs(List.of("--start-maximized", "--disable-web-security"))
        );
    }

    private List<Menu> scrapeMenuOfTheWeek(Page page) {
        List<ElementHandle> links = new ArrayList<>(List.of());
        page.querySelectorAll("nav.menu-day-selection a").forEach(
                elementHandle -> {
                    String href = elementHandle.getAttribute("href");
                    if (href != null && href.matches("^/menu/.+/date/\\d{4}-\\d{2}-\\d{2}$")) {
                        links.add(elementHandle);
                    }
                }
        );

        List<Menu> menus = new ArrayList<>(List.of());
        links.forEach(
                link -> {
                    link.click(new ElementHandle.ClickOptions().setTimeout(250));
                    page.waitForTimeout(1000);
                    LocalDate date = LocalDate.parse(link.getAttribute("href").replaceAll("/menu/.+/date/", ""));
                    menus.add(parseMenu(page, date));
                }
        );
        return menus;
    }

    public List<Menu> scrapeSvMenu() {
        log.info("Starting scraping process for SV menu.");
        Browser browser = launchBrowser();
        Page page = browser.newPage();
        page.navigate(SV_ENDPOINT);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/render_initial.png")));

        acceptCookies(page);
        page.waitForTimeout(2000);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/render_after_cookies.png")));


//        page.waitForSelector("nav.menu-day-selection",
//                new Page.WaitForSelectorOptions().setTimeout(15000)
//        );

        List<Menu> menus = new ArrayList<>(List.of());

        extracted(page, 0);
        menus.addAll(scrapeMenuOfTheWeek(page));
        extracted(page, 1);
        menus.addAll(scrapeMenuOfTheWeek(page));

        browser.close();
        log.info("Scraping completed. Total menus scraped: {}", menus.size());
        return menus;
    }

    private Menu parseMenu(Page page, LocalDate date) {
        List<ElementHandle> categories = page.querySelectorAll("app-category");
        Menu menu = Menu.builder().date(date).isHoliday(false).build();

        if (categories.isEmpty()) {
            menu.setHoliday(true);
            return menu;
        }
        menu.setCategories(new ArrayList<>());

        categories.forEach(category -> {
            String rawTitle = category.querySelector("h3.category-header").innerText();
            String normalized = Normalizer.normalize(rawTitle, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "") // Remove diacritics
                    .toUpperCase()
                    .replace(" ", "_");
            String description = category.querySelector(".product-teaser").innerText();
            String title = category.querySelector(".legacy-text-xxl").innerText();
            TypeCategory type = TypeCategory.valueOf(normalized);
            List<ElementHandle> prices = category.querySelectorAll(".price");
            String priceInt = prices.getFirst().innerText();
            String priceExt = prices.size() > 1 ? prices.get(1).innerText() : "";
            Category cat = Category.builder()
                    .type(type)
                    .title(title)
                    .description(description)
                    .priceInt(priceInt)
                    .priceExt(priceExt)
                    .build();

            menu.getCategories().add(cat);
        });
        return menu;
    }
}
