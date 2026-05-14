package com.chan.medmes.inspection.controller;

import com.chan.medmes.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AI 비전 카메라 연동 3차 고도화 시 구현 예정
@RestController
@RequiredArgsConstructor
public class VisionInspectionController {

    @PostMapping("/api/vision-inspections")
    public ResponseEntity<ApiResponse<Void>> create() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.success(null, "AI 비전 검사 연동은 3차 고도화 시 구현 예정입니다."));
    }

    @GetMapping("/api/lots/{lotId}/vision")
    public ResponseEntity<ApiResponse<Void>> getByLot(@PathVariable Long lotId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.success(null, "AI 비전 검사 연동은 3차 고도화 시 구현 예정입니다."));
    }
}