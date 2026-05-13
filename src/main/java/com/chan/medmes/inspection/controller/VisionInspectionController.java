package com.chan.medmes.inspection.controller;

import com.chan.medmes.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 3차 고도화 대상 — AI 비전 카메라 연동 시 구현
 * 현재는 엔드포인트 구조만 잡아둔 스켈레톤입니다.
 */
@RestController
@RequiredArgsConstructor
public class VisionInspectionController {

    @PostMapping("/api/vision-inspections")
    public ResponseEntity<ApiResponse<Void>> create() {
        return ResponseEntity.ok(ApiResponse.success(null, "3차 고도화 시 구현 예정입니다."));
    }

    @GetMapping("/api/lots/{lotId}/vision")
    public ResponseEntity<ApiResponse<Void>> getByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(ApiResponse.success(null, "3차 고도화 시 구현 예정입니다."));
    }
}