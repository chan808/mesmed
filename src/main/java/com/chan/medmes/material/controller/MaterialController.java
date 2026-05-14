package com.chan.medmes.material.controller;

import com.chan.medmes.global.response.ApiResponse;
import com.chan.medmes.material.dto.InspectionSpecRequest;
import com.chan.medmes.material.dto.InspectionSpecResponse;
import com.chan.medmes.material.dto.RawMaterialRequest;
import com.chan.medmes.material.dto.RawMaterialResponse;
import com.chan.medmes.material.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RawMaterialResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(materialService.getAllMaterials()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RawMaterialResponse>> create(
            @RequestBody @Valid RawMaterialRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialService.createMaterial(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RawMaterialResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(materialService.getMaterial(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(ApiResponse.success(null, "원자재가 삭제되었습니다."));
    }

    @GetMapping("/{id}/specs")
    public ResponseEntity<ApiResponse<List<InspectionSpecResponse>>> getSpecs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(materialService.getSpecsByMaterial(id)));
    }

    @PostMapping("/specs")
    public ResponseEntity<ApiResponse<InspectionSpecResponse>> createSpec(
            @RequestBody @Valid InspectionSpecRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialService.createSpec(request)));
    }

    @PatchMapping("/specs/{id}")
    public ResponseEntity<ApiResponse<InspectionSpecResponse>> updateSpec(
            @PathVariable Long id,
            @RequestBody @Valid InspectionSpecRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialService.updateSpec(id, request)));
    }

    @DeleteMapping("/specs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpec(@PathVariable Long id) {
        materialService.deleteSpec(id);
        return ResponseEntity.ok(ApiResponse.success(null, "검사기준이 삭제되었습니다."));
    }
}