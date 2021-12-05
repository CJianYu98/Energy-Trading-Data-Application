package com.smu.energydatatradingapp.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This enum class contains the number equivalent of the month in indonesian language
 */
@AllArgsConstructor
@Getter
public enum MonthIndoWordToNumTranslation {
    Januari(1),
    Februari(2),
    Maret(3),
    April(4),
    Mei(5),
    Juni(6),
    Juli(7),
    Agustus(8),
    September(9),
    Oktober(10),
    November(11),
    Desember(12);

    private final int monthNum;
}
