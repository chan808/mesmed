package com.chan.medmes.production.dto;

public record DashboardResponse(
        int todayProducedQty,
        long activeAlarmCount,
        double equipmentRunningRate,
        long pendingLotCount,
        long runningEquipmentCount,
        long totalEquipmentCount,
        long passLotCount,
        long failLotCount,
        long holdLotCount
) {
}
