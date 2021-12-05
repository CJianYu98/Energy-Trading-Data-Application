package com.smu.energydatatradingapp.repository;

import com.smu.energydatatradingapp.model.TWConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This TWConsumptionRepository interface extends the JPA Repository interface and provides CRUD methods for standard
 * data access available in a standard DAO
 */
@Repository
public interface TWConsumptionRepository extends JpaRepository<TWConsumption, Long> {

}
