package sv.menu.svm.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Menu {
    @NonNull
    LocalDate date;
    boolean isHoliday;
    List<Category> categories;
}
