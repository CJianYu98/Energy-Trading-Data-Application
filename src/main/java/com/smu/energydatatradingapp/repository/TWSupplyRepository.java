package com.smu.energydatatradingapp.repository;

import com.smu.energydatatradingapp.model.TWSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This TWSupplyRepository interface extends the JPA Repository interface and provides CRUD methods for standard
 * data access available in a standard DAO
 */
@Repository
public interface TWSupplyRepository extends JpaRepository<TWSupply, Long> {

}
