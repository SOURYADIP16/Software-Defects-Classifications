package com.ProjectFinalYr.CSE.registrationlogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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

    private String classificationDetails = "";
    private String details = "";
    private String metrics = "";
    private String pythonOutput = "";
    private String pythonErrorOutput = "";
    private long executionTime = 0;

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
            boolean finished = process.waitFor(120, TimeUnit.SECONDS); // Adjust timeout if necessary
            if (!finished) {
                process.destroy();
                logger.error("Python script execution timed out.");
                redirectAttributes.addFlashAttribute("errorMessage", "Python script execution timed out.");
                return "redirect:/op";
            }

            // Capture script output and error streams
            this.pythonOutput = captureOutput(process.getInputStream());
            this.pythonErrorOutput = captureOutput(process.getErrorStream());

            logger.info("Python script output: {}", pythonOutput);
            if (!pythonErrorOutput.isEmpty()) {
                logger.error("Python script error output: {}", pythonErrorOutput);
            }

            if (pythonErrorOutput == null || pythonErrorOutput.isEmpty()) {
                this.pythonErrorOutput = null;  // Set to null when there is no error output
            }


            // Update class-level attributes
            this.classificationDetails = classificationDetails;
            this.details = details;
            this.metrics = metrics;

            // Calculate and store the execution time
            long endTime = System.currentTimeMillis();
            this.executionTime = endTime - startTime;

            // Clean up temporary files
            Files.deleteIfExists(trainFilePath);
            Files.deleteIfExists(testFilePath);
            Files.deleteIfExists(tempDir);
            logger.info("Temporary files cleaned up.");

        } catch (IOException e) {
            logger.error("File handling error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "File handling error: " + e.getMessage());
            return "redirect:/op";
        } catch (InterruptedException e) {
            logger.error("Script execution interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            redirectAttributes.addFlashAttribute("errorMessage", "Script execution interrupted.");
            return "redirect:/op";
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error: " + e.getMessage());
            return "redirect:/op";
        }

        return "redirect:/op";
    }

    @GetMapping("/op")
    public String showOpPage(Model model) {
        model.addAttribute("classificationDetails", this.classificationDetails);
        model.addAttribute("details", this.details);
        model.addAttribute("metrics", this.metrics);
        model.addAttribute("pythonOutput", this.pythonOutput);
        model.addAttribute("pythonErrorOutput", this.pythonErrorOutput);
        model.addAttribute("executionTime", this.executionTime);

        logger.info("Data passed to the /op page: {}", model);
        return "op";  // Return the output page
    }

    // Helper method to capture output from input streams
    private String captureOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
