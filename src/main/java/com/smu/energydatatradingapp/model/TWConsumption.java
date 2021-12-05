package com.smu.energydatatradingapp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * This TWConsumption class is for creating tw_consumption table in MYSQL.
 * This table contains attributes related to different types of energy consumption
 * for each main and sub sector across time
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "tw_consumption")
public class TWConsumption {
    /**
     * Unique ID for tw_consumption table
     * Primary Key
     */
    @Id
    @SequenceGenerator(name = "tw_consumption_sequence", sequenceName = "tw_consumption_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tw_consumption_sequence")
    private long id;

    /**
     * Year of record
     */
    @Column(nullable=false, columnDefinition = "int")
    private int year;

    /**
     * Month of record
     * */
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
     * Sector name
     */
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private String sector;

    /**
     * Sub-sector name
     */
    @Column(name = "sub_sector", nullable = false, columnDefinition = "varchar(50)")
    private String subSector;

    /**
     * Volume of product in KBD (thousand barrels per day)
     */
    @Column(nullable = false, columnDefinition = "double")
    private double volume;

    /**
     * Constructor for Taiwan Consumption record
     * Unique ID excluded as it will be auto-generated
     * @param year Year of record
     * @param month Month of record
     * @param product Product category
     * @param specificProduct Specific product name
     * @param sector Sector name
     * @param subSector Sub-sector name
     * @param volume Volume of product in kbd (thousand barrels per day)
     */
    public TWConsumption(int year, int month, String product, String specificProduct, String sector, String subSector, double volume){
        this.year = year;
        this.month= month;
        this.product = product;
        this.specificProduct = specificProduct;
        this.sector = sector;
        this.subSector = subSector;
        this.volume = volume;
    }
}
