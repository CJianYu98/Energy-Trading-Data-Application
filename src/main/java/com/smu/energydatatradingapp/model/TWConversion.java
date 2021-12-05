package com.smu.energydatatradingapp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * This TWConversion class is for creating tw_conversion table in MYSQL.
 * This table contains attributes related to different types of energy conversion for each product across time
 */

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "tw_conversion")
public class TWConversion {
    /**
     * Unique ID for tw_conversion table
     * Primary Key
     */
    @Id
    @SequenceGenerator(name = "tw_conversion_sequence", sequenceName = "tw_conversion_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tw_conversion_sequence")
    private long id;

    /**
     * Year of record
     */
    @Column(nullable=false, columnDefinition = "int")
    private int year;

    /**
     * Month of record
     */
    @Column(nullable=false, columnDefinition = "int")
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
     * Type of conversion
     */
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private String type;

    /**
     * Volume of product in KBD (thousand barrels per day)
     */
    @Column(nullable = false, columnDefinition = "double")
    private double volume;

    /**
     * Constructor for Taiwan Conversion record
     * Unique ID excluded as it will be auto-generated
     * @param year Year of record
     * @param month Month of record
     * @param product Product category
     * @param specificProduct Specific product name
     * @param type Type of conversion
     * @param volume Volume of product in kbd (thousand barrels per day)
     */
    public TWConversion(int year, int month, String product, String specificProduct, String type, double volume){
        this.year = year;
        this.month= month;
        this.product = product;
        this.specificProduct = specificProduct;
        this.type = type;
        this.volume = volume;
    }
}
