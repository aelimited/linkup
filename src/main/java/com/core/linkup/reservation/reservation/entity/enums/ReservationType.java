package com.core.linkup.reservation.reservation.entity.enums;

import com.core.linkup.common.entity.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReservationType {

    DESIGNATED_SEAT("지정석"),
    TEMPORARY_SEAT("자율 좌석"),
    SPACE("공간");

    private final String description;
    public static ReservationType fromKor(String indescriptionInKor) {
        for (ReservationType type : ReservationType.values()) {
            if (type.getDescription().equals(indescriptionInKor)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching occupation type for: " + indescriptionInKor);
    }
}
