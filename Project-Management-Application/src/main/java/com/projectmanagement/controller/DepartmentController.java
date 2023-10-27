package com.projectmanagement.controller;

import com.projectmanagement.dto.DepartmentDto;
import com.projectmanagement.entity.DepartmentDetails;
import com.projectmanagement.entity.ProjectDetails;
import com.projectmanagement.exception.DepartmentAlreadyExistsException;
import com.projectmanagement.service.serviceimpl.DepartmentServiceImpl;
import com.projectmanagement.service.serviceimpl.ProjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/department")
public class DepartmentController {
    @Autowired
    private DepartmentServiceImpl departmentService;
    @Autowired
    private ProjectServiceImpl projectService;

    /**
     * Method for create new department
     * @param model
     * @return
     */
    @GetMapping("/add-department")
    public String addDepartmentForm(Model model) {
        model.addAttribute("department", new DepartmentDetails());
        return "create-new-department";
    }

    /**
     * Method for save the department if it is not existing in system
     * @param department
     * @return
     */
    @PostMapping("/add-department")
    public String addDepartmentSubmit(
            @ModelAttribute DepartmentDetails department) throws DepartmentAlreadyExistsException {
                String customId = departmentService.generateCustomDepartmentId();
                department.setDepartmentId(customId);
                departmentService.saveDepartment(department);
                return "redirect:/department/success";
    }



    /**
     * Method for view the bench pool
     * @param model
     * @return
     */
    @GetMapping("/view-bench-pool")
    public String getAllDepartments(Model model) {
        List<DepartmentDetails> departmentList = departmentService.getAllDepartment();
        model.addAttribute("listAll", departmentList);
        return "view-bench-pool";
    }

    /**
     * Method for show adding employees to project form while creating new project
     * @param model
     * @return
     */
    @GetMapping("/add-required-employees")
    public String showAddEmployeesForm(Model model) {
        model.addAttribute("departmentDto", new DepartmentDto());
        model.addAttribute("departmentList", departmentService.getAllDepartments());
        return "employee-requirement-form";
    }

    /**
     * Method for allocate employees to project according to requirements
     * @param departmentDto
     * @param model
     * @param session
     * @return
     */
    @PostMapping("/add-required-employees")
    public String addEmployees(@ModelAttribute("departmentDto") DepartmentDto departmentDto, Model model, HttpSession session) {
        Map<String, Integer> employeesRequired = departmentDto.getEmployeesRequired();

        String projectId = (String) session.getAttribute("projectId");    // Fetching projectId

        ProjectDetails projectDetails = projectService.findByProjectId(projectId);
        String manager = projectDetails.getProjectManager();
        String architect = projectDetails.getSolutionArchitect();

        boolean employeesAvailable = departmentService.checkEmployeesAvailability(employeesRequired, projectId,manager,architect);
        if (employeesAvailable) {
            model.addAttribute("employeesAvailable", true);
            model.addAttribute("departmentList", departmentService.getAllDepartments());

            return "redirect:/project/project-success";
        }
        else {
            return "redirect:/department/add-required-employees?error=true";
        }
    }

    /**
     * Method to show success message after adding department
     * @param model
     * @return
     */
    @GetMapping("/success")
    public String showSuccessPage(Model model) {
        String successMessage = "Department Added Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-admin";
    }

    /**
     * Method for handling the exception - DepartmentAlreadyExistsException
     * @param model
     * @return
     */
    @ExceptionHandler(DepartmentAlreadyExistsException.class)
    public String departmentAlreadyExistException(Model model){
        String message = "Department Already Exists! Try with another one!";
        model.addAttribute("exceptionMessage", message);
        return "exception-page-admin";
    }
}
