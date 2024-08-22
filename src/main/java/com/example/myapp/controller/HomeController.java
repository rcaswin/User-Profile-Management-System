package com.example.myapp.controller;

import com.example.myapp.models.Role;
import com.example.myapp.models.User;
import com.example.myapp.repository.RoleRepository;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Scanner;

@Controller
public class HomeController {


    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"","/"})
    public String postLoginRedirect(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            // If the user is an admin, show the list of users
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);
            return "admin-dashboard"; // Return the view showing the list of users
        } else {
            // If the user is not an admin, check if the form is already filled
            if (user.isProfileComplete()) { // Assuming you have a method to check if the profile is complete
                return "redirect:/edit-profile"; // Redirect to the edit profile page
            } else {
                model.addAttribute("user", user);
                return "user-profile-form"; // Return the view for filling out the form
            }
        }
    }

    @GetMapping("/edit-profile")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        model.addAttribute("user", user);
        return "edit-profile";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, @RequestParam("role") String roleName) {
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            // Create a new Role object
            Role newRole = new Role();
            newRole.setName(roleName); // Use setter to set the name
            return roleRepository.save(newRole);
        });

        user.getRoles().add(role);
        userService.save(user);
        return "redirect:/login";
    }

    @PostMapping("/save-profile")
    public String saveUserProfile(@ModelAttribute("user") User user) {
        userService.updateUserProfile(user); // Update only the profile details
        return "redirect:/";
    }




    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
