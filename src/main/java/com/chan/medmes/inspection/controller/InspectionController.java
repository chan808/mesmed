package com.chan.medmes.inspection.controller;

import com.chan.medmes.global.response.ApiResponse;
import com.chan.medmes.inspection.dto.InspectionRequest;
import com.chan.medmes.inspection.dto.InspectionResponse;
import com.chan.medmes.inspection.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR')")
    @PostMapping("/inspections")
    public ResponseEntity<ApiResponse<InspectionResponse>> create(
            @RequestBody @Valid InspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inspectionService.createInspection(request)));
    }

    @GetMapping("/inspections/{id}")
    public ResponseEntity<ApiResponse<InspectionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inspectionService.getInspection(id)));
    }

    @GetMapping("/lots/{lotId}/inspections")
    public ResponseEntity<ApiResponse<List<InspectionResponse>>> getByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(ApiResponse.success(inspectionService.getInspectionsByLot(lotId)));
    }
}
