package com.chan.medmes.production.controller;

import com.chan.medmes.global.response.ApiResponse;
import com.chan.medmes.production.dto.*;
import com.chan.medmes.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    // ── Equipment ─────────────────────────────────────────────────

    @GetMapping("/api/equipment")
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getAllEquipment() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllEquipment()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/equipment")
    public ResponseEntity<ApiResponse<EquipmentResponse>> createEquipment(
            @RequestBody @Valid EquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productionService.createEquipment(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PatchMapping("/api/equipment/{id}/status")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid EquipmentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productionService.updateEquipmentStatus(id, request)));
    }

    // ── ProductionLog ─────────────────────────────────────────────

    @GetMapping("/api/production-logs")
    public ResponseEntity<ApiResponse<List<ProductionLogResponse>>> getLogs(
            @RequestParam(defaultValue = "false") boolean todayOnly) {
        List<ProductionLogResponse> result = todayOnly
                ? productionService.getTodayProductionLogs()
                : productionService.getAllProductionLogs();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping("/api/production-logs")
    public ResponseEntity<ApiResponse<ProductionLogResponse>> createLog(
            @RequestBody @Valid ProductionLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productionService.createProductionLog(request)));
    }

    // ── AlarmLog ──────────────────────────────────────────────────

    @GetMapping("/api/alarms")
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getAlarms(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<AlarmResponse> result = activeOnly
                ? productionService.getActiveAlarms()
                : productionService.getAllAlarms();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping("/api/alarms")
    public ResponseEntity<ApiResponse<AlarmResponse>> createAlarm(
            @RequestBody @Valid AlarmRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productionService.createAlarm(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PatchMapping("/api/alarms/{id}/resolve")
    public ResponseEntity<ApiResponse<AlarmResponse>> resolveAlarm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productionService.resolveAlarm(id)));
    }
}