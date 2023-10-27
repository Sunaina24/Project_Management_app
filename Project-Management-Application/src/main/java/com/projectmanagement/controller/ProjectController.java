package com.projectmanagement.controller;

import com.projectmanagement.entity.DepartmentDetails;
import com.projectmanagement.entity.ProjectDetails;
import com.projectmanagement.entity.User;
import com.projectmanagement.exception.DepartmentNotFoundException;
import com.projectmanagement.exception.NoSuchUserExistException;
import com.projectmanagement.service.serviceimpl.AdminServiceImpl;
import com.projectmanagement.service.serviceimpl.DepartmentServiceImpl;
import com.projectmanagement.service.serviceimpl.ProjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectServiceImpl projectService;
    @Autowired
    private AdminServiceImpl adminService;
    @Autowired
    private DepartmentServiceImpl departmentService;

    @GetMapping("/dashboard")
    public String showProjectDashboard(Model model) {
        return "admin-dashboard";
    }

    /**
     * Show project creation form to admin
     * @param model
     * @return
     */
    @GetMapping("/add-project")
    public String showProjectForm(Model model) throws DepartmentNotFoundException, NoSuchUserExistException {
        model.addAttribute("project", new ProjectDetails());
        List<User> manager = adminService.getAllManagers();
        model.addAttribute("managers",manager);
        List<User> architect = adminService.getAllArchitects();
        model.addAttribute("architects", architect);
        return "project-creation-form";
    }

    /**
     * Saving the project details into database and show employee requirement form to admin
     * @param projectDetails
     * @param session
     * @return
     */
    @PostMapping("/save-project")
    public String saveProjectDetails(@ModelAttribute ProjectDetails projectDetails, HttpSession session) {
        String customId = projectService.generateCustomProjectId();
        projectDetails.setProjectId(customId);
        projectService.saveProjectDetails(projectDetails);

        session.setAttribute("projectId", customId);
        return "redirect:/department/add-required-employees";
    }

    /**
     * View project details existing in database
     * @param model
     * @return
     */
    @GetMapping("/view-projects")
    public String showProjectDetails(Model model) {
        List<ProjectDetails> allProjects = projectService.getAllProjects();
        model.addAttribute("listAll", allProjects);
        return "view-project-details";
    }

    /**
     *  Update project details
     * @param id
     * @param model
     * @return
     */
    @GetMapping("/update-project/{id}")
    public String updateProject(@PathVariable String id, Model model) {
        ProjectDetails project = projectService.findByProjectId(id);
        model.addAttribute("project",project);
        return "update-project-details";
    }

    /**
     * Saving the updated details into database
     * @param project
     * @return
     */
    @PostMapping("/update-project")
    public String update(@ModelAttribute ProjectDetails project){
        projectService.updateProjects(project);
        return "redirect:/project/update-success";
    }

    /**
     * View project team associates who all are working in same project
     * @param id
     * @param model
     * @return
     */
    @GetMapping("/view-project-team/{id}")
    public String ViewProjectTeam(@PathVariable String id, Model model) {
        ProjectDetails project = projectService.findByProjectId(id);
        List<User> userList = adminService.getEmployeeByProjectId(project.getProjectId());
        List<DepartmentDetails> departments = departmentService.getAllDepartment();
        Map<String, String> departmentMap = new HashMap<>();

        for (DepartmentDetails department : departments) {
            departmentMap.put(department.getDepartmentId(), department.getDepartmentName());
        }
        model.addAttribute("departments", departmentMap);
        model.addAttribute("userLists", userList);
        model.addAttribute("project",project);
        return "show-project-members";
    }

    /**
     *  Show success message after creating new project
     * @param model
     * @return
     */
    @GetMapping("/project-success")
    public String showSuccessPage(Model model) {
        String successMessage = "Project Created Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-admin";    }

    /**
     * Show success message after updating project details
     * @param model
     * @return
     */
    @GetMapping("/update-success")
    public String showSuccessPageProject(Model model) {
        String successMessage = "Project Details Updated Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-admin";    }

    /**
     * Method for handling the exception
     * @param model
     * @return
     */
    @ExceptionHandler({DepartmentNotFoundException.class, NoSuchUserExistException.class})
    public String  ExceptionHandler(Model model){
        String message = "No Managers / Solution Architect are Available Now!";
        model.addAttribute("exceptionMessage", message);
        return "exception-page-admin";
    }
}