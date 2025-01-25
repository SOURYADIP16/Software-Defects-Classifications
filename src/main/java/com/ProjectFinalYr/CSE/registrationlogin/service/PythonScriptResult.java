package com.ProjectFinalYr.CSE.registrationlogin.service;

public class PythonScriptResult {

    private String classificationDetails;
    private String details;
    private String metrics;
    private String pythonOutput;
    private String pythonErrorOutput;
    private long executionTime;
    private String errorMessage;
    private boolean success; // Indicates whether the script executed successfully
    private String scriptPath; // The path of the executed Python script
    private String inputFilePath; // Path of the input dataset file

    // Getters and Setters
    public String getClassificationDetails() {
        return classificationDetails;
    }

    public void setClassificationDetails(String classificationDetails) {
        this.classificationDetails = classificationDetails;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getPythonOutput() {
        return pythonOutput;
    }

    public void setPythonOutput(String pythonOutput) {
        this.pythonOutput = pythonOutput;
    }

    public String getPythonErrorOutput() {
        return pythonErrorOutput;
    }

    public void setPythonErrorOutput(String pythonErrorOutput) {
        this.pythonErrorOutput = pythonErrorOutput;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    @Override
    public String toString() {
        return "PythonScriptResult{" +
                "classificationDetails='" + classificationDetails + '\'' +
                ", details='" + details + '\'' +
                ", metrics='" + metrics + '\'' +
                ", pythonOutput='" + pythonOutput + '\'' +
                ", pythonErrorOutput='" + pythonErrorOutput + '\'' +
                ", executionTime=" + executionTime +
                ", errorMessage='" + errorMessage + '\'' +
                ", success=" + success +
                ", scriptPath='" + scriptPath + '\'' +
                ", inputFilePath='" + inputFilePath + '\'' +
                '}';
    }
}