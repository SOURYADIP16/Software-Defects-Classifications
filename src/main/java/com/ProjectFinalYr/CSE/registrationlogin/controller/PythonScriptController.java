package com.ProjectFinalYr.CSE.registrationlogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class PythonScriptController {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptController.class);

    @PostMapping("/run-python")
    public String runPythonScript(
            @RequestParam("classificationDetails") String classificationDetails,
            @RequestParam("details") String details,
            @RequestParam("metrics") String metrics,
            @RequestParam("trainFile") MultipartFile trainFile,
            @RequestParam("testFile") MultipartFile testFile,
            RedirectAttributes redirectAttributes) {

        String pythonScriptPath = "C:\\Users\\User\\OneDrive\\Desktop\\ProjectFinalYr\\Python_script\\1.py";

        Path tempDir;
        long startTime = System.currentTimeMillis();
        try {
            tempDir = Files.createTempDirectory("datasets");

            // Storing files temporarily
            Path trainFilePath = tempDir.resolve(trainFile.getOriginalFilename());
            Path testFilePath = tempDir.resolve(testFile.getOriginalFilename());
            trainFile.transferTo(trainFilePath.toFile());
            testFile.transferTo(testFilePath.toFile());

            logger.info("Temporary files stored: {} and {}", trainFilePath, testFilePath);

            // Executing Python script with ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", pythonScriptPath, trainFilePath.toString(), testFilePath.toString());
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr
            Process process = processBuilder.start();

            logger.info("Executing Python script: {}", pythonScriptPath);

            // Wait for script to finish with a timeout
            boolean finished = process.waitFor(65000, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                redirectAttributes.addFlashAttribute("error", "Python script execution timed out.");
                logger.error("Python script execution timed out.");
                return "redirect:/error";
            }

            // Capture script output and error streams
            String output = captureOutput(process.getInputStream());
            String errorOutput = captureOutput(process.getErrorStream());

            // Add outputs to model attributes for the frontend
            if (!output.isEmpty()) {
                redirectAttributes.addFlashAttribute("pythonOutput", output);
                logger.info("Python script output: {}", output);
            }
            if (!errorOutput.isEmpty()) {
                redirectAttributes.addFlashAttribute("pythonErrorOutput", errorOutput);
                logger.error("Python script error output: {}", errorOutput);
            }

            // Clean up temporary files
            Files.deleteIfExists(trainFilePath);
            Files.deleteIfExists(testFilePath);
            Files.deleteIfExists(tempDir);
            logger.info("Temporary files cleaned up.");

            // Add additional attributes for the frontend
            redirectAttributes.addFlashAttribute("classificationDetails", classificationDetails);
            redirectAttributes.addFlashAttribute("details", details);
            redirectAttributes.addFlashAttribute("metrics", metrics);

            // Calculate and pass the execution time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            redirectAttributes.addFlashAttribute("executionTime", executionTime);

        } catch (IOException e) {
            logger.error("File handling error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "File handling error: " + e.getMessage());
            return "redirect:/error"; // Error page for file issues
        } catch (InterruptedException e) {
            logger.error("Script execution interrupted: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Script execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return "redirect:/error"; // Error page for script interruptions
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Unexpected error: " + e.getMessage());
            return "redirect:/error"; // Generic error page for other issues
        }

        return "redirect:/op";  // Redirect to results page
    }

    // Helper method to capture output from input streams
    private String captureOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    // Get method to display the output page
    @GetMapping("/op")
    public String showOpPage(Model model) {
        logger.info("Model attributes: pythonOutput = {}, pythonErrorOutput = {}, executionTime = {}, classificationDetails = {}, details = {}, metrics = {}",
                model.getAttribute("pythonOutput"),
                model.getAttribute("pythonErrorOutput"),
                model.getAttribute("executionTime"),
                model.getAttribute("classificationDetails"),
                model.getAttribute("details"),
                model.getAttribute("metrics"));
        return "op";  // Return the output page
    }
}