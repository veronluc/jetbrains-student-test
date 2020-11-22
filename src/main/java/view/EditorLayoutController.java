package view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class EditorLayoutController {

    @FXML
    private TextArea script;
    @FXML
    private Button clearScriptButton;
    @FXML
    private Button saveAndRunButton;
    @FXML
    private Circle scriptAchievementIndicator;
    @FXML
    private TextField filePath;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private ProgressIndicator loading;

    Service<Void> compilationService;
    ProcessBuilder processBuilder;

    public EditorLayoutController() {
        initCompilationService();
        processBuilder = new ProcessBuilder();
    }

    void initCompilationService() {
        compilationService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            processBuilder.command("kotlinc", "-script", filePath.getText()).redirectErrorStream(true);
                            Process process = processBuilder.start();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                consoleOutput.appendText(line + "\n");
                            }
                            int exitVal = process.waitFor();
                            if (exitVal == 0) {
                                handleCompilationEnd(Color.GREEN);
                            } else {
                                handleCompilationEnd(Color.RED);
                            }
                        } catch (IOException | InterruptedException e) {
                            consoleOutput.appendText("Exception error: \t" + e.getMessage() + "\n");
                        }
                        return null;
                    }
                };
            }
        };
        compilationService.setOnFailed(e -> {
            handleCompilationEnd(Color.RED);
            consoleOutput.appendText(compilationService.getException().getMessage());
        });
    }

    void prepareCompilation() {
        consoleOutput.setText("");
        scriptAchievementIndicator.setFill(Color.WHITE);
        script.setDisable(true);
        loading.setVisible(true);
        saveAndRunButton.setDisable(true);
        filePath.setDisable(true);
        clearScriptButton.setDisable(true);
    }

    void handleCompilationEnd(Color color) {
        scriptAchievementIndicator.setFill(color);
        script.setDisable(false);
        loading.setVisible(false);
        saveAndRunButton.setDisable(false);
        filePath.setDisable(false);
        clearScriptButton.setDisable(false);
    }

    @FXML
    private void saveAndRun() {
        if (saveFile()) {
            runCompilation();
        }
    }

    @FXML
    private void runCompilation() {
        prepareCompilation();
        compilationService.restart();
    }

    @FXML
    private boolean saveFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath.getText())));
            writer.write(script.getText());
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            scriptAchievementIndicator.setFill(Color.RED);
            consoleOutput.setText("Error while saving file");
            return false;
        }
    }

    @FXML
    private void clearScript() {
        script.setText("");
    }
}
