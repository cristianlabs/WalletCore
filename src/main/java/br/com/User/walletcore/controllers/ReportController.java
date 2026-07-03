package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.CategoryReportItem;
import br.com.User.walletcore.dtos.MonthlyReportResponse;
import br.com.User.walletcore.dtos.YearlyReportResponse;
import br.com.User.walletcore.security.AuthenticatedUser;
import br.com.User.walletcore.services.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        return ResponseEntity.ok(reportService.getMonthlyReport(principal.getUser(), year, month));
    }

    @GetMapping("/year")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(reportService.getYearlyReport(principal.getUser(), year));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryReportItem>> getCategoryReport(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.getCategoryReport(principal.getUser(), from, to));
    }
}
