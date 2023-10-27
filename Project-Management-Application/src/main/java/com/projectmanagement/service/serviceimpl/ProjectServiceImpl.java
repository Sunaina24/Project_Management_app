package com.projectmanagement.service.serviceimpl;

import com.projectmanagement.entity.ProjectDetails;
import com.projectmanagement.id.NextProjectId;
import com.projectmanagement.repository.NextProjectIdRepository;
import com.projectmanagement.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ProjectServiceImpl {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private NextProjectIdRepository idRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    /**
     * Method to save project details in database
     * @param projectDetails
     */
    public void saveProjectDetails(ProjectDetails projectDetails) {
        projectRepository.save(projectDetails);
        logger.info(String.valueOf(projectDetails));
    }

    /**
     * Method to update project , it fetches project details from projectId and sets the requirements and deadline
     * @param project
     */
    public void updateProjects(ProjectDetails project){
        ProjectDetails projects = projectRepository.findByProjectId(project.getProjectId());
        projects.setRequirements(project.getRequirements());
        projects.setDeadline(project.getDeadline());
        projectRepository.save(projects);
    }
    /**
     * Method fetches all the projects
     * @return
     */
    public List<ProjectDetails> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Method is used to create a custom project ID in three_digit format, it retrieves the current value of the next project ID and updates the next project ID
     * @return
     */
    public String generateCustomProjectId() {
        NextProjectId nextId = idRepository.findById("projectId").orElse(new NextProjectId("projectId", 0));
        int currentNextId = nextId.getNextProjectId();
        String sequentialId = String.format("%03d", currentNextId + 1);
        nextId.setNextProjectId(currentNextId + 1);
        idRepository.save(nextId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String dateStr = sdf.format(new Date());
        return sequentialId +"-"+ dateStr;
    }


    /**
     * Method to find project details by project ID
     * @param projectId
     * @return
     */
    public ProjectDetails findByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId);
    }

    /**
     * Method to update the project details , if the project details are not null then no of employees will be allocated
     * @param projectId
     * @param totalEmployeesAllocated
     */
    public void updateProjectDetails(String projectId, int totalEmployeesAllocated) {
        ProjectDetails projectDetails = projectRepository.findByProjectId(projectId);

        if (projectDetails != null) {
            projectDetails.setNoOfEmployeesAllocated(totalEmployeesAllocated);
            projectRepository.save(projectDetails);
        }
    }

}
