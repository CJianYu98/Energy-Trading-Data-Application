package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.IndoData;

import java.util.List;

/**
 * Interface class for Indonesia energy data.
 */
public interface IndoDataService {

    // method below is for inserting IndoData in batches
    void createBatchIndoData(List<IndoData> indoDataList);

    // methods below are for getting IndoData by different conditions
    List<IndoData> getDistinctByCriteria(String selectParam, String isExport,
                                         String year, String month, String category,
                                         String country, String harbor);

    List<Object[]> getByCriteriaGroupBy(String selectParam1, String selectParam2,
                                        String isExport, String year, String month,
                                        String category, String country, String harbor);
}
