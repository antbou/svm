package sv.menu.svm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.menu.svm.domain.Menu;
import sv.menu.svm.repository.MenuRepository;
import sv.menu.svm.service.ScraperService;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class SvmController {
    MenuRepository menuRepository;
    ScraperService scraperService;

    @Operation(summary = "Get weekly menu", description = "Returns the menu for the current week")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of weekly menu")
    })
    @GetMapping("/menus")
    @ResponseStatus(HttpStatus.OK)
    public List<Menu> getWeeklyMenu() {
        return menuRepository.getWeeklyMenu(LocalDate.now());
    }

    @Operation(
            summary = "Scrape and store menus",
            description = "Admin endpoint to trigger menu scraping",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Menu resource created successfully")
    })
    @PostMapping("/admin/scrape")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> scrapeSvMenu() {
        log.info("Admin endpoint called to scrape menus.");
        List<Menu> menus = menuRepository.saveMenu(scraperService.scrapeSvMenu());
        if (menus.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No new menus found to scrape.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Menu resource created successfully.");
    }

    @Operation(
            summary = "List screenshots",
            description = "Returns a list of available screenshots taken during scraping",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @GetMapping("/admin/screenshots")
    public List<String> listScreenshots() {
        File folder = new File("screenshots");
        if (!folder.exists() || !folder.isDirectory()) {
            return List.of();
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return List.of();
        }

        return Arrays.stream(files)
                .map(File::getName)
                .toList();
    }

}
