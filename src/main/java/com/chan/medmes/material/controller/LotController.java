package com.chan.medmes.material.controller;

import com.chan.medmes.global.response.ApiResponse;
import com.chan.medmes.material.dto.LotHistoryResponse;
import com.chan.medmes.material.dto.LotRequest;
import com.chan.medmes.material.dto.LotResponse;
import com.chan.medmes.material.dto.LotStatusRequest;
import com.chan.medmes.material.service.LotHistoryService;
import com.chan.medmes.material.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class LotController {

    private final MaterialService materialService;
    private final LotHistoryService lotHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LotResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(materialService.getAllLots()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LotResponse>> create(
            @RequestBody @Valid LotRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialService.createLot(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LotResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(materialService.getLot(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<LotHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(lotHistoryService.getHistory(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LotResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid LotStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialService.updateLotStatus(id, request)));
    }
}