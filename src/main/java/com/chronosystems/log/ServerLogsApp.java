/**
 * 
 */
package com.chronosystems.log;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author andre.silva
 *
 */
public class ServerLogsApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final Image logo = new Image(ServerLogsApp.class.getResourceAsStream("/logo.png"));
		stage.getIcons().add(logo);
        stage.setTitle("Monitoramento de LOGs");
        stage.setResizable(false);

        initRootLayout(stage);
	}

	/**
     * Inicializa o root layout (layout base).
     */
    public void initRootLayout(Stage stage) {
        try {
            // Carrega o root layout do arquivo fxml.
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ServerLogsApp.class.getResource("view/ServerLogsView.fxml"));

            final VBox root = (VBox) loader.load();
            //final ServerLogsController controller = loader.<ServerLogsController>getController();

            // Mostra a scene (cena) contendo oroot layout.
            final Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}