package com.smu.energydatatradingapp.repository;

import com.smu.energydatatradingapp.model.IndoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * This IndoDataRepository interface extends the JPA Repository interface and provides CRUD methods for standard
 * data access available in a standard DAO
 */
@Repository
public interface IndoDataRepository extends JpaRepository<IndoData, Long>, JpaSpecificationExecutor<IndoData> {

}