package com.agrocenter.bff.controller;

import com.agrocenter.bff.dto.dashboard.AdminDashboardResponse;
import com.agrocenter.bff.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse dashboard() {
        return dashboardService.getAdminDashboard();
    }
}
