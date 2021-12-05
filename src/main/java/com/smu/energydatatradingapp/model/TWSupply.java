package com.smu.energydatatradingapp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * This TWSupply class is for creating tw_supply table in MYSQL.
 * This table contains attributes related to energy product supplied over time
 */

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "tw_supply")
public class TWSupply {
    /**
     * Unique ID for tw_supply table
     * Primary Key
     */
    @Id
    @SequenceGenerator(name = "tw_supply_sequence", sequenceName = "tw_supply_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tw_supply_sequence")
    private long id;

    /**
     * Year of record
     */
    @Column(nullable=false, columnDefinition = "int")
    private int year;

    /**
     * Month of record
     */
    @Column(nullable=false, columnDefinition = "varchar(20)")
    private int month;

    /**
     * Product category
     */
    @Column(nullable=false, columnDefinition = "varchar(50)")
    private String product;

    /**
     * Specific product name
     */
    @Column(name="specific_product", nullable=false, columnDefinition = "varchar(50)")
    private String specificProduct;

    /**
     * Type of supply
     */
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private String type;

    /**
     * Volume of product in KBD (thousand barrels per day)
     */
    @Column(nullable = false, columnDefinition = "double")
    private double volume;

    /**
     * Constructor for Taiwan Supply record
     * Unique ID excluded as it will be auto-generated
     * @param year Year of record
     * @param month Month of record
     * @param product Product category
     * @param specificProduct Specific product name
     * @param type Type of supply
     * @param volume Volume of product in kbd (thousand barrels per day)
     */
    public TWSupply(int year, int month, String product, String specificProduct, String type, double volume){
        this.year = year;
        this.month= month;
        this.product = product;
        this.specificProduct = specificProduct;
        this.type = type;
        this.volume = volume;
    }
}
