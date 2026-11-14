package com.orbit.payroll.service;

import com.orbit.payroll.dto.request.*;
import com.orbit.payroll.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface PayrollService {
    EmployeePayrollResponseDTO createEmployeePayroll(EmployeePayrollRequestDTO request);

    EmployeePayrollResponseDTO updateEmployeePayroll(UUID id, EmployeePayrollRequestDTO request);

    EmployeePayrollResponseDTO getEmployeePayrollById(UUID id);

    List<EmployeePayrollResponseDTO> getAllEmployeePayroll();

    void deleteEmployeePayroll(UUID id);

    EmployeePayrollResponseDTO saveBankDetails(UUID payrollId, BankDetailsRequestDTO request);

    EmployeePayrollResponseDTO saveTaxDetails(UUID payrollId, TaxDetailsRequestDTO request);

    EmployeePayrollResponseDTO saveSalaryStructure(UUID payrollId, SalaryStructureRequestDTO request);

    EmployeePayrollResponseDTO saveAttendance(UUID payrollId, AttendanceRequestDTO request);
}
