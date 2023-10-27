package com.projectmanagement.service.serviceimpl;

import com.projectmanagement.entity.AppUserRole;
import com.projectmanagement.entity.User;
import com.projectmanagement.repository.AppPermissionRepository;
import com.projectmanagement.repository.AppUserRepository;
import com.projectmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);


    /**
     * Method for fetching employee Details form database by using username.
     * @param username
     * @return
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);

    }

    /**
     * Method for fetching employee Details form database by using employeeId.
     * @param id
     * @return
     */
    public User getData(String id){
        return userRepository.findAll().stream().filter(user->user.getUserId().equalsIgnoreCase(id)).findFirst().get();
    }

    /**
     * Method used for updating the details of employee.
     * @param employee
     */
    public void updateEmployee(User employee) {
        User user = userRepository.findByUserId(employee.getUserId());
        user.setFullName(employee.getFullName());
        user.setContactNo(employee.getContactNo());
        user.setPassword(passwordEncoder.encode(employee.getPassword()));
        userRepository.save(user);
        logger.info(String.valueOf(user));
    }

    /**
     * Method for fetching department Details form database by using departmentId.
     * @param department
     * @return
     */
    public List<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }

    /**
     * Method for fetching project Details form database by using projectId.
     * @param projectId
     * @return
     */
    public List<User> getUsersByProjectId(String projectId) {
        return userRepository.findByProjectId(projectId);
    }
}
