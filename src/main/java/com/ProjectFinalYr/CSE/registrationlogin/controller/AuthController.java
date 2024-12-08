package com.ProjectFinalYr.CSE.registrationlogin.controller;

import com.ProjectFinalYr.CSE.registrationlogin.dto.UserDto;
import com.ProjectFinalYr.CSE.registrationlogin.entity.User;
import com.ProjectFinalYr.CSE.registrationlogin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("index")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto user,
                               BindingResult result,
                               Model model) {
        User existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }
        userService.saveUser(user);
        return "redirect:/register?success";
    }

    @GetMapping("/users")
    public String listRegisteredUsers(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);

        String message = (String) model.asMap().get("message");
        if (message != null) {
            model.addAttribute("message", message);
        }

        return "users";
    }

//    @GetMapping("/op")
//    public String showNewDashboard(Model model) {
//        model.addAttribute("outputResults", model.asMap().get("outputResults"));
//        model.addAttribute("error", model.asMap().get("error"));
//        return "op";
//    }
/*
    @PostMapping("/run-python")
    public String runPythonScript(@RequestParam("classificationDetails") String classificationDetails,
                                  @RequestParam("details") String details,
                                  @RequestParam("metrics") String metrics,
                                  @RequestParam("trainFile") MultipartFile trainFile,
                                  @RequestParam("testFile") MultipartFile testFile,
                                  Model model) {
        // Define the Python script path
        String pythonScriptPath = "C:\\Users\\User\\OneDrive\\Desktop\\ProjectFinalYr\\Python_script\\1.py";

        // Temporary storage for uploaded files
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("datasets");
            Path trainFilePath = tempDir.resolve(trainFile.getOriginalFilename());
            Path testFilePath = tempDir.resolve(testFile.getOriginalFilename());
            trainFile.transferTo(trainFilePath.toFile());
            testFile.transferTo(testFilePath.toFile());

            // Run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", pythonScriptPath, trainFilePath.toString()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Capture the output of the Python script
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            // Clean up temporary files
            Files.deleteIfExists(trainFilePath);
            Files.deleteIfExists(testFilePath);

            // Add output to the model for rendering in the op dashboard
            model.addAttribute("pythonOutput", output);

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while running the Python script: " + e.getMessage());
            return "error"; // Create an error page for graceful handling
        }

        return "op"; // Redirect to the op.html page
    }

 */
/*
    @PostMapping("/run-python")  // Endpoint to handle the Python script run
    public String runPythonScript(@RequestParam("trainFile") MultipartFile trainFile,
                                  @RequestParam("testFile") MultipartFile testFile,
                                  RedirectAttributes redirectAttributes) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String trainFilePath = tempDir + File.separator + trainFile.getOriginalFilename();
        String testFilePath = tempDir + File.separator + testFile.getOriginalFilename();

        try {
            // Save the uploaded files to the temp directory
            trainFile.transferTo(new File(trainFilePath));
            testFile.transferTo(new File(testFilePath));

            // Specify the path to your Python script
            String pythonScriptPath = "C:\\Users\\User\\OneDrive\\Desktop\\ProjectFinalYr\\Python_script\\1.py";

            // Set up the process builder to run the Python script with the CSV file paths as arguments
            ProcessBuilder processBuilder = new ProcessBuilder("python3", pythonScriptPath, trainFilePath, testFilePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Capture the output from the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

            // Add output results to the redirect attributes
            redirectAttributes.addFlashAttribute("outputResults", output.toString());

            // Redirect to the output page
            return "redirect:/op";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to execute Python script: " + e.getMessage());
            return "redirect:/op"; // Redirect to the output page with an error message
        }*/

}
