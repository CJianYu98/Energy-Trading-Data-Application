package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWConsumption;
import com.smu.energydatatradingapp.repository.TWConsumptionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TWConsumptionService interface class.
 */
@Service
@AllArgsConstructor
public class TWConsumptionServiceImpl implements TWConsumptionService{
    private final TWConsumptionRepository twConsumptionRepository;
    private final EntityManager entityManager;

    /**
     * This method is used to insert Taiwan energy consumption records in batches
     * @param twConsumptionList List of Taiwan energy consumption record
     */
    @Override
    public void createBatchTWConsumption(List<TWConsumption> twConsumptionList) {
        twConsumptionRepository.saveAll(twConsumptionList);
    }

    /**
     * This method is used to retrieve all Taiwan energy consumption records
     * @return List of TWConsumption objects
     */
    @Override
    public List<TWConsumption> getTWConsumptionList() {
        return twConsumptionRepository.findAll();
    }

    /**
     * This method dynamically query TWConsumption model class based on JPA Criteria
     * @param selectParam Parameter to select in SQL query
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param product Product category
     * @param sector Sector
     * @param subSector More specific area of the sector
     * @return List of TWConsumption objects
     */
    @Override
    public List<TWConsumption> getDistinctByCriteria(String selectParam, String year,
                                                String month, String product,
                                                String sector, String subSector) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TWConsumption> cq = cb.createQuery(TWConsumption.class);
        Root<TWConsumption> twConsumption = cq.from(TWConsumption.class);

        // Select output path for the query
        Path<TWConsumption> selectPath;

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add(cb.equal(twConsumption.get("year"), Integer.parseInt(year)) );
        }
        if (month != null) {
            criterias.add( cb.equal(twConsumption.get("month"), Integer.parseInt(month)) );
        }
        if (product != null) {
            criterias.add( cb.equal(twConsumption.get("product"), product) );
        }
        if (sector != null) {
            criterias.add( cb.equal(twConsumption.get("sector"), sector) );
        }
        if (subSector != null) {
            criterias.add( cb.equal(twConsumption.get("subSector"), subSector) );
        }

        // Create the selectPath and CriteriaQuery
        selectPath = twConsumption.get(selectParam);
        cq.select(selectPath)
                .distinct(true)
                .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                .orderBy(cb.asc(twConsumption.get(selectParam)));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * This method dynamically query TWConsumption model class based on group by
     * conditions
     * @param selectParam1 First parameter to select in SQL query
     * @param selectParam2 Second parameter to select in SQL query
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param product Product category
     * @param sector Sector
     * @param subSector More specific area of the sector
     * @return List of Object list
     */
    @Override
    public List<Object[]> getByCriteriaGroupBy(String selectParam1, String selectParam2,
                                               String year, String month, String product,
                                               String sector, String subSector) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<TWConsumption> twConsumption = cq.from(TWConsumption.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add(cb.equal(twConsumption.get("year"), Integer.parseInt(year)) );
        }
        if (month != null) {
            criterias.add( cb.equal(twConsumption.get("month"), Integer.parseInt(month)) );
        }
        if (product != null) {
            criterias.add( cb.equal(twConsumption.get("product"), product) );
        }
        if (sector != null) {
            criterias.add( cb.equal(twConsumption.get("sector"), sector) );
        }
        if (subSector != null) {
            criterias.add( cb.equal(twConsumption.get("subSector"), subSector) );
        }

        // Create CriteriaQuery based on the value of selectParam2
        if (selectParam2 != null) {
            cq.multiselect(twConsumption.get(selectParam1),
                            twConsumption.get(selectParam2),
                            cb.sum(twConsumption.get("volume")))
                    .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                    .groupBy(twConsumption.get(selectParam1),
                            twConsumption.get(selectParam2),
                            twConsumption.get("product"))
                    .orderBy(cb.asc(twConsumption.get(selectParam1)),
                            cb.asc(twConsumption.get(selectParam2)));
        } else {
            cq.multiselect(twConsumption.get(selectParam1),
                            cb.sum(twConsumption.get("volume")))
                    .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                    .groupBy(twConsumption.get(selectParam1),
                            twConsumption.get("product"))
                    .orderBy(cb.asc(twConsumption.get(selectParam1)));
        }

        return entityManager.createQuery(cq).getResultList();
    }
}
