package com.chan.medmes.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LotRepository extends JpaRepository {
    boolean existsByLotNo(String lotNo);
    long countByLotNoStartingWith(String prefix); // Lot 번호 자동 생성 시
}
