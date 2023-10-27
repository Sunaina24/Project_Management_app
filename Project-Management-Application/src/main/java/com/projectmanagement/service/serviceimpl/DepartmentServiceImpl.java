package com.projectmanagement.service.serviceimpl;


import com.projectmanagement.dto.DepartmentDto;
import com.projectmanagement.entity.DepartmentDetails;
import com.projectmanagement.entity.User;
import com.projectmanagement.exception.DepartmentAlreadyExistsException;
import com.projectmanagement.exception.DepartmentNotFoundException;
import com.projectmanagement.id.NextDepartmentId;
import com.projectmanagement.repository.DepartmentRepository;
import com.projectmanagement.repository.NextDepartmentIdRepository;
import com.projectmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ProjectServiceImpl projectService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private NextDepartmentIdRepository idRepository;

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    /**
     * Method to increment the employee allocated in no. of employees and employees available
     * @param departmentId
     */
    public void incrementEmployeesAllocated(String departmentId) {
        DepartmentDetails department = departmentRepository.findByDepartmentId(departmentId);
        department.setNoOfEmployees(department.getNoOfEmployees() +1);
        department.setEmployeesAvailable(department.getEmployeesAvailable() + 1);
        departmentRepository.save(department);
    }

    /**
     * Method used to save the department
     * @param department
     */
    public void saveDepartment(DepartmentDetails department) throws DepartmentAlreadyExistsException {
        try {

            DepartmentDetails isExist = departmentRepository.findByDepartmentName(department.getDepartmentName());
            if (isExist != null) {
                throw new DepartmentAlreadyExistsException("Department Already Exists");
            }

            departmentRepository.save(department);
            logger.info(String.valueOf(department));
        } catch (DataAccessException e) {

            throw new DepartmentAlreadyExistsException("Department Already Exists", e);
        }
    }

    /**
     * Method to get the list of all departments
     * @return
     */
    public List<DepartmentDetails> getAllDepartment() {
        return departmentRepository.findAll();
    }

    /**
     *  Method to generate a custom department ID in three_digit format ,it retrieves the current value of the next department ID and updates the next departmentID
     * @return
     */
    public String generateCustomDepartmentId() {
        NextDepartmentId nextId = idRepository.findById("departmentId").orElse(new NextDepartmentId("departmentId", 0));
        int currentNextId = nextId.getNextDepartmentId();
        String sequentialId = String.format("%03d", currentNextId + 1);
        nextId.setNextDepartmentId(currentNextId + 1);
        idRepository.save(nextId);
        return "D-" + sequentialId;
    }

    /**
     * Method fetches all the department and returns a List of DepartmentDto objects
     * @return
     */
    public List<DepartmentDto> getAllDepartments() {
        List<DepartmentDetails> departments = departmentRepository.findAll();
        return departments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Method checks if there are enough available employees in different departments to allocate to a specific project
     * @param employeesRequired
     * @param projectId
     * @param manager
     * @param architect
     * @return
     */
    public boolean checkEmployeesAvailability(Map<String, Integer> employeesRequired, String projectId, String manager,String architect) {
        int totalEmployeesAllocated = 0;

        for (Map.Entry<String, Integer> entry : employeesRequired.entrySet()) {
            String departmentId = entry.getKey();
            int employeesRequiredCount = entry.getValue();

            DepartmentDetails department = departmentRepository.findByDepartmentId(departmentId);

            if (department == null || department.getEmployeesAvailable() < employeesRequiredCount) {
                return false;
            }

            totalEmployeesAllocated += allocateEmployees(department, employeesRequiredCount, projectId,manager,architect);
            updateDepartmentDetails(department, employeesRequiredCount);
        }

        projectService.updateProjectDetails(projectId, totalEmployeesAllocated);
        return true;
    }

    /**
     * Method is to allocate a specified number of employees to a project within a department while considering any pre-defined roles like manager and solution architect
     * @param department
     * @param employeesRequiredCount
     * @param projectId
     * @param manager
     * @param architect
     * @return
     */
    private int allocateEmployees(DepartmentDetails department, int employeesRequiredCount, String projectId,String manager,String architect) {
        List<User> employees;
        if ("Manager".equals(department.getDepartmentName())){
            employees = userRepository.findByFullName(manager);
        }
        else if ("Solution_Architect".equals(department.getDepartmentName())){
            employees = userRepository.findByFullName(architect);

        }
        else {
            employees = userRepository.findByDepartmentAndProjectAssignedFalse(department.getDepartmentId());
        }

        Collections.shuffle(employees);

        int assignedEmployeesCount = 0;

        if (employeesRequiredCount != 0) {
            for (User employee : employees) {
                if (!employee.isProjectAssigned()) {
                    employee.setProjectAssigned(true);
                    employee.setProjectId(projectId);
                    userRepository.save(employee);
                    assignedEmployeesCount++;

                    emailService.sendProjectEmail(employee);
                }

                if (assignedEmployeesCount == employeesRequiredCount) {
                    break;
                }
            }
        }

        return assignedEmployeesCount;
    }


    /**
     * Method that updates the details of a department
     * @param department
     * @param employeesRequiredCount
     */
    private void updateDepartmentDetails(DepartmentDetails department, int employeesRequiredCount) {
        int newEmployeesAvailable = department.getEmployeesAvailable() - employeesRequiredCount;
        int newEmployeesAllocated = department.getEmployeesAllocated() + employeesRequiredCount;
        department.setEmployeesAvailable(newEmployeesAvailable);
        department.setEmployeesAllocated(newEmployeesAllocated);
        departmentRepository.save(department);
    }

    /**
     * Method extracts relevant information and stores this data in a DepartmentDto
     * @param department
     * @return
     */
    private DepartmentDto convertToDto(DepartmentDetails department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setEmployeesAvailable(department.getEmployeesAvailable());
        return dto;
    }

    /**
     * Method to fetch all managers
     * @param manager
     * @return
     */
    public String getAllManagers(String manager) throws DepartmentNotFoundException {
        try {
            DepartmentDetails department = departmentRepository.findByDepartmentName(manager);
            if (department == null) {
                throw new DepartmentNotFoundException("Department not found for manager: " + manager);
            }
            return department.getDepartmentId();
        } catch (Exception ex) {
            throw new DepartmentNotFoundException("Department not found for manager: " + manager);

        }
    }
    /**
     * Method to fetch all architects
     * @param architect
     * @return
     */
    public String getAllArchitects(String architect) throws DepartmentNotFoundException {
        try {
            DepartmentDetails department = departmentRepository.findByDepartmentName(architect);
            if (department == null) {
                throw new DepartmentNotFoundException("Department not found for architect: " + architect);
            }
            return department.getDepartmentId();
        } catch (Exception ex) {
            throw new DepartmentNotFoundException("Department not found for architect: " + architect);

        }
    }


}