package com.chan.medmes.material.repository;

import com.chan.medmes.material.LotStatus;
import com.chan.medmes.material.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LotRepository extends JpaRepository<Lot, Long> {
    boolean existsByLotNo(String lotNo);
    long countByLotNoStartingWith(String prefix);
    List<Lot> findAllByDeletedAtIsNull();
    Optional<Lot> findByIdAndDeletedAtIsNull(Long id);
    long countByStatusAndDeletedAtIsNull(LotStatus status);
}