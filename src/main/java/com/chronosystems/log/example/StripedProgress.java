package com.chronosystems.log.example;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Displays progress on a striped progress bar */
public class StripedProgress extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) {
		ProgressBar bar = new ProgressBar(0);
		bar.setPrefSize(200, 24);

		Timeline task = new Timeline(
				new KeyFrame(
						Duration.ZERO, 
						new KeyValue(bar.progressProperty(), 0)
				), 
				new KeyFrame(
						Duration.seconds(2),
						new KeyValue(bar.progressProperty(), 1)
				)
		);

		Button button = new Button("Go!");
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				task.playFromStart();
			}
		});

		VBox layout = new VBox(10);
		layout.getChildren().setAll(bar, button);
		layout.setPadding(new Insets(10));
		layout.setAlignment(Pos.CENTER);

		layout.getStylesheets().add(
				StripedProgress.class.getResource("/striped-progress.css")
				.toExternalForm());

		stage.setScene(new Scene(layout));
		stage.show();
	}
}