package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWSupply;
import com.smu.energydatatradingapp.repository.TWSupplyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TWSupplyService interface class.
 */
@Service
@AllArgsConstructor
public class TWSupplyServiceImpl implements TWSupplyService{
    private final TWSupplyRepository twSupplyRepository;
    private final EntityManager entityManager;

    /**
     * This method is used to insert Taiwan energy supply records in batches
     * @param twSupplyList List of Taiwan energy supply record
     */
    @Override
    public void createBatchTWSupply(List<TWSupply> twSupplyList) {
        twSupplyRepository.saveAll(twSupplyList);
    }

    /**
     * This method is used to retrieve total volume of a particular product for a particular supply type
     * @return List of TWSupply objects
     */
    @Override
    public double getVolumeOfTypeOfProduct(String type, String product, String year) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<TWSupply> twSupply = cq.from(TWSupply.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();
        criterias.add( cb.equal(twSupply.get("type"), type) );

        if (product != null) {
            criterias.add( cb.equal(twSupply.get("product"), product) );
        }
        if (year != null) {
            criterias.add( cb.equal(twSupply.get("year"), Integer.parseInt(year)) );
        }

        // Create the CriteriaQuery
        cq.multiselect(cb.sum(twSupply.get("volume")))
                .where(cb.and(criterias.toArray(new Predicate[criterias.size()])));

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * This method retrieves net import
     * @param year Gregorian Year
     * @return double value for net import
     */
    @Override
    public double getNetImport(String year) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<TWSupply> twSupply1 = cq.from(TWSupply.class);
        Root<TWSupply> twSupply2 = cq.from(TWSupply.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();
        criterias.add( cb.equal(twSupply1.get("specificProduct"), twSupply2.get("specificProduct")) );
        criterias.add( cb.equal(twSupply1.get("year"), twSupply2.get("year")) );
        criterias.add( cb.equal(twSupply1.get("month"), twSupply2.get("month")) );

        criterias.add( cb.equal(twSupply1.get("type"), "Import") );
        criterias.add( cb.equal(twSupply2.get("type"), "Export") );

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add( cb.equal(twSupply1.get("year"), Integer.parseInt(year)) );
        }

        // Create the CriteriaQuery
        cq.multiselect(cb.diff(cb.sum(twSupply1.get("volume")), cb.sum(twSupply2.get("volume"))))
                .where(criterias.toArray(new Predicate[criterias.size()]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * This method retrieves net import by criteria given
     * @param year Gregorian Year
     * @return List of Object Arrays
     */
    @Override
    public List<Object[]> getNetImportByCriteria(String selectParam1, String selectParam2, String year) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<TWSupply> twSupply1 = cq.from(TWSupply.class);
        Root<TWSupply> twSupply2= cq.from(TWSupply.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();
        criterias.add( cb.equal(twSupply1.get("specificProduct"), twSupply2.get("specificProduct")) );
        criterias.add( cb.equal(twSupply1.get("year"), twSupply2.get("year")) );
        criterias.add( cb.equal(twSupply1.get("month"), twSupply2.get("month")) );

        criterias.add( cb.equal(twSupply1.get("type"), "Import") );
        criterias.add( cb.equal(twSupply2.get("type"), "Export") );

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add( cb.equal(twSupply1.get("year"), Integer.parseInt(year)) );
        }

        // Create CriteriaQuery based on the value of selectParam2
        if (selectParam2 != null) {
            cq.multiselect(twSupply1.get(selectParam1), twSupply1.get(selectParam2),
                            cb.diff(cb.sum(twSupply1.get("volume")), cb.sum(twSupply2.get("volume"))))
                    .where(criterias.toArray(new Predicate[criterias.size()]))
                    .groupBy(twSupply1.get(selectParam1), twSupply1.get(selectParam2))
                    .orderBy(cb.asc(twSupply1.get(selectParam1)), cb.asc(twSupply1.get(selectParam2)));
        }
        else {
            cq.multiselect(twSupply1.get(selectParam1),
                            cb.diff(cb.sum(twSupply1.get("volume")), cb.sum(twSupply2.get("volume"))))
                    .where(criterias.toArray(new Predicate[criterias.size()]))
                    .groupBy(twSupply1.get(selectParam1))
                    .orderBy(cb.asc(twSupply1.get(selectParam1)));
        }

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * This method dynamically query TWSupply model class based on JPA Criteria
     * @param selectParam Parameter to select in SQL query
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param product Product category
     * @param type Type of supply
     * @return List of TWSupply objects
     */
    @Override
    public List<TWSupply> getDistinctByCriteria(String selectParam, String year,
                                                     String month, String product,
                                                     String type) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TWSupply> cq = cb.createQuery(TWSupply.class);
        Root<TWSupply> twSupply = cq.from(TWSupply.class);

        // Select output path for the query
        Path<TWSupply> selectPath;

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add( cb.equal(twSupply.get("year"), Integer.parseInt(year)) );
        }
        if (month != null) {
            criterias.add( cb.equal(twSupply.get("month"), Integer.parseInt(month)) );
        }
        if (product != null) {
            criterias.add( cb.equal(twSupply.get("product"), product) );
        }
        if (type != null) {
            criterias.add( cb.equal(twSupply.get("type"), type) );
        }

        // Create the selectPath and CriteriaQuery
        selectPath = twSupply.get(selectParam);
        cq.select(selectPath)
                .distinct(true)
                .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                .orderBy(cb.asc(twSupply.get(selectParam)));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * This method dynamically query TWSupply model class based on group by
     * conditions
     * @param selectParam1 First parameter to select in SQL query
     * @param selectParam2 Second parameter to select in SQL query
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param product Product category
     * @param type Type of supply
     * @return List of Object list
     */
    @Override
    public List<Object[]> getByCriteriaGroupBy(String selectParam1, String selectParam2,
                                               String year, String month, String product,
                                               String type) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<TWSupply> twSupply = cq.from(TWSupply.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add( cb.equal(twSupply.get("year"), Integer.parseInt(year)) );
        }
        if (month != null) {
            criterias.add( cb.equal(twSupply.get("month"), Integer.parseInt(month)) );
        }
        if (product != null) {
            criterias.add( cb.equal(twSupply.get("product"), product) );
        }
        if (type != null) {
            criterias.add( cb.equal(twSupply.get("type"), type) );
        }

        // Create CriteriaQuery based on the value of selectParam2
        if (selectParam2 != null) {
            cq.multiselect(twSupply.get(selectParam1),
                            twSupply.get(selectParam2),
                            cb.sum(twSupply.get("volume")))
                    .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                    .groupBy(twSupply.get(selectParam1),
                            twSupply.get(selectParam2))
                    .orderBy(cb.asc(twSupply.get(selectParam1)),
                            cb.asc(twSupply.get(selectParam2)));
        } else {
            cq.multiselect(twSupply.get(selectParam1),
                            cb.sum(twSupply.get("volume")))
                    .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                    .groupBy(twSupply.get(selectParam1))
                    .orderBy(cb.asc(twSupply.get(selectParam1)));
        }

        return entityManager.createQuery(cq).getResultList();
    }
}
