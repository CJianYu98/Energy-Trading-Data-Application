package com.smu.energydatatradingapp.controller;

import com.smu.energydatatradingapp.exception.DataNotFoundException;
import com.smu.energydatatradingapp.service.TWConsumptionService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * This TWConsumptionController class is the api controller which interacts with frontend client for TWConsumption
 * model entity.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/twConsumption")
public class TWConsumptionController {
    private final TWConsumptionService twConsumptionService;
    private final Logger LOGGER = LoggerFactory.getLogger(TWConsumptionController.class);

    /**
     * API endpoint which retrieves all distinct sectors and sub-sectors
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getSectorsSubSectors")
    @Cacheable(cacheNames = "getSectorsSubSectors")
    public ResponseEntity<?> getSectorsSubSectors() throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getSectorsSubSectors in TWConsumptionController");

        List<Object[]> resultList =
                twConsumptionService.getByCriteriaGroupBy("sector", "subSector",
                        null, null, "Crude Oil", null, null);;
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getDistinctProducts in TWConsumptionController");
            throw new DataNotFoundException("No Taiwan consumption data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        Map<String, ArrayList<String>> sectors = new HashMap<String, ArrayList<String>>();
        for (Object[] obj : resultList) {
            String sectorName = String.valueOf(obj[0]);
            ArrayList<String> subSectors = new ArrayList<String>();
            if (sectors.containsKey(sectorName)) {
                subSectors = sectors.get(sectorName);
            }
            subSectors.add(String.valueOf(obj[1]));
            sectors.put(sectorName, subSectors);
        }

        Map<String, Map> returnObj = new HashMap<String, Map>();
        returnObj.put(new String("sectors"), sectors);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves total domestic energy consumption of all products
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getDomesticEnergyConsumption")
    @Cacheable(cacheNames = "getDomesticEnergyConsumption")
    public ResponseEntity<?> getDomesticEnergyConsumption(
            @RequestParam String selectParam,
            @RequestParam(required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getPrimaryEnergyConsumption in TWConsumptionController");

        List<Object[]> resultList =
                twConsumptionService.getByCriteriaGroupBy(selectParam, "product",
                        year, null, null, null, null);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getDomesticEnergyConsumption in TWConsumptionController");
            throw new DataNotFoundException("No Taiwan consumption data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        Set<Integer> time = new HashSet<Integer>();
        Map<String, ArrayList<Double>> products = new HashMap<String, ArrayList<Double>>();
        for (Object[] obj : resultList) {
            time.add((Integer)obj[0]);
            String productName = String.valueOf(obj[1]);
            ArrayList<Double> volumes = new ArrayList<Double>();
            if (products.containsKey(productName)) {
                volumes = products.get(productName);
            }
            volumes.add((Double)obj[2]);
            products.put(String.valueOf(obj[1]), volumes);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("time"), time);
        returnObj.put(new String("products"), products);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves volume of consumption for each product (optional: for a particular sector)
     * @param sector Sector
     * @param sector Sub-category of a sector
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getProductBreakdown")
    @Cacheable(cacheNames = "getProductBreakdown")
    public ResponseEntity<?> getProductBreakdown(
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String subSector,
            @RequestParam(required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getProductBreakdown in TWConsumptionController");

        List<Object[]> resultList =
                twConsumptionService.getByCriteriaGroupBy("product", null,
                        year, null, null, sector, subSector);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getProductBreakdown in TWConsumptionController");
            throw new DataNotFoundException("No Taiwan consumption data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        ArrayList<String> products = new ArrayList<String>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (Object[] obj : resultList) {
            products.add(String.valueOf(obj[0]));
            volumes.add((Double)obj[1]);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("products"), products);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves volume of consumption by each sector for a particular product
     * @param product Product category
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getProductSectorBreakdown")
    @Cacheable(cacheNames = "getProductSectorBreakdown")
    public ResponseEntity<?> getProductSectorBreakdown(
            @RequestParam String product,
            @RequestParam(required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getProductSectorBreakdown in TWConsumptionController");

        List<Object[]> resultList =
                twConsumptionService.getByCriteriaGroupBy("sector", null,
                        year, null, product, null, null);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getProductSectorBreakdown in TWConsumptionController");
            throw new DataNotFoundException("No Taiwan consumption data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        ArrayList<String> sectors = new ArrayList<String>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (Object[] obj : resultList) {
            sectors.add(String.valueOf(obj[0]));
            volumes.add((Double)obj[1]);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("sectors"), sectors);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves volume of consumption for a particular product for each sector and sub-sector
     * @param product Product category
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getSectorSubSectorBreakdown")
    @Cacheable(cacheNames = "getSectorSubSectorBreakdown")
    public ResponseEntity<?> getSectorSubSectorBreakdown(
            @RequestParam String product,
            @RequestParam(required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getSectorSubSectorBreakdown in TWConsumptionController");

        List<Object[]> resultList =
                twConsumptionService.getByCriteriaGroupBy("sector", "subSector",
                        year, null, product, null, null);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getSectorSubSectorBreakdown in TWConsumptionController");
            throw new DataNotFoundException("No Taiwan consumption data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        Map<String, Map<String, Double>> sectors = new HashMap<String, Map<String, Double>>();
        for (Object[] obj : resultList) {
            Map<String, Double> subSectors = new HashMap<String, Double>();
            String subSectorName = String.valueOf(obj[1]);
            String sectorName = String.valueOf(obj[0]);
            if (sectors.containsKey(sectorName)) {
                subSectors = sectors.get(sectorName);
            }
            subSectors.put(subSectorName, (Double)obj[2]);
            sectors.put(sectorName, subSectors);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("sectors"), sectors);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }
}
