package sv.menu.svm.service;

import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sv.menu.svm.domain.Category;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.domain.TypeCategory;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ScraperService {
    private final Playwright playwright;

    @Value("${sv.endpoint}")
    private String SV_ENDPOINT;

    public List<Menu> scrapeSvMenu() {
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
        Page page = browser.newPage();
        page.navigate(SV_ENDPOINT);
        page.waitForSelector("#cookiescript_reject", new Page.WaitForSelectorOptions().setTimeout(10000)).click();
        page.waitForTimeout(2000);

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

        AtomicInteger index = new AtomicInteger(0);

        links.forEach(
                link -> {
                    link.click(new ElementHandle.ClickOptions().setTimeout(250));
                    page.waitForTimeout(250);
//                    page.screenshot(new Page.ScreenshotOptions()
//                            .setPath(Paths.get("screenshot_" + index.get() + ".png"))
//                    );
                    System.out.println("Clicked on link: " + link.innerText());
                    index.getAndIncrement();
                    LocalDate date = LocalDate.parse(link.getAttribute("href").replaceAll("/menu/.+/date/", ""));
                    menus.add(parseMenu(page, date));
                }
        );
        browser.close();
        System.out.println("Scraping completed. Total menus scraped: " + menus.size());
        return menus;
    }

    private Menu parseMenu(Page page, LocalDate date) {
        System.out.println("Parsing menu for date: " + date);
        List<ElementHandle> categories = page.querySelectorAll("app-category");
        Menu menu = Menu.builder().date(date).isHoliday(false).build();

        if (categories.isEmpty()) {
            System.out.println("No categories found on the page.");
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
