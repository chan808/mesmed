package com.chan.medmes.material.repository;

import com.chan.medmes.material.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
    boolean existsByCode(String code);
    List<RawMaterial> findAllByDeletedAtIsNull();
    Optional<RawMaterial> findByIdAndDeletedAtIsNull(Long id);
}