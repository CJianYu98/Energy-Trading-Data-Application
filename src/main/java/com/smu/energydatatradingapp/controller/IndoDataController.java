package com.smu.energydatatradingapp.controller;

import com.smu.energydatatradingapp.exception.DataNotFoundException;
import com.smu.energydatatradingapp.model.IndoData;
import com.smu.energydatatradingapp.service.IndoDataService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This IndoDataController class is the api controller which interacts with
 * frontend client for the IndoData model entity.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/indoData")
public class IndoDataController {
    private final IndoDataService indoDataService;
    private final Logger LOGGER = LoggerFactory.getLogger(IndoDataController.class);

    /**
     * API endpoint which query and retrieves distinct values from IndoData
     * model class dynamically
     * @param selectParam Parameter to select in SQL query
     * @param isExport Whether record is related to import or export
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param category Product category
     * @param country Country exported to/imported from
     * @param harbor Harbor imported to/exported to
     * @return List of IndoData objects
     * @throws DataNotFoundException thrown when no data is found
     */
    @GetMapping("/getDistinctByCriteria")
    @Cacheable(cacheNames = "getDistinctByCriteria")
    public ResponseEntity<?> getDistinctByCriteria(
            @RequestParam String selectParam,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String harbor,
            @RequestParam(required = false) String isExport) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getDistinctByCriteria in IndoDataController");

        List<IndoData> indoDataList = indoDataService.getDistinctByCriteria(
                selectParam, isExport, year, month, category, country, harbor);
        if (indoDataList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getDistinctByCriteria in IndoDataController");
            throw new DataNotFoundException("No Indonesian data found in " +
                    "database based on query parameters");
        }

        return ResponseEntity.status(HttpStatus.OK.value()).body(indoDataList);
    }

    /**
     * API endpoint which query and retrieves aggregated values from IndoData
     * model class using group by condition dynamically
     * @param selectParam1 First parameter to select in SQL query
     * @param selectParam2 Second parameter to select in SQL query
     * @param isExport Whether record is related to import or export
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param category Product category
     * @param country Country exported to/imported from
     * @param harbor Harbor imported to/exported to
     * @return List of Object list
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getByCriteriaGroupBy")
    @Cacheable(cacheNames = "getByCriteriaGroupBy")
    public ResponseEntity<?> getByCriteriaGroupBy(
            @RequestParam String selectParam1,
            @RequestParam(required = false) String selectParam2,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String harbor,
            @RequestParam(required = false) String isExport) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getByCriteriaGroupBy in IndoDataController");

        List<Object[]> resultList =
                indoDataService.getByCriteriaGroupBy(selectParam1, selectParam2,
                isExport, year, month, category, country, harbor);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getByCriteriaGroupBy in IndoDataController");
            throw new DataNotFoundException("No Indonesian data found in " +
                    "database based on query parameters");
        }

        return ResponseEntity.status(HttpStatus.OK.value()).body(resultList);
    }
}