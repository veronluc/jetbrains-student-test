import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Jetbrains students test");
        initRootLayout();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/RootLayout.fxml"));
            AnchorPane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            URL keywordFileUrl = this.getClass().getClassLoader().getResource("keywords.css");
            if (keywordFileUrl != null) {
                scene.getStylesheets().add(keywordFileUrl.toExternalForm());
            }
            primaryStage.setMaximized(true);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}