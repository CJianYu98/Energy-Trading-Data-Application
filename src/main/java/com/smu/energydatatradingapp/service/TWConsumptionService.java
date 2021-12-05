package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWConsumption;

import java.util.List;

/**
 * Interface class for Taiwan energy consumption data.
 */
public interface TWConsumptionService {

    void createBatchTWConsumption(List<TWConsumption> twConsumptionList);

    List<TWConsumption> getTWConsumptionList();

    List<TWConsumption> getDistinctByCriteria(String selectParam, String year, String month,
                                         String product, String sector, String subSector);

    List<Object[]> getByCriteriaGroupBy(String selectParam1, String selectParam2,
                                        String year, String month, String product,
                                        String sector, String subSector);
}
