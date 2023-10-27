package com.projectmanagement.service.serviceimpl;

import com.projectmanagement.dto.UserDto;
import com.projectmanagement.entity.AppUserPermission;
import com.projectmanagement.entity.AppUserRole;
import com.projectmanagement.entity.DepartmentDetails;
import com.projectmanagement.entity.User;
import com.projectmanagement.exception.DepartmentNotFoundException;
import com.projectmanagement.exception.NoSuchUserExistException;
import com.projectmanagement.exception.UserAlreadyExistsException;
import com.projectmanagement.id.NextUserId;
import com.projectmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepartmentServiceImpl departmentService;
    @Autowired
    private NextUserIdRepository idRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    AppPermissionRepository appUserPermission;
    @Autowired
    AppUserRepository appUserRepository;
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    /**
     * Method fetches user by username from a database, converts it into a UserDto object, and returns it
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        saveUserDetail();
        User user=userRepository.findAll().stream().filter(user1->user1.getUsername().equalsIgnoreCase(username)).findFirst().get();
        UserDto userDto=new UserDto(user);

        return userDto;
    }

    /**
     * initialize and save user-related details, including roles and permissions
     */
    public void saveUserDetail() {
        if (userRepository.count() == 0) {
            appUserPermission.save(new AppUserPermission("Employee_Read"));
            appUserPermission.save(new AppUserPermission("Employee_Write"));

            appUserRepository.save(new AppUserRole("Employee", appUserPermission.findAll().stream().filter(perm -> perm.getPermission().equalsIgnoreCase("Employee_Read")).collect(Collectors.toSet())));
            appUserRepository.save(new AppUserRole("Admin", appUserPermission.findAll().stream().collect(Collectors.toSet())));
        }
    }



    /**
     * Method used to save or create a new employee
     * @param user
     */
    public void saveEmployee(User user) throws UserAlreadyExistsException {
        try{

        User userDetails = userRepository.findByUsername(user.getUsername());
        if(userDetails!=null){
            throw new UserAlreadyExistsException("User Already Exists");
        }

        DepartmentDetails department = departmentRepository.findByDepartmentId(user.getDepartment());
        AppUserRole userRole1;

        if("Admin".equals(department.getDepartmentName())){
            userRole1 = appUserRepository.findAll().stream().filter(role ->
                    role.getRole().equalsIgnoreCase("Admin")).findFirst().get();
        }
        else {
            userRole1 = appUserRepository.findAll().stream().filter(role ->
                    role.getRole().equalsIgnoreCase("Employee")).findFirst().get();
        }

                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setAccountNonExpired(true);
                user.setAccountNonLocked(true);
                user.setCredentialsNonExpired(true);
                user.setEnabled(true);
                user.setProjectAssigned(false);
                user.setProjectId("Not Assigned");
                user.setOtp(0);
                user.setRole(userRole1);
                user.setPermissions(userRole1.getPermissions());
                userRepository.save(user);
                logger.info(String.valueOf(user));
         }catch (DataAccessException e){
            throw new UserAlreadyExistsException("User Already Exists");
        }
    }

    /**
     * Method to fetch all users from database
     * @return
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Method to generate a custom user ID in three_digit format ,it retrieves the current value of the next user ID and updates he next userID
     * @return
     */
    public String generateCustomUserId() {
        NextUserId nextId = idRepository.findById("userId").orElse(new NextUserId("userId", 0));
        int currentNextId = nextId.getNextUserId();
        String sequentialId = String.format("%03d", currentNextId + 1);
        nextId.setNextUserId(currentNextId + 1);
        idRepository.save(nextId);
        return "FT-" + sequentialId;
    }


    /**
     * Method is to find user by userID from database
     * @param userId
     * @return
     */
    public User findUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * Method used to delete an employee from the system based on their userId and decrement the no. of employees and employees available
     * @param userId
     */
    public void deleteEmployee(String userId) {
        User employee = userRepository.findByUserId(userId);

        String departmentId = employee.getDepartment();
        DepartmentDetails department = departmentRepository.findByDepartmentId(departmentId);

        if (department != null) {
            department.setNoOfEmployees(department.getNoOfEmployees() - 1);
            department.setEmployeesAvailable(department.getEmployeesAvailable() - 1);
            departmentRepository.save(department);
        }
        userRepository.delete(employee);
    }

    /**
     * Method to update user details
     * @param employee
     */
    public void updateEmployee(User employee) {
        User user = userRepository.findByUserId(employee.getUserId());
        user.setUsername(employee.getUsername());
        user.setFullName(employee.getFullName());
        user.setContactNo(employee.getContactNo());
        userRepository.save(user);
    }

    /**
     * Method to fetch all managers
     * @return
     */
    public List<User> getAllManagers() throws DepartmentNotFoundException, NoSuchUserExistException{
        try {
            String manager = "Manager";
            String managerId = departmentService.getAllManagers(manager).toString();
            List<User> userList =userRepository.findByDepartmentAndProjectAssignedFalse(managerId);
            if(userList == null){
                throw new NoSuchUserExistException("No User is exist in department");
            }
            return userList;
        }catch (Exception ex){
            throw new NoSuchUserExistException("No User is exist in department");
        }
    }

    /**
     * Method to fetch all architects
     * @return
     */
    public List<User> getAllArchitects() throws DepartmentNotFoundException, NoSuchUserExistException {
        try {
            String architect = "Solution_Architect";
            String architectId = departmentService.getAllArchitects(architect).toString();
            List<User> userList =  userRepository.findByDepartmentAndProjectAssignedFalse(architectId);
            if(userList == null){
                throw new NoSuchUserExistException("No User is exist in department");
            }
            return userList;
        }catch (Exception ex){
            throw new NoSuchUserExistException("No User is exist in department");
        }
    }



    /**
     * Method to fetch employees through projectID
     * @param projectId
     * @return
     */
    public List<User> getEmployeeByProjectId(String projectId) {
        return userRepository.findByProjectId(projectId);
    }

    /**
     * Method to check whether the user exists or not
     * @param username
     * @return
     */
    public boolean IsUserExist(String username) {
        User exist = userRepository.findByUsername(username);
        if (exist !=null)  return true;
        return false;
    }

    /**
     * Method to fetch user through username
     * @param username
     * @return
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Method to change user password
     * @param username
     * @param password
     */
    public void changeUserPassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}