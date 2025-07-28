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

    public List<Menu> scrapeSvMenu() {
        List<Menu> menus = new ArrayList<>();
        try (Browser browser = launchBrowser()) {
            Page page = browser.newPage();
            page.setDefaultTimeout(90000);

            try {
                log.info("Navigating to endpoint: {}", SV_ENDPOINT);
                page.navigate(SV_ENDPOINT, new Page.NavigateOptions().setTimeout(90000));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                waitForSelectorWithRetries(page, "nav.menu-day-selection", 2000, 10);
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
            throw new RuntimeException("Scraping failed", e);
        }

        return menus;
    }

    private List<Menu> scrapeMenuOfTheWeek(Page page, int step) {
        List<Menu> menus = new ArrayList<>();
        List<Locator> links = page.locator("nav.menu-day-selection a").all();
        log.info("Found {} menu links", links.size());

        for (Locator link : links) {
            String href = link.getAttribute("href");
            String tabId = link.getAttribute("id");

            if (!isValidLink(href, tabId)) {
                log.warn("Invalid link or tab ID: href={}, tabId={}", href, tabId);
                continue;
            }

            String panelSelector = "mat-tab-nav-panel[aria-labelledby='" + tabId + "']";
            LocalDate date = extractDateFromHref(href);
            log.info("Processing date {}", date);

            boolean success = retryWithBackoff(() -> {
                link.click(new Locator.ClickOptions().setTimeout(5000));
                page.waitForSelector("#" + tabId + "[aria-selected='true']",
                        new Page.WaitForSelectorOptions().setTimeout(8000));
                page.waitForSelector(panelSelector + " app-category, " + panelSelector + " div.text-gray-400",
                        new Page.WaitForSelectorOptions().setTimeout(10000));
            }, 5, 2000);

            if (!success) {
                log.warn("Skipping date {} after retries", date);
                continue;
            }

            Locator activePanel = page.locator(panelSelector);
            boolean hasCategory = activePanel.locator("app-category").count() > 0;
            boolean hasNoData = activePanel.locator("div.text-gray-400").isVisible();

            Menu menu = Menu.builder().date(date).isHoliday(false).build();

            if (hasCategory) {
                log.info("Found data for {}", date);
                menu = parseMenu(page, date);
            } else if (hasNoData) {
                log.info("No data for {}, marking as holiday", date);
                menu.setHoliday(true);
            } else {
                log.warn("No clear data state for {}", date);
                continue;
            }

            menus.add(menu);
        }

        return menus;
    }

    private boolean isValidLink(String href, String tabId) {
        return href != null && tabId != null && href.matches("^/menu/.+/date/\\d{4}-\\d{2}-\\d{2}$");
    }

    private LocalDate extractDateFromHref(String href) {
        return LocalDate.parse(href.replaceAll("/menu/.+/date/", ""));
    }

    private boolean retryWithBackoff(Runnable action, int maxRetries, long delayMillis) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                action.run();
                return true;
            } catch (Exception e) {
                log.warn("Attempt {}/{} failed: {}", i + 1, maxRetries, e.getMessage());
            }
        }
        return false;
    }

    private boolean waitForSelectorWithRetries(Page page, String selector, int baseTimeout, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(baseTimeout * attempt));
                return true;
            } catch (Exception e) {
                log.warn("Attempt {} failed for selector '{}': {}", attempt, selector, e.getMessage());
            }
        }
        return false;
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

    private void selectWeek(Page page, int index, int step) {
        log.info("Selecting week index: {}", index);
        try {
            waitForSelectorWithRetries(page, "mat-form-field.week-select-form-field", 2000, 10);
            page.locator("mat-form-field.week-select-form-field").click(new Locator.ClickOptions().setTimeout(30000));

            waitForSelectorWithRetries(page, "mat-option", 1000, 10);
            List<Locator> options = page.locator("mat-option").all();
            log.info("Found {} week options", options.size());

            if (options.size() > index) {
                options.get(index).click(new Locator.ClickOptions().setTimeout(5000));
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
                waitForSelectorWithRetries(page, "#cookiescript_reject", 500, 10);
                reject.click(new Locator.ClickOptions().setTimeout(10000).setForce(true));
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
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(100));
    }
}
