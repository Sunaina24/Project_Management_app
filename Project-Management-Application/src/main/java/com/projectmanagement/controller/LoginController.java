package com.projectmanagement.controller;

import com.projectmanagement.entity.User;
import com.projectmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller("/")
public class LoginController {
    @Autowired
    private UserRepository userRepository;

    /**
     * View Home page
     * @return
     */
    @GetMapping("/app/v1/home-page")
    public String homePageView(){
        return "home-page";
    }

    /**
     *  View Login Page
     * @return
     */
    @GetMapping("/app/v1/login")
    public String loginPage(){return "login-form";}

    /**
     * View OTP verification page
     * @return
     */
    @GetMapping("app/v1/otp/verification")
    public String otpSent() {
        return "otp-verification";
    }

    /**
     * Verify the otp entered by employee and stored in database are same or not
     */
    @PostMapping("/app/v1/otp/verification")
    public String otpVerification(@ModelAttribute("otpValue") User userDetail) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UserDetails employee = (UserDetails) securityContext.getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(employee.getUsername());
        String role = user.getRole().getRole();

        if(user.getOtp() == userDetail.getOtp() && role.equals("Employee")) {
            user.setOtp(0);
            userRepository.save(user);
            return "redirect:/app/v1/employee";
        }
        else if(user.getOtp() == userDetail.getOtp() && role.equals("Admin")){
            user.setOtp(0);
            userRepository.save(user);
            return "redirect:/app/v1/admin";
        }
        else
            return "redirect:/app/v1/otp/employee?error";
    }

    /**
     * View Employee Dashboard
     * @return
     */
    @GetMapping("/app/v1/employee")
    public String login(){
        return "employee-dashboard";
    }

    /**
     * View Admin dashboard
     * @return
     */
    @GetMapping("/app/v1/admin")
    public String admin(){
        return "admin-dashboard";
    }

}
