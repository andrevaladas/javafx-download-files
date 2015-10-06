/**
 * 
 */
package com.chronosystems.log.controller;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import org.apache.commons.io.FilenameUtils;

import com.chronosystems.log.helper.PropertiesHelper;
import com.chronosystems.log.model.Environment;
import com.chronosystems.log.service.ServerLogsService;

/**
 * @author andre.silva
 *
 */
public class ServerLogsController implements Initializable {

	private static final String LOG_SERVER_PATH = "log.server.path";
	private static final String LOG_SERVER_ADDRESS = "log.server.address.";

	private ObservableList<Environment> serverComboBoxData = FXCollections.observableArrayList();

	@FXML
	private TextField outputFolderTextField;

	@FXML
	private ComboBox<Environment> serverComboBox;

	@FXML
	private Button downloadFilesButton;
	
	@FXML
	private ProgressIndicator progressIndicator;
	
	@FXML
	private Label statusBar;

	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		initData();
		initComponents();
	}

	private void initData() {

		final List<String> serverList = PropertiesHelper.getProperties(LOG_SERVER_PATH);
		for (String serverName : serverList) {
			serverComboBoxData.add(new Environment(serverName, true));
			final List<String> addressList = PropertiesHelper.getProperties(LOG_SERVER_ADDRESS + serverName);
			for (String serverAddress : addressList) {
				serverComboBoxData.add(new Environment(serverName, serverAddress));
			}
			serverComboBoxData.add(new Environment());//line separator
		}
		if (serverComboBoxData.size() > 0) {
			serverComboBoxData.remove(serverComboBoxData.size()-1);
		}

		// Init ComboBox items.
		serverComboBox.setItems(serverComboBoxData);
	}

	private void initComponents() {
		progressIndicator.setVisible(false);
		
		// Define rendering of the list of values in1 ComboBox drop down. 
		serverComboBox.setCellFactory((comboBox) -> {
		    return new ListCell<Environment>() {
		        @Override
		        protected void updateItem(Environment item, boolean empty) {
		            super.updateItem(item, empty);

		            if (item == null || empty) {
		                setText(null);
		            } else {
		                setText(item.toString());
		            }
		        }
		    };
		});

		// Define rendering of selected value shown in ComboBox.
		serverComboBox.setConverter(new StringConverter<Environment>() {
		    @Override
		    public String toString(Environment item) {
		        if (item == null) {
		            return null;
		        } else {
		            return item.toString();
		        }
		    }

		    @Override
		    public Environment fromString(String personString) {
		        return null; // No conversion fromString needed.
		    }
		});

		// Handle ComboBox event.
    	serverComboBox.setOnAction((event) -> {
    		//final Environment selectedItem = serverComboBox.getSelectionModel().getSelectedItem();
    	    //System.out.println("ComboBox Action (selected: " + selectedItem.toString() + ")");
    	});
	}

	@FXML
    private void handleButtonAction(ActionEvent event) {
        // Button was clicked, do something...
		final Environment selectedItem = serverComboBox.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {

			if (selectedItem.isAll()) {
				final ObservableList<Environment> items = serverComboBox.getItems();
				final List<Environment> environments = new LinkedList<Environment>();
				final String serverPath = selectedItem.getPath();
				for (Environment item : items) {
					if (serverPath.equals(item.getPath()) && item.isValid()) {
						environments.add(item);
					}
				}
				statusBar.setText("Status: Searching files...");
				downloadItems(environments.toArray(new Environment[]{}));
			} else if (selectedItem.isValid()) {

				statusBar.setText("Status: Searching files...");
				downloadItems(selectedItem);

			} else {
				statusBar.setText("Status: Select a Server for download!");
			}
		} else {
			statusBar.setText("Status: Select a Server for download!");
		}
    }

	private void downloadItems(Environment...environments) {

        if (progressIndicator.isVisible()) {
            return;
        }

        final String outputFolder = outputFolderTextField.getText();

        // scan all servers
        for (Environment environment : environments) {

        	// clears the list items and start displaying the loading indicator at the Application Thread
        	if (!progressIndicator.isVisible()) {
        		checkComponentsControl();
        	}

        	List<String> downloadList;
        	try {
        		downloadList = ServerLogsService.collectLinksToDownload(environment);
        	} catch (IOException e) {
        		statusBar.setText(e.getMessage());
        		checkComponentsControl();
        		continue;
        	}
        	
        	final List<String> completeList = new LinkedList<>();
        	
        	// loads the items at another thread, asynchronously
        	Task<List<String>> listLoader = new Task<List<String>>() {
        		{
        			setOnSucceeded(workerStateEvent -> {
        	        	if (progressIndicator.isVisible()) {
        	        		checkComponentsControl(); // stop displaying the loading indicator
        	        	}
        				statusBar.setText(String.format("Status: %s files downloaded successfully!", completeList.size()));
        			});
        			
        			setOnFailed(workerStateEvent -> {
        				getException().printStackTrace();
        				System.out.println(getException().getMessage());
        			});
        		}
        		
        		@Override
        		protected List<String> call() throws Exception {
        			// populates the list view with dummy items
        			for (String downloadUrl : downloadList) {
        				updateMessage(downloadUrl);
						ServerLogsService.downloadFiles(environment, outputFolder, downloadUrl);
        				completeList.add(downloadUrl);
        			}
        			return completeList;
        		}
        	};
        	
        	listLoader.messageProperty().addListener(new ChangeListener<String>() {
        		@Override
        		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                	if (!progressIndicator.isVisible()) {
                		checkComponentsControl();
                	}
        			statusBar.setText("Status: Downloading... "+String.format("[%s/%s] => ", completeList.size(), downloadList.size()) + FilenameUtils.getName(newValue));
        		}
        	});

        	final Thread loadingThread = new Thread(listLoader, "list-loader");
        	loadingThread.setDaemon(true);
        	loadingThread.start();
		}
        
    }
	
	private void checkComponentsControl() {
		final boolean disable = !progressIndicator.isVisible();
		progressIndicator.setVisible(disable);
		downloadFilesButton.setDisable(disable);
		outputFolderTextField.setDisable(disable);
		serverComboBox.setDisable(disable);
		downloadFilesButton.setDisable(disable);
	}
}
