package com.chan.medmes.production.controller;

import com.chan.medmes.global.response.ApiResponse;
import com.chan.medmes.production.dto.DailyProductionDto;
import com.chan.medmes.production.dto.DashboardResponse;
import com.chan.medmes.production.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }

    @GetMapping("/daily-production")
    public ResponseEntity<ApiResponse<List<DailyProductionDto>>> getDailyProduction() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDailyProduction()));
    }
}