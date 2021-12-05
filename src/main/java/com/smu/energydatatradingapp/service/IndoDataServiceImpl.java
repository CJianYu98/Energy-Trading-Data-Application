package com.smu.energydatatradingapp.service;

import com.smu.energydatatradingapp.model.IndoData;
import com.smu.energydatatradingapp.repository.IndoDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of IndoDataService interface class.
 */
@Service
@AllArgsConstructor
public class IndoDataServiceImpl implements IndoDataService {
    private final IndoDataRepository indoDataRepository;
    private final EntityManager entityManager;

    /**
     * This method is used to insert Indonesian energy records in batches
     * @param indoDataList List of IndoData objects
     */
    @Override
    public void createBatchIndoData(List<IndoData> indoDataList) {
        indoDataRepository.saveAll(indoDataList);
    }

    /**
     * This method dynamically query IndoData model class based on JPA Criteria
     * @param selectParam Parameter to select in SQL query
     * @param isExport Whether record is related to import or export
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param category Product category
     * @param country Country exported to/imported from
     * @param harbor Harbor imported to/exported to
     * @return List of IndoData objects
     */
    @Override
    public List<IndoData> getDistinctByCriteria(String selectParam,
                                                String isExport, String year,
                                                String month, String category,
                                                String country, String harbor) {

        // instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IndoData> cq = cb.createQuery(IndoData.class);
        Root<IndoData> indoData = cq.from(IndoData.class);

        // Select output path for the query
        Path<IndoData> selectPath;

        //  To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (isExport != null) {
            criterias.add(cb.equal(indoData.get("isExport"), Integer.parseInt(isExport)));
        }
        if (year != null) {
            criterias.add(cb.equal(indoData.get("year"), Integer.parseInt(year)));
        }
        if (month != null) {
            criterias.add( cb.equal(indoData.get("month"), Integer.parseInt(month)));
        }
        if (category != null) {
            criterias.add( cb.equal(indoData.get("category"), category) );
        }
        if (country != null) {
            criterias.add( cb.equal(indoData.get("country"), country) );
        }
        if (harbor != null) {
            criterias.add( cb.equal(indoData.get("harbor"), harbor) );
        }

        // Create the selectPath and CriteriaQuery
        selectPath = indoData.get(selectParam);
        cq.select(selectPath)
                .distinct(true)
                .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                .orderBy(cb.asc(indoData.get(selectParam)));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * This method dynamically query IndoData model class based on group by
     * conditions
     * @param selectParam1 First parameter to select in SQL query
     * @param selectParam2 Second parameter to select in SQL query
     * @param isExport Whether record is related to import or export
     * @param year Year imported/exported
     * @param month Month imported/exported
     * @param category Product category
     * @param country Country exported to/imported from
     * @param harbor Harbor imported to/exported to
     * @return List of Object list
     */
    @Override
    public List<Object[]> getByCriteriaGroupBy(String selectParam1, String selectParam2,
                                               String isExport, String year,
                                               String month, String category,
                                               String country, String harbor) {

        // instantiate CriteriaBuilder, CriteriaQuery and Root variables
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<IndoData> indoData = cq.from(IndoData.class);

        //  To store list of search criteria using CriteriaBuilder
        List<Predicate> criterias = new ArrayList<>();

        /*
        Below if-else conditions check if search condition is null or not.
        If condition is null, search criteria will not be added to the list
        of criteria.
        */
        if (isExport != null) {
            criterias.add(cb.equal(indoData.get("isExport"), Integer.parseInt(isExport)));
        }
        if (year != null) {
            criterias.add(cb.equal(indoData.get("year"), Integer.parseInt(year)));
        }
        if (month != null) {
            criterias.add( cb.equal(indoData.get("month"), Integer.parseInt(month)));
        }
        if (category != null) {
            criterias.add( cb.equal(indoData.get("category"), category) );
        }
        if (country != null) {
            criterias.add( cb.equal(indoData.get("country"), country) );
        }
        if (harbor != null) {
            criterias.add( cb.equal(indoData.get("harbor"), harbor) );
        }

        // Create CriteriaQuery based on the value of selectParam2
        if (selectParam2 != null) {
            cq.multiselect(indoData.get(selectParam1),
                            indoData.get(selectParam2),
                            cb.sum(indoData.get("value")),
                            cb.sum(indoData.get("weightInKg")),
                            cb.sum(indoData.get("weightInTonne")))
                    .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                    .groupBy(indoData.get(selectParam1),
                            indoData.get(selectParam2))
                    .orderBy(cb.asc(indoData.get(selectParam1)),
                            cb.asc(indoData.get(selectParam2)));
        } else {
            cq.multiselect(indoData.get(selectParam1),
                            cb.sum(indoData.get("value")),
                            cb.sum(indoData.get("weightInKg")),
                            cb.sum(indoData.get("weightInTonne")))
                    .where(cb.and(criterias.toArray(new Predicate[criterias.size()])))
                    .groupBy(indoData.get(selectParam1))
                    .orderBy(cb.asc(indoData.get(selectParam1)));
        }

        return entityManager.createQuery(cq).getResultList();
    }
}
