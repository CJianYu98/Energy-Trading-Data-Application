package com.smu.energydatatradingapp.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * This IndoData class is for creating indo_data table in MySQL.
 * This table contains attributes related to imported and exported energy product
 * from/to different Indonesian harbor and country at different period of time
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(
        name = "indo_data"
)
public class IndoData {

    /**
     * Unique ID for indo_export table
     * Primary Key
     * */
    @Id
    @SequenceGenerator(name = "indo_data_sequence", sequenceName = "indo_data_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indo_data_sequence")
    private long id;

    /**
     * HS Code for product
     */
    @Column(nullable = false, columnDefinition = "int")
    private int hsCode;

    /** Year of the export/import */
    @Column(nullable = false, columnDefinition = "int")
    private int year;

    /** Month of the export/import */
    @Column(nullable = false, columnDefinition = "int")
    private int month;

    /** Energy product name */
    @NotEmpty(message = "Product attribute must not be null or empty")
    @Column(nullable = false, columnDefinition = "varchar(150)")
    private String product;

    /** Energy product category */
    @NotEmpty(message = "Product category attribute must not be null or empty")
    @Column(nullable = false, columnDefinition = "varchar(15)")
    private String category;

    /** Country exported to/imported from */
    @NotEmpty(message = "Country attribute must not be null or empty")
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private String country;

    /** Indonesia harbor exported from/imported to */
    @NotEmpty(message = "Harbor attribute must not be null or empty")
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private String harbor;

    /** Net value of export in USD */
    @Column(nullable = false, columnDefinition = "float")
    private double value;

    /** Net weight of export in kilograms(KG) */
    @Column(name = "weight_kg", nullable = false, columnDefinition = "float")
    private double weightInKg;

    /** Net weight of export in metric ton(Tonne) */
    @Column(name = "weight_tonne", nullable = false, columnDefinition = "float")
    private double weightInTonne;

    /**
     * Whether is import or export data
     */
    @Column(name = "is_export", nullable = false, columnDefinition = "int")
    @Min(value = 0)
    @Max(value = 1)
    private int isExport;

    /**
     * Constructor for Indonesian energy trading record
     * Unique ID excluded as it will be auto-generated
     * @param hsCode HS Code for product
     * @param year Year of export
     * @param month Month of export
     * @param product Energy product name
     * @param category Energy product category
     * @param country Country exported to
     * @param harbor Harbor exported from
     * @param value Net value in USD
     * @param weightInKg Net weight in kilograms(KG)
     * @param weightInTonne Net weight in metric ton(Tonne)
     * @param isExport Whether is import/export data
     */
    public IndoData(int hsCode, int year, int month, String product, String category, String country, String harbor, double value, double weightInKg, double weightInTonne, int isExport) {
        this.hsCode = hsCode;
        this.year = year;
        this.month = month;
        this.product = product;
        this.category = category;
        this.country = country;
        this.harbor = harbor;
        this.value = value;
        this.weightInKg = weightInKg;
        this.weightInTonne = weightInTonne;
        this.isExport = isExport;
    }
}
