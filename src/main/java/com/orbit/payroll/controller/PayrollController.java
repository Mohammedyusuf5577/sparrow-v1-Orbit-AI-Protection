package com.orbit.payroll.controller;

import com.orbit.dto.response.ApiResponse;
import com.orbit.payroll.dto.request.*;
import com.orbit.payroll.dto.response.EmployeePayrollResponseDTO;
import com.orbit.payroll.service.PayrollService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Payroll Module APIs")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/employee")
    @Operation(summary = "Create a new employee payroll record")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> createEmployeePayroll(
            @Valid @RequestBody EmployeePayrollRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYROLL_CREATED.getMessage(), payrollService.createEmployeePayroll(request)));
    }

    @PutMapping("/employee/{id}")
    @Operation(summary = "Update employee payroll record")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> updateEmployeePayroll(
            @PathVariable UUID id, @Valid @RequestBody EmployeePayrollRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYROLL_UPDATED.getMessage(), payrollService.updateEmployeePayroll(id, request)));
    }

    @GetMapping("/employee/{id}")
    @Operation(summary = "Get employee payroll details by ID")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> getEmployeePayrollById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYROLL_FETCHED.getMessage(), payrollService.getEmployeePayrollById(id)));
    }

    @GetMapping("/employee")
    @Operation(summary = "Get all employee payroll records")
    public ResponseEntity<ApiResponse<List<EmployeePayrollResponseDTO>>> getAllEmployeePayroll() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYROLL_LIST_FETCHED.getMessage(), payrollService.getAllEmployeePayroll()));
    }

    @DeleteMapping("/employee/{id}")
    @Operation(summary = "Delete employee payroll record")
    public ResponseEntity<ApiResponse<Void>> deleteEmployeePayroll(@PathVariable UUID id) {
        payrollService.deleteEmployeePayroll(id);
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.PAYROLL_DELETED.getMessage()));
    }

    @PostMapping("/{id}/bank")
    @Operation(summary = "Save bank details for an employee")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> saveBankDetails(
            @PathVariable UUID id, @Valid @RequestBody BankDetailsRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.BANK_DETAILS_SAVED.getMessage(), payrollService.saveBankDetails(id, request)));
    }

    @PostMapping("/{id}/tax")
    @Operation(summary = "Save tax details for an employee")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> saveTaxDetails(
            @PathVariable UUID id, @Valid @RequestBody TaxDetailsRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.TAX_DETAILS_SAVED.getMessage(), payrollService.saveTaxDetails(id, request)));
    }

    @PostMapping("/{id}/salary")
    @Operation(summary = "Save salary structure for an employee")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> saveSalaryStructure(
            @PathVariable UUID id, @Valid @RequestBody SalaryStructureRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.SALARY_STRUCTURE_SAVED.getMessage(), payrollService.saveSalaryStructure(id, request)));
    }

    @PostMapping("/{id}/attendance")
    @Operation(summary = "Save attendance data for an employee")
    public ResponseEntity<ApiResponse<EmployeePayrollResponseDTO>> saveAttendance(
            @PathVariable UUID id, @Valid @RequestBody AttendanceRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.ATTENDANCE_SAVED.getMessage(), payrollService.saveAttendance(id, request)));
    }
}
