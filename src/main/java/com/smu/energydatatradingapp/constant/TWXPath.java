package com.smu.energydatatradingapp.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This enum class contains the xpath of an element on the Taiwan website
 */
@AllArgsConstructor
@Getter
public enum TWXPath {
    crudeOil("//input[@value='1_3_1']"),
    liquefiedCB("//input[@value='1_3_5']"),
    propaneCB("//input[@value='1_3_6']"),
    naturalGasCB("//input[@value='1_3_7']"),
    napthaCB("//input[@value='1_3_8']"),
    motorGasCB("//input[@value='1_3_9']"),
    unleadedGasCB("//input[@value='1_3_10']"),
    aviationGasCB("//input[@value='1_3_11']"),
    aviationFuelGCB("//input[@value='1_3_12']"),
    aviationFuelKCB("//input[@value='1_3_13']"),
    keroseneCB("//input[@value='1_3_14']"),
    dieselCB("//input[@value='1_3_15']"),
    fuelCB("//input[@value='1_3_16']"),

    supplyCB("//*[@id=\"rightBox\"]/article[1]/div/div[1]/label/input"),
    consumptionCB("//*[@id=\"rightBox\"]/article[3]/div/div[1]/label/input"),
    conversionCB("//*[@id=\"rightBox\"]/article[2]/div/div[1]/label/input"),
    submitBtn("//*[@id=\"data_accordion\"]/div[2]/div[2]/button"),
    downloadBtn("//a[@title='xlsx 另開視窗， EXCEL匯出下載']");

    private final String productXPath;
}
