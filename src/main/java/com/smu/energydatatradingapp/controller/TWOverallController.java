package com.smu.energydatatradingapp.controller;

import com.smu.energydatatradingapp.exception.DataNotFoundException;
import com.smu.energydatatradingapp.model.TWSupply;
import com.smu.energydatatradingapp.service.TWOverallService;
import com.smu.energydatatradingapp.service.TWSupplyService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This TWOverallController class is the api controller which interacts with frontend client for
 * TWConsumption, TWConversion and TWSupply model entity.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/twOverall")
public class TWOverallController {
    private final Logger LOGGER = LoggerFactory.getLogger(TWOverallController.class);
    private final TWOverallService twOverallService;
    private final TWSupplyService twSupplyService;

    /**
     * API endpoint which gets overall gross balance
     * @param year Year
     * @return double value of overall gross balance
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getGrossBalance")
    @Cacheable(cacheNames = "getGrossBalance")
    public ResponseEntity<?> getGrossBalance(
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getGrossBalance in TWOverallController");

        double balance = twOverallService.getGrossBalance(year);
        double volume = twSupplyService.getVolumeOfTypeOfProduct("Self Produced", "Crude Oil", year);
        if (balance == 0 | volume == 0) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getNetBalanceByProduct in TWSupplyController");
            throw new DataNotFoundException("No Taiwan supply data found" +
                    " in database based on query parameters");
        }

        return ResponseEntity.status(HttpStatus.OK.value()).body(balance + volume);
    }

    /**
     * API endpoint which gets gross balance of each product over time
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getGrossBalanceByProduct")
    @Cacheable(cacheNames = "getGrossBalanceByProduct")
    public ResponseEntity<?> getGrossBalanceByProduct(
            @RequestParam String selectParam,
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getGrossBalanceByProduct in TWOverallController");

        List<Object[]> resultList = twOverallService.getGrossBalanceByCriteria(selectParam, "product",
                year);
        List<Object[]> crudeOil = twSupplyService.getByCriteriaGroupBy(
                selectParam, null, year, null, "Crude Oil", "Self Produced");
        if (resultList.isEmpty() | crudeOil.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getGrossBalanceByProduct in TWOverallController");
            throw new DataNotFoundException("No Taiwan overall data found in " +
                    "database based on query parameters");
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

        ArrayList<Double> crudeOilList = new ArrayList<Double>();
        for (Object[] obj : crudeOil) {
            crudeOilList.add((Double) obj[1]);
        }
        products.put("Crude Oil", crudeOilList);

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("time"), time);
        returnObj.put(new String("products"), products);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which gets gross balance over time
     * @param selectParam Parameter to select in SQL query
     * @param year Year
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getGrossBalanceOverTime")
    @Cacheable(cacheNames = "getGrossBalanceOverTime")
    public ResponseEntity<?> getGrossBalanceOverTime(
            @RequestParam String selectParam,
            @RequestParam (required = false) String year) throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getGrossBalanceOverTime in TWOverallController");

        List<Object[]> resultList = twOverallService.getGrossBalanceByCriteria(selectParam, null,
                year);
        List<Object[]> crudeOil = twSupplyService.getByCriteriaGroupBy(
                selectParam, null, year, null, "Crude Oil", "Self Produced");
        if (resultList.isEmpty() | crudeOil.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getGrossBalanceOverTime in TWOverallController");
            throw new DataNotFoundException("No Taiwan overall data found in " +
                    "database based on query parameters");
        }

        // Format result into return object
        Set<Integer> time = new HashSet<Integer>();
        ArrayList<Double> volumes = new ArrayList<Double>();
        for (int i=0; i< resultList.size(); i++) {
            time.add((Integer)resultList.get(i)[0]);
            double volume = (Double)resultList.get(i)[1] + (Double)crudeOil.get(i)[1];
            volumes.add(volume);
        }

        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("time"), time);
        returnObj.put(new String("volumes"), volumes);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }

    /**
     * API endpoint which retrieves all distinct products
     * @return Map of String to Object
     * @throws DataNotFoundException throw when no result is found
     */
    @GetMapping("/getDistinctProducts")
    @Cacheable(cacheNames = "getDistinctProducts")
    public ResponseEntity<?> getDistinctProducts() throws IllegalArgumentException {
        LOGGER.info(">>>>> Requested getDistinctProducts in TWOverallController");

        List<TWSupply> resultList =
                twSupplyService.getDistinctByCriteria("product",
                        null,null, null, null);
        if (resultList.isEmpty()) {
            LOGGER.error("DataNotFoundException thrown when calling " +
                    "getDistinctProducts in TWOverallController");
            throw new DataNotFoundException("No Taiwan data found" +
                    " in database based on query parameters");
        }

        // Format result into return object
        Map<String, Object> returnObj = new HashMap<String, Object>();
        returnObj.put(new String("products"), resultList);

        return ResponseEntity.status(HttpStatus.OK.value()).body(returnObj);
    }
}
