//// Assuming you can retrieve email attribute from OAuth2 user
//package com.projectmanagement.config;
//
//import com.projectmanagement.entity.User;
//import com.projectmanagement.repository.UserRepository;
//import com.projectmanagement.service.serviceimpl.AdminServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.DefaultRedirectStrategy;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private AdminServiceImpl userDetailsService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException {
//
//        String redirectUrl = null;
//
//
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        String email = oAuth2User.getAttribute("email");
//        User user = userRepository.findAll().stream().filter(users->users.getUsername().equalsIgnoreCase(email)).findFirst().get();
//        String output = user.getRole().getRole();
//
//        if (user != null) {
//            if (output.equalsIgnoreCase("employee")) {
//
//
//                redirectUrl = "/app/v1/employee";
//            } else {
//
//                redirectUrl = "/app/v1/admin";
//            }
//        } else {
//            redirectUrl = "/app/v1/login?error";
//        }
//
//        new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
//    }
//}
