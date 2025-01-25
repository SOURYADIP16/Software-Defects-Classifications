package com.ProjectFinalYr.CSE.registrationlogin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PythonScriptService {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptService.class);

    @Value("${python.script.path}")
    private String pythonScriptPath;

    @Value("${static.location}")
    private String staticLocation;

    @Async
    public CompletableFuture<PythonScriptResult> runPythonScriptAsync(
            String classificationDetails,
            String details,
            String metrics,
            MultipartFile trainFile,
            MultipartFile testFile) {

        PythonScriptResult result = new PythonScriptResult();
        result.setClassificationDetails(classificationDetails);
        result.setDetails(details);
        result.setMetrics(metrics);

        Path tempDir = null;
        long startTime = System.currentTimeMillis();

        try {
            // Create a temporary directory to store uploaded files
            tempDir = Files.createTempDirectory("datasets");

            Path trainFilePath = tempDir.resolve(trainFile.getOriginalFilename());
            Path testFilePath = tempDir.resolve(testFile.getOriginalFilename());
            trainFile.transferTo(trainFilePath.toFile());
            testFile.transferTo(testFilePath.toFile());

            logger.info("Temporary files stored: {} and {}", trainFilePath, testFilePath);

            // Execute the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", pythonScriptPath, trainFilePath.toString(), testFilePath.toString());
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr
            Process process = processBuilder.start();

            logger.info("Executing Python script: {}", pythonScriptPath);

            // Wait for the script to finish with a timeout
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                logger.error("Python script execution timed out.");
                result.setErrorMessage("Python script execution timed out.");
                return CompletableFuture.completedFuture(result);
            }

            // Capture script output
            String output = captureOutput(process.getInputStream());
            result.setPythonOutput(output);
            result.setPythonErrorOutput(captureOutput(process.getErrorStream()));

            // Log the output from the script
            logger.info("Python script output: {}", output);

            if (result.getPythonErrorOutput() != null && !result.getPythonErrorOutput().isEmpty()) {
                logger.error("Python script error output: {}", result.getPythonErrorOutput());
            }

            // Calculate execution time
            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);

            logger.info("Python script executed successfully in {} ms.", result.getExecutionTime());

        } catch (IOException e) {
            logger.error("File handling error: {}", e.getMessage(), e);
            result.setErrorMessage("File handling error: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Script execution interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            result.setErrorMessage("Script execution interrupted.");
        } finally {
            // Clean up temporary files
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .map(Path::toFile)
                            .forEach(File::delete);
                    logger.info("Temporary files cleaned up.");
                } catch (IOException e) {
                    logger.error("Error cleaning up temporary files: {}", e.getMessage(), e);
                }
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    private String captureOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
