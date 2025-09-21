package sv.menu.svm.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Nutrition {
    @NonNull
    Float portion;
    @NonNull
    Float energy;
    @NonNull
    Float fat;
    Float saturatedFat;
    @NonNull
    Float carbohydrates;
    Float sugars;
    Float fiber;
    @NonNull
    Float protein;
    Float salt;
}
