package com.projectmanagement.service.serviceimpl;

import com.projectmanagement.entity.User;
import com.projectmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    /**
     * Method used to send an email containing a one-time password (OTP) to a specific user
     * @param user
     * @return
     */
    public String sendOtpEmail(User user) {
        generateAndSaveOTP(user);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            String htmlTemplate = getHtmlTemplate(1);
            String emailContent = replacePlaceholders(htmlTemplate, user);

            helper.setFrom("kamilpraseej742@gmail.com");
            helper.setTo(user.getUsername());
            helper.setSubject("Your One-Time Password (OTP) for Prodevans Technologies");
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            logger.info("Email Sent Successfully to "+user.getUsername());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }


    /**
     * Method used for sending a forgot password OTP (One-Time Password) verification email to a user
     * @param user
     * @return
     */
    public String sendForgotPasswordOtp(User user) {
        generateAndSaveOTP(user);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper
                    (message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            String htmlTemplate = getHtmlTemplate(2);
            String emailContent = replacePlaceholders(htmlTemplate, user);

            helper.setFrom("kamilpraseej742@gmail.com");
            helper.setTo(user.getUsername());
            helper.setSubject("Prodevans Technologies - Forgot Password OTP Verification");
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            logger.info("Email Sent Successfully to "+user.getUsername());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "error";
        }
        return "success";

    }

    /**
     * Method contains the necessary information about the user, such as their username, email address
     * @param user
     * @return
     */
    public String  sendEmail(User user) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            String htmlTemplate = getHtmlTemplate(3);
            String emailContent = replacePlaceholders(htmlTemplate, user);

            helper.setFrom("kamilpraseej742@gmail.com");
            helper.setTo(user.getUsername());
            helper.setSubject("Welcome to Prodevans Technologies - Your Login Credentials");
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            logger.info("Email Sent Successfully to "+user.getUsername());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    /**
     * Method used to send mail to those who are assigned the project
     * @param user
     * @return
     */
    public String sendProjectEmail(User user) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            String htmlTemplate = getHtmlTemplate(4);
            String emailContent = replacePlaceholders(htmlTemplate, user);

            helper.setFrom("kamilpraseej742@gmail.com");
            helper.setTo(user.getUsername());
            helper.setSubject("New Project Assignment - Action Required");
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            logger.info("Email Sent Successfully to "+user.getUsername());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "error";
        }
        return "success";

    }

    /**
     * Method used to send suggestion mail to admin
     * @param user
     * @param messageText
     */
    public void sendSuggestionEmail(User user, String messageText) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kamilpraseej742@gmail.com");
        message.setTo(user.getUsername());
        message.setSubject("Employee Suggestion Message");
        message.setText("Employee ID: "+user.getUserId()+"\nEmployee Name: "+user.getFullName()+"\nDepartment ID : "+user.getDepartment()+"\nProject ID: "+user.getProjectId()+"\n\nDear Admin,\n\n"+messageText);
        javaMailSender.send(message);
        logger.info("Email Sent Successfully to "+user.getUsername());

    }

    /**
     * Method to send email to employee from admin
     * @param user
     * @param messageText
     */
    public void sendEmailToEmployee(User user, String messageText) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kamilpraseej742@gmail.com");
        message.setTo(user.getUsername());
        message.setSubject("Message from Admin - Prodevans Technologies");
        message.setText("Dear "+user.getFullName()+",\n\n"+messageText);
        javaMailSender.send(message);
        logger.info("Email Sent Successfully to "+user.getUsername());

    }


    /**
     * @param number
     * @return
     * @throws IOException
     */
    private String getHtmlTemplate(int number) throws IOException {
        Resource resource = null;
        switch (number){
            case 1:
                resource = new ClassPathResource("messages/otp-message.html");
                break;

            case 2:
                resource = new ClassPathResource("messages/forget-otp-message.html");
                break;

            case 3:
                resource = new ClassPathResource("messages/send-employee-email.html");
                break;

            case 4:
                resource = new ClassPathResource("messages/project-email.html");
                break;

        }

        byte[] fileBytes = resource.getInputStream().readAllBytes();
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    /**
     * @param emailContent
     * @param user
     * @return
     */
    private String replacePlaceholders(String emailContent, User user) {
        emailContent = emailContent.replace("{FULL_NAME}", user.getFullName());
        emailContent = emailContent.replace("{OTP}", String.valueOf(user.getOtp()));
        emailContent = emailContent.replace("{USERNAME}", user.getUsername());
        emailContent = emailContent.replace("{PASSWORD}", user.getPassword());

        return emailContent;
    }

    /**
     * To generate and save otp
     * @param user
     */
    public void generateAndSaveOTP(User user) {
        int otp = (int) (Math.random() * 900000) + 100000;
        user.setOtp(otp);
        userRepository.save(user);
    }

}