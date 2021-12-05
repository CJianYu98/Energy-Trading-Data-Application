package com.smu.energydatatradingapp.service;

import java.util.List;

/**
 * Interface class for overall Taiwan energy data.
 */
public interface TWOverallService {

    double getGrossBalance(String year);

    List<Object[]> getGrossBalanceByCriteria(String selectParam1, String selectParam2, String year);

}
