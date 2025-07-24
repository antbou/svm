package sv.menu.svm.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TypeCategory {
    JARDIN("Jardin"),
    MENU("Menu"),
    MARCHE("Marché"),
    SOUPE_DU_JOUR("Soupe du jour");

    private final String label;

    @JsonValue
    public String toJson() {
        return label;
    }
}
