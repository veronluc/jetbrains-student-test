package view;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static org.jetbrains.kotlin.cli.common.environment.UtilKt.setIdeaIoUseFallback;

public class EditorLayoutController {

    ScriptEngineManager manager;
    ScriptEngine engine;
    PrintStream printStream;
    ByteArrayOutputStream baos;
    @FXML
    private TextArea script;
    @FXML
    private Button runButton;
    @FXML
    private Button clearScriptButton;
    @FXML
    private Button saveButton;
    @FXML
    private Circle scriptAchievementIndicator;
    @FXML
    private TextField filePath;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private ProgressIndicator loading;

    Service<Void> compilationService;

    public EditorLayoutController() {
        setIdeaIoUseFallback();
        manager = new ScriptEngineManager();
        engine = manager.getEngineByExtension("kts");
        baos = new ByteArrayOutputStream();
        printStream = new PrintStream(baos);
        System.setOut(printStream);
        initCompilationService();
    }

    void initCompilationService() {
        compilationService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws ScriptException {
                        for (CharSequence line : script.getParagraphs()) {
                            engine.eval(line.toString());
                            consoleOutput.appendText(baos.toString());
                            baos.reset();
                        }
                        return null;
                    }
                };
            }
        };
        compilationService.setOnSucceeded(e -> {
            scriptAchievementIndicator.setFill(Color.GREEN);
            loading.setVisible(false);
            runButton.setDisable(false);
            saveButton.setDisable(false);
            filePath.setDisable(false);
            clearScriptButton.setDisable(false);
        });
        compilationService.setOnFailed(e -> {
            scriptAchievementIndicator.setFill(Color.RED);
            loading.setVisible(false);
            runButton.setDisable(false);
            saveButton.setDisable(false);
            filePath.setDisable(false);
            clearScriptButton.setDisable(false);
            consoleOutput.appendText(compilationService.getException().getMessage());
        });
    }

    @FXML
    private void run() {
        printStream.flush();
        baos.reset();
        consoleOutput.setText("");
        loading.setVisible(true);
        runButton.setDisable(true);
        saveButton.setDisable(true);
        filePath.setDisable(true);
        clearScriptButton.setDisable(true);
        compilationService.restart();
    }

    @FXML
    private void saveFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath.getText())));
            writer.write(script.getText());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearScript() {
        script.setText("");
    }
}
