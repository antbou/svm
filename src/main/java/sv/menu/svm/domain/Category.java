package sv.menu.svm.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Category {
    @NonNull
    TypeCategory type;
    @NonNull
    String title;
    @NonNull
    String description;
    @NonNull
    String priceInt;
    @NonNull
    String priceExt;
}
