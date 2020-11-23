import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class RootLayoutController {

    private final String[] KEYWORDS = new String[]{"var", "val", "fun", "override", "class", "in", "while", "return", "super", "by"};
    private final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private final Pattern PATTERN = Pattern.compile("(?<KEYWORD>" + KEYWORD_PATTERN + ")");

    @FXML
    private CodeArea script;
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

    public RootLayoutController() {
        initCompilationService();
        processBuilder = new ProcessBuilder();
    }

    @FXML
    public void initialize() {
        script.setParagraphGraphicFactory(LineNumberFactory.get(script));
        script.getVisibleParagraphs().addModificationObserver(new VisibleParagraphStyler<>(script, this::computeHighlighting));
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
                                String finalLine = line;
                                Platform.runLater(() -> consoleOutput.appendText(finalLine + "\n"));
                            }
                            int exitVal = process.waitFor();
                            if (exitVal == 0) {
                                Platform.runLater(() -> handleCompilationEnd(Color.GREEN));
                            } else {
                                Platform.runLater(() -> handleCompilationEnd(Color.RED));
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
    }

    void handleCompilationEnd(Color color) {
        scriptAchievementIndicator.setFill(color);
        script.setDisable(false);
        loading.setVisible(false);
        saveAndRunButton.setDisable(false);
        filePath.setDisable(false);
    }

    @FXML
    private void saveAndRun() {
        if (filePath.getText().endsWith(".kts")) {
            if (saveFile()) {
                runCompilation();
            }
        } else {
            consoleOutput.setText("The past must points to a .kts file !");
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

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = "keyword";
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}