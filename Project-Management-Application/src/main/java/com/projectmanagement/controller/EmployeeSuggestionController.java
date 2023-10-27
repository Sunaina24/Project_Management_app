package com.projectmanagement.controller;

import com.projectmanagement.entity.EmployeeSuggestion;
import com.projectmanagement.entity.User;
import com.projectmanagement.service.serviceimpl.EmailServiceImpl;
import com.projectmanagement.service.serviceimpl.EmployeeServiceImpl;
import com.projectmanagement.service.serviceimpl.EmployeeSuggestionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/suggestion")
public class EmployeeSuggestionController {
    @Autowired
    private EmployeeServiceImpl employeeService;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private EmployeeSuggestionServiceImpl suggestionService;

    /**
     * Method for show compose email page for employee suggestion or feedback.
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/add-suggestion")
    public String showSuggestionPage(Model model, Principal principal) {
        String username = principal.getName();
        User employeeDetails = employeeService.findByUsername(username);
        model.addAttribute("employeeDetails", employeeDetails);
        return "suggestion-page";
    }

    /**
     * Method request to add a suggestion from an employee
     * @param user
     * @param message
     * @return
     */
    @PostMapping("/add-suggestion")
    public String  sendSuggestion(@ModelAttribute User user, @RequestParam("message") String message){

        EmployeeSuggestion suggestion = new EmployeeSuggestion();
        String customId = suggestionService.generateCustomDepartmentId();
        suggestion.setSuggestionId(customId);
        suggestion.setUserId(user.getUserId());
        suggestion.setFullName(user.getFullName());
        suggestion.setUsername(user.getUsername());
        suggestion.setDepartment(user.getDepartment());
        suggestion.setProjectId(user.getProjectId());
        suggestion.setMessages(message);
        suggestionService.saveSuggestion(suggestion);

        emailService.sendSuggestionEmail(user,message);
        return "redirect:/suggestion/email-success";
    }

    /**
     * Method for admin to view suggestions given by employees
     * @param model
     * @return
     */
    @GetMapping("/view-suggestions")
    public String getAllDepartments(Model model) {
        List<EmployeeSuggestion> suggestionList = suggestionService.getAllSuggestions();
        model.addAttribute("listAll", suggestionList);
        return "view-suggestion-list";
    }

    /**
     * Method to show success message
     * @param model
     * @return
     */
    @GetMapping("/email-success")
    public String showSuccessPage(Model model) {
        String successMessage = "Email Sent Successfully";
        model.addAttribute("successMessage", successMessage);
        return "success-page-employee";    }

    /**
     * Method for deleting suggestion from database
     * @param suggestion
     * @return
     */
    @GetMapping("/delete-suggestion/{suggestion}")
    public String deleteUser(@PathVariable String suggestion) {
        suggestionService.deleteEmployeeSuggestionBySuggestionId(suggestion);
        return "redirect:/suggestion/view-suggestions";
    }

}
