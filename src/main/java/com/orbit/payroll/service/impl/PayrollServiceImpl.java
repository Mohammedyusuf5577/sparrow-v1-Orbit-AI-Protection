package com.orbit.payroll.service.impl;

import com.orbit.exception.AppException;
import com.orbit.entity.UserEntity;
import com.orbit.repository.UserRepository;
import com.orbit.payroll.dto.request.*;
import com.orbit.payroll.dto.response.*;
import com.orbit.payroll.entity.*;
import com.orbit.payroll.repository.*;
import com.orbit.payroll.service.PayrollService;
import com.orbit.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final EmployeePayrollRepository employeePayrollRepository;
    private final PayrollBankDetailsRepository bankDetailsRepository;
    private final PayrollTaxDetailsRepository taxDetailsRepository;
    private final PayrollSalaryStructureRepository salaryStructureRepository;
    private final PayrollAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EmployeePayrollResponseDTO createEmployeePayroll(EmployeePayrollRequestDTO request) {
        UserEntity employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(AppUtils.Status.USER_NOT_FOUND));

        EmployeePayrollEntity entity = EmployeePayrollEntity.builder()
                .employee(employee)
                .name(request.getName())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .joinDate(request.getJoinDate())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .employmentType(request.getEmploymentType())
                .build();

        entity = employeePayrollRepository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public EmployeePayrollResponseDTO updateEmployeePayroll(UUID id, EmployeePayrollRequestDTO request) {
        EmployeePayrollEntity entity = employeePayrollRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        if (request.getName() != null)
            entity.setName(request.getName());
        if (request.getMobile() != null)
            entity.setMobile(request.getMobile());
        if (request.getEmail() != null)
            entity.setEmail(request.getEmail());
        if (request.getJoinDate() != null)
            entity.setJoinDate(request.getJoinDate());
        if (request.getDepartment() != null)
            entity.setDepartment(request.getDepartment());
        if (request.getDesignation() != null)
            entity.setDesignation(request.getDesignation());
        if (request.getEmploymentType() != null)
            entity.setEmploymentType(request.getEmploymentType());

        entity = employeePayrollRepository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public EmployeePayrollResponseDTO getEmployeePayrollById(UUID id) {
        EmployeePayrollEntity entity = employeePayrollRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));
        return mapToResponse(entity);
    }

    @Override
    public List<EmployeePayrollResponseDTO> getAllEmployeePayroll() {
        return employeePayrollRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEmployeePayroll(UUID id) {
        if (!employeePayrollRepository.existsById(id)) {
            throw new AppException(AppUtils.Status.PAYROLL_NOT_FOUND);
        }
        employeePayrollRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EmployeePayrollResponseDTO saveBankDetails(UUID payrollId, BankDetailsRequestDTO request) {
        EmployeePayrollEntity payroll = employeePayrollRepository.findById(payrollId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        PayrollBankDetailsEntity bankDetails = payroll.getBankDetails();
        if (bankDetails == null) {
            bankDetails = new PayrollBankDetailsEntity();
            bankDetails.setEmployeePayroll(payroll);
        }

        bankDetails.setAccountName(request.getAccountName());
        bankDetails.setBankName(request.getBankName());
        bankDetails.setAccountNumber(request.getAccountNumber());
        bankDetails.setIfsc(request.getIfsc());

        bankDetailsRepository.save(bankDetails);
        payroll.setBankDetails(bankDetails);
        return mapToResponse(payroll);
    }

    @Override
    @Transactional
    public EmployeePayrollResponseDTO saveTaxDetails(UUID payrollId, TaxDetailsRequestDTO request) {
        EmployeePayrollEntity payroll = employeePayrollRepository.findById(payrollId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        PayrollTaxDetailsEntity taxDetails = payroll.getTaxDetails();
        if (taxDetails == null) {
            taxDetails = new PayrollTaxDetailsEntity();
            taxDetails.setEmployeePayroll(payroll);
        }

        taxDetails.setPan(request.getPan());
        taxDetails.setAadhaar(request.getAadhaar());
        taxDetails.setUan(request.getUan());

        taxDetailsRepository.save(taxDetails);
        payroll.setTaxDetails(taxDetails);
        return mapToResponse(payroll);
    }

    @Override
    @Transactional
    public EmployeePayrollResponseDTO saveSalaryStructure(UUID payrollId, SalaryStructureRequestDTO request) {
        EmployeePayrollEntity payroll = employeePayrollRepository.findById(payrollId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        PayrollSalaryStructureEntity salary = payroll.getSalaryStructure();
        if (salary == null) {
            salary = new PayrollSalaryStructureEntity();
            salary.setEmployeePayroll(payroll);
        }

        salary.setBasicSalary(request.getBasicSalary());
        salary.setAllowance(request.getAllowance());
        salary.setPf(request.getPf());
        salary.setTax(request.getTax());
        salary.setOtherDeduction(request.getOtherDeduction());

        salaryStructureRepository.save(salary);
        payroll.setSalaryStructure(salary);
        return mapToResponse(payroll);
    }

    @Override
    @Transactional
    public EmployeePayrollResponseDTO saveAttendance(UUID payrollId, AttendanceRequestDTO request) {
        EmployeePayrollEntity payroll = employeePayrollRepository.findById(payrollId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        PayrollAttendanceEntity attendance = payroll.getAttendance();
        if (attendance == null) {
            attendance = new PayrollAttendanceEntity();
            attendance.setEmployeePayroll(payroll);
        }

        attendance.setWorkingDays(request.getWorkingDays());
        attendance.setPresentDays(request.getPresentDays());
        attendance.setLeaveDays(request.getLeaveDays());
        attendance.setOvertimeHours(request.getOvertimeHours());

        attendanceRepository.save(attendance);
        payroll.setAttendance(attendance);
        return mapToResponse(payroll);
    }

    private EmployeePayrollResponseDTO mapToResponse(EmployeePayrollEntity entity) {
        EmployeePayrollResponseDTO dto = AppUtils.map(entity, EmployeePayrollResponseDTO.class);
        dto.setEmployeeId(entity.getEmployee().getId());

        // Calculate Net Salary
        BigDecimal basic = entity.getSalaryStructure() != null ? entity.getSalaryStructure().getBasicSalary()
                : BigDecimal.ZERO;
        BigDecimal allowance = entity.getSalaryStructure() != null ? entity.getSalaryStructure().getAllowance()
                : BigDecimal.ZERO;
        BigDecimal pf = entity.getSalaryStructure() != null ? entity.getSalaryStructure().getPf() : BigDecimal.ZERO;
        BigDecimal tax = entity.getSalaryStructure() != null ? entity.getSalaryStructure().getTax() : BigDecimal.ZERO;
        BigDecimal otherDeduction = entity.getSalaryStructure() != null
                ? entity.getSalaryStructure().getOtherDeduction()
                : BigDecimal.ZERO;

        // Overtime Amount calculation (Hours * 100 as a placeholder factor)
        BigDecimal overtimeHours = entity.getAttendance() != null ? entity.getAttendance().getOvertimeHours()
                : BigDecimal.ZERO;
        BigDecimal overtimeAmount = overtimeHours != null ? overtimeHours.multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        // Net Salary = (basic_salary + allowance + overtime_amount) - (pf + tax +
        // other_deduction)
        BigDecimal earnings = basic.add(allowance).add(overtimeAmount);
        BigDecimal deductions = pf.add(tax).add(otherDeduction);
        dto.setNetSalary(earnings.subtract(deductions));

        if (entity.getBankDetails() != null) {
            dto.setBankDetails(AppUtils.map(entity.getBankDetails(), BankDetailsResponseDTO.class));
        }
        if (entity.getTaxDetails() != null) {
            dto.setTaxDetails(AppUtils.map(entity.getTaxDetails(), TaxDetailsResponseDTO.class));
        }
        if (entity.getSalaryStructure() != null) {
            dto.setSalaryStructure(AppUtils.map(entity.getSalaryStructure(), SalaryStructureResponseDTO.class));
        }
        if (entity.getAttendance() != null) {
            dto.setAttendance(AppUtils.map(entity.getAttendance(), AttendanceResponseDTO.class));
        }

        return dto;
    }
}
