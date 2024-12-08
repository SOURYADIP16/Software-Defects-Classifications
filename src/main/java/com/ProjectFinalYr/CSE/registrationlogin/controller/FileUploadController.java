package com.ProjectFinalYr.CSE.registrationlogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Controller
public class FileUploadController {

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("dataset1") MultipartFile file1,
                                   @RequestParam("dataset2") MultipartFile file2,
                                   RedirectAttributes redirectAttributes) {
        // Process the uploaded files
        System.out.println("Uploaded files:");
        System.out.println(file1.getOriginalFilename());
        System.out.println(file2.getOriginalFilename());

        // Add a success message to the redirect attributes
        redirectAttributes.addFlashAttribute("message", "Files uploaded successfully!");

        // Redirect to the users page
        return "redirect:/users"; // Redirecting to the users endpoint
    }
}
