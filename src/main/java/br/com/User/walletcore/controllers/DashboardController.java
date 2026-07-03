package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.DashboardResponse;
import br.com.User.walletcore.security.AuthenticatedUser;
import br.com.User.walletcore.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(dashboardService.getDashboard(principal.getUser()));
    }
}
