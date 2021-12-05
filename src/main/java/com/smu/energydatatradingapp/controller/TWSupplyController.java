package com.smu.energydatatradingapp.controller;

import com.smu.energydatatradingapp.exception.DataNotFoundException;
import com.smu.energydatatradingapp.service.TWSupplyService;
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
 * This TWSupplyController class is the api controller which interacts with frontend client for TWSupply
 * model entity.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/twSupply")
public class TWSupplyController {
    private final TWSupplyService twSupplyService;
    private final Logger LOGGER = LoggerFactory.getLogger(TWSupplyController.class);

    /**
     * API endpoint which retrieves total primary energy supply of all products
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getPrimaryEnergySupply")
    @Cacheable(cacheNames = "getPrimaryEnergySupply")
    public ResponseEntity<?> getPrimaryEnergySupply(
            @RequestParam String selectParam,
            @RequestParam(required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getPrimaryEnergySupply in TWSupplyController");

        List<Object[]> resultList =
                twSupplyService.getByCriteriaGroupBy(selectParam, "product",
                        year, null, null, "Total Primary Energy Supply");
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getPrimaryEnergySupply in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
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
     * API endpoint which retrieves volume for each type of supply for a particular product
     * @param year Year
     * @param product Product category
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getTypeBreakdownOfProduct")
    @Cacheable(cacheNames = "getTypeBreakdownOfProduct")
    public ResponseEntity<?> getTypeBreakdownOfProduct(
            @RequestParam String product,
            @RequestParam(required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getPrimaryEnergySupply in TWSupplyController");

        List<Object[]> resultList =
                twSupplyService.getByCriteriaGroupBy("type", null,
                        year, null, product, null);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getTypeBreakdownOfProduct in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        ArrayList<String> types = new ArrayList<String>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (Object[] obj : resultList) {
            types.add(String.valueOf(obj[0]));
            volumes.add((Double)obj[1]);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("product"), product);
        returnObj.put(new String("types"), types);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves overall net balance
     * @param year Year
     * @return double value of overall net balance
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getNetBalance")
    @Cacheable(cacheNames = "getNetBalance")
    public ResponseEntity<?> getNetBalance(
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getNetBalance in TWSupplyController");

        double netBalance = twSupplyService.getVolumeOfTypeOfProduct("Inventory Changes", null, year);
        if (netBalance == 0) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getNetBalanceByProduct in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
                    " in database based on query parameters");
        }

        return ResponseEntity.status(HttpStatus.OK.value()).body(netBalance);
    }

    /**
     * API endpoint which retrieves net balance over time
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getNetBalanceOverTime")
    @Cacheable(cacheNames = "getNetBalanceOverTime")
    public ResponseEntity<?> getNetBalanceOverTime(
            @RequestParam String selectParam,
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getNetBalanceOverTime in TWSupplyController");

        List<Object[]> resultList = twSupplyService.getByCriteriaGroupBy(selectParam, null,
                year, null, null, "Inventory Changes");
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getNetBalanceOverTime in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found in " +
                    "database based on query parameters");
        }

        // Format result into return object
        Set<Integer> time = new HashSet<Integer>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (Object[] obj : resultList) {
            time.add((Integer)obj[0]);
            volumes.add((Double)obj[1]);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("time"), time);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves net balance for each product over time
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getNetBalanceByProduct")
    @Cacheable(cacheNames = "getNetBalanceByProduct")
    public ResponseEntity<?> getNetBalanceByProduct(
            @RequestParam String selectParam,
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getNetBalanceByProduct in TWSupplyController");

        List<Object[]> resultList = twSupplyService.getByCriteriaGroupBy(selectParam, "product",
                year, null,null, "Inventory Changes");
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getNetBalanceByProduct in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
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
     * API endpoint which retrieves overall net import
     * @param year Year
     * @return double value of overall net import
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getNetImport")
    @Cacheable(cacheNames = "getNetImport")
    public ResponseEntity<?> getNetImport(
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getNetImport in TWSupplyController");

        double netImport = twSupplyService.getNetImport(year);
        if (netImport == 0) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getNetBalanceByProduct in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
                    " in database based on query parameters");
        }

        return ResponseEntity.status(HttpStatus.OK.value()).body(netImport);
    }

    /**
     * API endpoint which retrieves net balance over time
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getNetImportOverTime")
    @Cacheable(cacheNames = "getNetImportOverTime")
    public ResponseEntity<?> getNetImportOverTime(
            @RequestParam String selectParam,
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getNetImportOverTime in TWSupplyController");

        List<Object[]> resultList = twSupplyService.getNetImportByCriteria(selectParam, null, year);

        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getNetBalanceByProduct in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        Set<Integer> time = new HashSet<Integer>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (Object[] obj : resultList) {
            time.add((Integer)obj[0]);
            volumes.add((Double)obj[1]);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("time"), time);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves net balance over time
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getSupplyTypeOverTime")
    @Cacheable(cacheNames = "getSupplyTypeOverTime")
    public ResponseEntity<?> getSupplyTypeOverTime(
            @RequestParam String selectParam,
            @RequestParam (required = false) String year,
            @RequestParam (required = false) String type) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getSupplyTypeOverTime in TWSupplyController");

        List<Object[]> resultList = twSupplyService.getByCriteriaGroupBy(selectParam, null,
                year, null, null, type);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getSupplyTypeOverTime in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found in " +
                    "database based on query parameters");
        }

        // Format result into return object
        Set<Integer> time = new HashSet<Integer>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (Object[] obj : resultList) {
            time.add((Integer)obj[0]);
            volumes.add((Double)obj[1]);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("time"), time);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }
}
