package com.newsportal.newsportal.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.newsportal.newsportal.model.Employee;
import com.newsportal.newsportal.model.JobApplication;
import com.newsportal.newsportal.model.User;
import com.newsportal.newsportal.repository.EmployeeRepository;
import com.newsportal.newsportal.repository.JobApplicationRepository;
import com.newsportal.newsportal.repository.UserRepository;
import com.newsportal.newsportal.service.JobApplicationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;


@Controller
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;
    @Autowired
    private JobApplicationRepository repo;
    @Autowired
    private UserRepository userRepository; // Repository for managing users
    @Autowired
    private EmployeeRepository employeeRepository; // Repository for managing employees

    @GetMapping("/applicationDetails/{id}")
    public String showApplicationDetails(@PathVariable("id") Long id, Model model) {
        JobApplication application = repo.findById(id).get();
        
        if (application != null) {
            model.addAttribute("name", application.getName());
            model.addAttribute("email", application.getEmail());
            model.addAttribute("phone", application.getPhone());
            model.addAttribute("qualifications", application.getQualifications());
            model.addAttribute("experience", application.getExperience());
            model.addAttribute("location", application.getLocation());
            model.addAttribute("status", application.getStatus());
            model.addAttribute("appliedBy", application.getAppliedBy());
            model.addAttribute("id", application.getId());
            }
        return "applicationDetails";
    }

    
    
    @PostMapping("/applyjob")
    public String applyJob(@ModelAttribute JobApplication jobApplication, HttpSession session) {
        User user = (User) session.getAttribute("user"); // Cast to User
        if (user != null) {
        	// System.out.print(jobApplication.getLocation());
            String username = user.getName(); // Get the username from the User object
            jobApplication.setAppliedBy(username); // Set the username to appliedBy
        } else {
            // Handle the case where user is null, possibly redirect to login
            return "redirect:/login";
        }
        jobApplicationService.saveApplication(jobApplication);
        return "redirect:/userDashboard"; // Redirect to dashboard after applying
    }


    @GetMapping("/applications")
    public String viewApplications(Model model) {
        List<JobApplication> applications = repo.findAll();
        model.addAttribute("applications", applications);
        return "adminDashboard"; // Thymeleaf template for application details
    }

    @PostMapping("/applications/approve/{id}")
    public String approveApplication(@PathVariable Long id) {
        JobApplication application = repo.findById(id).get();
        // System.out.println("ssssssssss" +application);
        
        if (application != null) {
            // Update application status to "Approved"
            jobApplicationService.updateApplicationStatus(id, "Approved");

            // Fetch the user associated with this application
            User user = userRepository.findByEmail(application.getEmail());
            System.out.println(user);
            if (user != null) {
                // Transfer necessary details to Employee entity
                Employee employee = new Employee();
                employee.setName(user.getName());
                employee.setEmail(user.getEmail());
                employee.setPassword(user.getPassword());
                employee.setPhone(application.getPhone());
                employee.setQualifications(application.getQualifications());
                employee.setExperience(application.getExperience());
                employee.setLocation(application.getLocation());


                // Save the new employee record
                employeeRepository.save(employee);

                // Delete the user from the User table
                userRepository.delete(user);
            }
        }

        return "redirect:/applications";  // Redirect to applications page
    }

    @PostMapping("/applications/reject/{id}")
    public String rejectApplication(@PathVariable Long id) {
    jobApplicationService.updateApplicationStatus(id, "Rejected");
    return "redirect:/applications";  // Redirect to applications page
}

    @PostMapping("/applications/update/{id}")
    public String updateApplication(@PathVariable Long id, @RequestParam String status) {
        jobApplicationService.updateApplicationStatus(id, status);
        return "redirect:/applicationDetails";  // Redirect to applications page
    }

}

