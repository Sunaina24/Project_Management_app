package com.projectmanagement.controller;

import com.projectmanagement.entity.DepartmentDetails;
import com.projectmanagement.entity.User;

import com.projectmanagement.exception.UserAlreadyExistsException;
import com.projectmanagement.service.serviceimpl.DepartmentServiceImpl;
import com.projectmanagement.service.serviceimpl.EmailServiceImpl;
import com.projectmanagement.service.serviceimpl.AdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminServiceImpl userService;
    @Autowired
    private DepartmentServiceImpl departmentService;
    @Autowired
    private EmailServiceImpl emailService;


    /**
     * method for show Employee Creation form
     * @param model
     * @return
     */
    @GetMapping("/add-employee")
    public String showUserForm(Model model) {
        List<DepartmentDetails> departments = departmentService.getAllDepartment();
        model.addAttribute("departments", departments);
        model.addAttribute("user", new User());
        return "employee-creation-form";
    }

    /**
     * Method for save the detail given in Employee Creation form
     * Store password temporarily using variable rawPassword
     * Send Email to employee with login credentials
     * @param user
     * @return
     */
    @PostMapping("/add-employee")
    public String addUser(@ModelAttribute User user) throws UserAlreadyExistsException {

            String customId = userService.generateCustomUserId();
            user.setUserId(customId);

            String rawPassword = user.getPassword();

            userService.saveEmployee(user);
            User employee = new User();
            employee.setUsername(user.getUsername());
            employee.setFullName(user.getFullName());
            employee.setPassword(rawPassword);
            emailService.sendEmail(employee);

            if (user.getDepartment() != null) {
                departmentService.incrementEmployeesAllocated(user.getDepartment());
            }

            return "redirect:/admin/create-success";

    }

    /**
     *  Method for view employee details
     * @param model
     * @return
     */
    @GetMapping("/view-employee")
    public String listUsers(Model model) {
        List<User> userList = userService.getAllUsers();
        List<DepartmentDetails> departments = departmentService.getAllDepartment();

        Map<String, String> departmentMap = new HashMap<>();            // Used to change departmentId to departmentName
        for (DepartmentDetails department : departments) {
            departmentMap.put(department.getDepartmentId(), department.getDepartmentName());
        }

        model.addAttribute("departments", departmentMap);
        model.addAttribute("userList", userList);
        return "view-employee-details";
    }

    /**
     * Method for checking employee is existed, if exist allow Admin to update details
     * @param username
     * @param model
     * @return
     */
    @GetMapping("/update-employee/{username}")
    public String updateUser(@PathVariable String username, Model model) {
        model.addAttribute("user", userService.findUserByUsername(username));
        return "update-employee-details";
    }

    /**
     * Update user details
     * @param user
     * @return
     */

    @PostMapping("/update-employee")
    public String update(@ModelAttribute User user){
            userService.updateEmployee(user);
            return "redirect:/admin/update-success";
    }

    /**
     * Method for deleting employee from organization (only if he is not assigned with project)
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/delete-employee/{userId}")
    public String deleteUser(@PathVariable String userId, Model model) {

        User employee = userService.findUserByUserId(userId);
        if (employee.isProjectAssigned()) {
            return "redirect:/admin/view-employee?error=true";
        } else {
            userService.deleteEmployee(userId);
            return "redirect:/admin/view-employee?success=true";
        }

    }

    /**
     * View employee details to Send email to Employee
     * @param model
     * @return
     */
    @GetMapping("/show-employee")
    public String viewEmployeeEmail(Model model) {
        List<User> userList = userService.getAllUsers();
        model.addAttribute("userList", userList);
        return "employee-details-view";
    }

    /**
     * Method for show compose email form
     * @param username
     * @param model
     * @return
     */
    @GetMapping("/send-email-employee/{username}")
    public String fetchEmail(@PathVariable String username, Model model) {
        model.addAttribute("user", userService.findUserByUsername(username));
        return "send-email-page";
    }

    /**
     * Send email to employee with employees credentials
     * @param user
     * @param message
     * @return
     */
    @PostMapping("/send-email-employee")
    public String sentEmail(@ModelAttribute User user, @RequestParam("message") String message){
        User user1 = new User();
        user1.setUserId(user.getUserId());
        user1.setFullName(user.getFullName());
        user1.setUsername(user.getUsername());

        emailService.sendEmailToEmployee(user1,message);
        return "redirect:/admin/email-success";
    }
    /**
     * Method for show success message after admin add the employee
     * @param model
     * @return
     */
    @GetMapping("/create-success")
    public String createSuccessPage(Model model) {
        String successMessage = "Employee Details Created Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-admin";
    }

    /**
     * Method for show success message after admin update the details
     * @param model
     * @return
     */
    @GetMapping("/update-success")
    public String showSuccessPage(Model model) {
        String successMessage = "Employee Details Updated Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-admin";
    }

    /**
     * Method for show success message after sent email
     * @param model
     * @return
     */
    @GetMapping("/email-success")
    public String showEmailSuccessPage(Model model) {
        String successMessage = "Email Sent Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-admin";
    }

    /**
     * Method for handling the exception - UserAlreadyExistsException
     * @param model
     * @return
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public String userAlreadyExistException(Model model){
        String message = "Username Already Exists! Try with another one!";
        model.addAttribute("exceptionMessage", message);
        return "exception-page-admin";
    }
}
