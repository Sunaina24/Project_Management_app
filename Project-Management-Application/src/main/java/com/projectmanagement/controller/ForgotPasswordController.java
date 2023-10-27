package com.projectmanagement.controller;

import com.projectmanagement.entity.User;
import com.projectmanagement.repository.UserRepository;
import com.projectmanagement.service.serviceimpl.AdminServiceImpl;
import com.projectmanagement.service.serviceimpl.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {
    @Autowired
    private AdminServiceImpl userService;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/user")
    public String forgotPassword()
    {
        return "username-verification";
    }

    /**
     * Method for verify given username is existed or not in our database when forgetting the password
     * @param userDetail
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/user")
    public String usernameVerification(@ModelAttribute("username") User userDetail, RedirectAttributes redirectAttributes) {

        boolean exist = userService.IsUserExist(userDetail.getUsername());
        if (exist) {
            User user = userService.findUserByUsername(userDetail.getUsername());
            emailService.sendForgotPasswordOtp(user);                          // Send email to employee with otp for verification.

            redirectAttributes.addAttribute("username", user.getUsername());

            return "redirect:/forgot-password/otp/verification";
        }
        else {
            return "redirect:/forgot-password/user?error";
        }
    }

    /**
     * Method for otp verification if employee forget the password
     * @param username
     * @param model
     * @return
     */
    @GetMapping("/otp/verification")
    public String otpSent(@RequestParam("username") String username, Model model) {
        model.addAttribute("username", username);
        return "forgot-password-otp-form";
    }

    /**
     * Verify the otp entered by employee and stored in database are same or not
     * @param userDetail
     * @return
     */
    @PostMapping("/otp/verification")
    public String otpVerification(@ModelAttribute("otpValue") User userDetail) {

        User users = userRepository.findByUsername(userDetail.getUsername());
        if(users.getOtp() == userDetail.getOtp()) {
            users.setOtp(0);
            userRepository.save(users);
            return "redirect:/forgot-password/change-password?username=" + userDetail.getUsername();
        }
        else
            return "redirect:/forgot-password/otp/verification?username=error";
    }

    /**
     * Method for show the change password page
     * @param username
     * @param model
     * @return
     */
    @GetMapping("/change-password")
    public String showPasswordChangeForm(@RequestParam("username") String username, Model model) {
        User user = userService.findUserByUsername(username);
        model.addAttribute("user", user);
        return "change-password";
    }

    /**
     * Method for updating password and save in database
     * @param user
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/change-password")
    public String processPasswordChange(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        userService.changeUserPassword(user.getUsername(), user.getPassword());
        return "redirect:/forgot-password/success";
    }

    /**
     * Success message passing after changing password.
     * @param model
     * @return
     */
    @GetMapping("/success")
    public String showSuccessPage(Model model) {
        String successMessage = "Password Changed Successfully";
        String successMessage1 = "Go back to Home Page";

        model.addAttribute("successMessage", successMessage);
        return "success-page-password";
    }
}
