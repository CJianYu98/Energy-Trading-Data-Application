package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWConversion;
import com.smu.energydatatradingapp.repository.TWConversionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of TWConversionService interface class.
 */
@Service
@AllArgsConstructor
public class TWConversionServiceImpl implements TWConversionService{
    private final TWConversionRepository twConversionRepository;

    /**
     * This method is used to insert Taiwan energy conversion records in batches
     * @param twConversionList List of Taiwan energy conversion record
     */
    @Override
    public void createBatchTWConversion(List<TWConversion> twConversionList) {
        twConversionRepository.saveAll(twConversionList);
    }
}
