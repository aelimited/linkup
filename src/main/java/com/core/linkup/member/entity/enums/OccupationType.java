package com.core.linkup.member.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OccupationType {

    DEVELOPER("개발"),
    DATA_ANALYST("데이터"),
    PLANNER("기획"),
    DESIGNER("디자인"),
    MARKETER("마케팅"),
    PROMOTER("홍보"),
    RESEARCHER("조사"),
    SALESPERSON("영업 판매"),
    VIDEOGRAPHER("촬영"),
    ACCOUNTANT("회계"),
    TAX_ACCOUNTANT("세무"),
    JUDICIAL_SCRIVENER("법무"),
    BUSINESSPERSON("영업"),
    PRODUCT_MANAGER("상품기획"),
    MERCHANDISER("MD"),
    MEDIA("미디어"),
    CULTURAL_CONTENT_MAKER("문화"),
    ET_CETERA("기타");

    private String occupationInKor;

    public static OccupationType fromKor(String occupationInKor) {
        for (OccupationType type : values()) {
            if (type.getOccupationInKor().equals(occupationInKor)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching occupation type for: " + occupationInKor);
    }
}
