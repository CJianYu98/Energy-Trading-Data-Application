package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWConversion;

import java.util.List;

/**
 * Interface class for Taiwan energy conversion data.
 */
public interface TWConversionService {

    void createBatchTWConversion(List<TWConversion> twConversionList);
}
