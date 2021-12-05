package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.TWConsumption;
import com.smu.energydatatradingapp.model.TWConversion;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TWOverallService interface class.
 */
@Service
@AllArgsConstructor
public class TWOverallServiceImpl implements TWOverallService{
    private final EntityManager entityManager;

    /**
     * This method retrieves gross balance (excluding crude oil)
     * @param year Gregorian Year
     * @return double value for gross balance (excluding crude oil)
     */
    @Override
    public double getGrossBalance(String year) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<TWConsumption> rootConsumption = cq.from(TWConsumption.class);
        Root<TWConversion> rootConversion= cq.from(TWConversion.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();
        criterias.add( cb.equal(rootConsumption.get("specificProduct"), rootConversion.get("specificProduct")) );
        criterias.add( cb.equal(rootConsumption.get("year"), rootConversion.get("year")) );
        criterias.add( cb.equal(rootConsumption.get("month"), rootConversion.get("month")) );

        criterias.add( cb.or(cb.equal(rootConversion.get("type"), "Transformation Output"),
                        cb.equal(rootConversion.get("type"), "Conversion Between Products (Transfer In)")));
        criterias.add( cb.notEqual(rootConversion.get("product"), "Crude Oil") );

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add( cb.equal(rootConsumption.get("year"), Integer.parseInt(year)) );
        }

        // Create the CriteriaQuery
        cq.multiselect(cb.diff(cb.sum(rootConversion.get("volume")), cb.sum(rootConsumption.get("volume"))))
                .where(criterias.toArray(new Predicate[criterias.size()]))
                .orderBy(cb.asc(rootConsumption.get("year")));

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * This method retrieves gross balance by criteria given (excluding crude oil)
     * @param year Gregorian Year
     * @return List of Object Array
     */
    @Override
    public List<Object[]> getGrossBalanceByCriteria(String selectParam1, String selectParam2, String year) {

        // Instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<TWConsumption> rootConsumption = cq.from(TWConsumption.class);
        Root<TWConversion> rootConversion= cq.from(TWConversion.class);

        // To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();
        criterias.add( cb.equal(rootConsumption.get("specificProduct"), rootConversion.get("specificProduct")) );
        criterias.add( cb.equal(rootConsumption.get("year"), rootConversion.get("year")) );
        criterias.add( cb.equal(rootConsumption.get("month"), rootConversion.get("month")) );
        criterias.add( cb.or(cb.equal(rootConversion.get("type"), "Transformation Output"),
                cb.equal(rootConversion.get("type"), "Conversion Between Products (Transfer In)")));
        criterias.add( cb.notEqual(rootConversion.get("product"), "Crude Oil") );

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (year != null) {
            criterias.add( cb.equal(rootConsumption.get("year"), Integer.parseInt(year)) );
        }

        // Create CriteriaQuery based on the value of selectParam2
        if (selectParam2 != null) {
            cq.multiselect(rootConsumption.get(selectParam1), rootConsumption.get(selectParam2),
                            cb.diff(cb.sum(rootConversion.get("volume")), cb.sum(rootConsumption.get("volume"))))
                    .where(criterias.toArray(new Predicate[criterias.size()]))
                    .groupBy(rootConsumption.get(selectParam1), rootConsumption.get(selectParam2))
                    .orderBy(cb.asc(rootConsumption.get(selectParam1)), cb.asc(rootConsumption.get(selectParam2)));
        }
        else {
            cq.multiselect(rootConsumption.get(selectParam1),
                            cb.diff(cb.sum(rootConversion.get("volume")), cb.sum(rootConsumption.get("volume"))))
                    .where(criterias.toArray(new Predicate[criterias.size()]))
                    .groupBy(rootConsumption.get(selectParam1))
                    .orderBy(cb.asc(rootConsumption.get(selectParam1)));
        }

        return entityManager.createQuery(cq).getResultList();
    }
}
