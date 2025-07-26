package sv.menu.svm.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sv.menu.svm.domain.Category;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.domain.TypeCategory;

import java.nio.file.Files;
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

    private void selectWeek(Page page, int index, int step) {
        log.info("Selecting week index: {}", index);
        try {
            page.waitForTimeout(10000);
            page.locator("mat-form-field.week-select-form-field").click(new Locator.ClickOptions().setTimeout(3000));
            page.waitForTimeout(1000);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_" + step + "_week_form_open.png")));

            List<Locator> options = page.locator("mat-option").all();
            log.info("Found {} week options", options.size());
            if (options.size() > index) {
                page.waitForTimeout(5000);
                options.get(index).click(new Locator.ClickOptions().setTimeout(5000));
                page.waitForTimeout(1000);
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_" + (step + 1) + "_week_" + index + "_selected.png")));
                log.info("Week {} selected successfully", index);
            } else {
                log.warn("Index {} is out of range for week options", index);
            }
        } catch (Exception e) {
            log.error("Week selection failed: {}", e.getMessage());
        }
    }

    private void acceptCookies(Page page) {
        log.info("Attempting to accept/reject cookies");
        try {
            Locator reject = page.locator("#cookiescript_reject");

            if (reject.isVisible()) {
                reject.scrollIntoViewIfNeeded();
                reject.hover();
                page.waitForTimeout(3000);
                reject.click(new Locator.ClickOptions()
                        .setTimeout(10000)
                        .setForce(true));
                page.waitForTimeout(500);
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_03_cookies_rejected.png")));
                log.info("Cookies rejected successfully");
            } else {
                log.info("Reject button not visible");
            }

        } catch (Exception e) {
            log.error("Cookie reject failed: {}", e.getMessage());
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_03_cookie_click_failed.png")));
        }
    }

    private Browser launchBrowser() {
        log.info("Launching headless browser");
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(100));
    }

    private List<Menu> scrapeMenuOfTheWeek(Page page, int step) {
        List<Menu> menus = new ArrayList<>();
        List<Locator> links = page.locator("nav.menu-day-selection a").all();
        log.info("Found {} menu links", links.size());

        int index = 0;
        for (Locator link : links) {
            try {
                String href = link.getAttribute("href");
                if (href != null && href.matches("^/menu/.+/date/\\d{4}-\\d{2}-\\d{2}$")) {
                    log.info("Clicking menu link: {}", href);
                    link.click(new Locator.ClickOptions().setTimeout(5000));
                    page.waitForSelector("app-category", new Page.WaitForSelectorOptions().setTimeout(10000));
                    page.waitForTimeout(500);
                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_" + step + "_menu_" + index + ".png")));
                    LocalDate date = LocalDate.parse(href.replaceAll("/menu/.+/date/", ""));
                    log.info("Parsing menu for date: {}", date);
                    menus.add(parseMenu(page, date));
                    index++;
                }
            } catch (Exception e) {
                log.warn("Failed to parse link: {}", e.getMessage());
            }
        }

        return menus;
    }

    public List<Menu> scrapeSvMenu() {
        List<Menu> menus = new ArrayList<>();
        try (Browser browser = launchBrowser()) {
            Page page = browser.newPage();
            page.setDefaultTimeout(90000);

            try {
                log.info("Navigating to endpoint: {}", SV_ENDPOINT);
                page.navigate(SV_ENDPOINT, new Page.NavigateOptions().setTimeout(90000));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                page.waitForSelector("nav.menu-day-selection", new Page.WaitForSelectorOptions().setTimeout(30000));
                page.waitForTimeout(3000);
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_01_page_loaded.png")));
                Files.writeString(Paths.get("screenshots/page_content.html"), page.content());
            } catch (TimeoutError e) {
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_01_page_failed.png")));
                log.error("Navigation failed: {}", e.getMessage());
                return menus;
            }

            acceptCookies(page);

            selectWeek(page, 0, 4);
            menus.addAll(scrapeMenuOfTheWeek(page, 5));

            selectWeek(page, 1, 6);
            menus.addAll(scrapeMenuOfTheWeek(page, 7));

            log.info("Scraping complete. Total menus scraped: {}", menus.size());
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/step_08_done.png")));
        } catch (Exception e) {
            log.error("Global scrape failed: {}", e.getMessage());
        }

        return menus;
    }

    private Menu parseMenu(Page page, LocalDate date) {
        Menu menu = Menu.builder().date(date).isHoliday(false).build();
        List<ElementHandle> categories = page.querySelectorAll("app-category");
        log.info("Found {} categories for date {}", categories.size(), date);

        if (categories.isEmpty()) {
            log.info("No categories found, marking date {} as holiday", date);
            menu.setHoliday(true);
            return menu;
        }

        menu.setCategories(new ArrayList<>());

        for (ElementHandle category : categories) {
            try {
                String rawTitle = category.querySelector("h3.category-header").innerText();
                String normalized = Normalizer.normalize(rawTitle, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
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

                log.info("Parsed category: {}", title);
                menu.getCategories().add(cat);
            } catch (Exception e) {
                log.warn("Failed to parse category: {}", e.getMessage());
            }
        }

        return menu;
    }
}
