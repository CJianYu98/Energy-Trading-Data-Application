package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWSupply;

import java.util.List;

/**
 * Interface class for Taiwan energy supply data.
 */
public interface TWSupplyService {

    void createBatchTWSupply(List<TWSupply> twSupplyList);

    double getVolumeOfTypeOfProduct(String type, String product, String year);

    double getNetImport(String year);

    List<Object[]> getNetImportByCriteria(String selectParam1, String selectParam2, String year);

    List<TWSupply> getDistinctByCriteria(String selectParam, String year,
                                              String month, String product,
                                              String type);

    List<Object[]> getByCriteriaGroupBy(String selectParam1, String selectParam2,
                                        String year, String month,
                                        String product, String type);
}
