package com.chan.medmes.material.repository;

import com.chan.medmes.material.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long>  {
    boolean existsByCode(String code);
}
