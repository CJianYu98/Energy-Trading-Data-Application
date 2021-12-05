package com.smu.energydatatradingapp.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This enum class contains the HS codes of products in Indonesia website
 */
@AllArgsConstructor
@Getter
public enum IndoProductHSCode {
    crudeOil("27090010;27090090"),
    consdensates("27090020"),
    gasoline("27101211;27101212;27101213;27101221;27101222;27101223;27101224;27101225;27101226;27101227;27101228;27101229"),
    naptha("27101270;27101280"),
    jetFuel("27101981;27101982;27101983"),
    diesel("27101971;27101972"),
    fuelOil("27101979");

    private final String hsCodes;

    /**
     * This method gets the respective HS codes under the specified product category
     * @return String Array containing HS codes under the specified product category
     */
    public String[] getHsCodes() {
        String hsCodes = this.hsCodes;
        return hsCodes.split(";");
    }
}
