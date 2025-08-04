package sv.menu.svm.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
@Document(collection = "menus")
public class Menu {
    @Id
    String id;
    @NonNull
    @Indexed(unique = true)
    LocalDate date;
    boolean isHoliday;
    List<Category> categories;
}
