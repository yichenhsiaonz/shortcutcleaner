package com.shortcutcleaner.controllers;

import java.nio.file.Path;
import java.io.File;
import java.util.List;

import javafx.stage.DirectoryChooser;
import java.util.ArrayList;
import mslinks.ShellLink;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ScanWindowController {

    private static List<File> files = new ArrayList<>();
    private static List<File> filesToDelete = new ArrayList<>();

    @FXML private Button scanButton;
    @FXML private Button deleteButton;
    @FXML private SplitMenuButton folderButton;
    @FXML private TextArea console;
    @FXML private TextArea list;
    @FXML private TextField pathTextField;

    private String userDesktopPath;
    private String systemDesktopPath;
    private String userStartPath;
    private String systemStartPath;

    @FXML
    private void initialize(){
        // set text areas to read only
        console.setEditable(false);
        list.setEditable(false);


        // set default paths
        userDesktopPath = System.getProperty("user.home") + File.separator +"Desktop";
        systemDesktopPath = System.getenv("PUBLIC") + File.separator +"Desktop";
        userStartPath = System.getenv("APPDATA") + File.separator + "Microsoft" + File.separator + "Windows" + File.separator + "Start Menu" + File.separator + "Programs";
        systemStartPath = System.getenv("ALLUSERSPROFILE") + File.separator +"Microsoft" + File.separator + "Windows" + File.separator + "Start Menu" + File.separator + "Programs";
        pathTextField.setText(userDesktopPath);
    }

    private void listAllFiles(File directiory){
        Platform.runLater(() -> {
            console.appendText("Scanning: " + directiory.getName() + "\n");
        });
        // loop through all files in directory "directory"
        for (File f : directiory.listFiles()) {
            try{
                if (f.isDirectory()) {
                    // if file is a directory, call this method recursively
                    listAllFiles(f);
                } else {
                    files.add(f);
                }
            } catch(Exception e) {
                Platform.runLater(() -> {
                    console.appendText("Error: " + f.getName() + " Please check manually\n"
                    +"at " + f.getAbsolutePath() + "\n");
                });
            }
            
        }
    }

    private void scanShortcuts(List<File> files){
        for (File f : files) {
            if (f.getName().endsWith(".lnk")) {
                try {
                    // use mslinks library to resolve shortcut target
                    ShellLink sl = new ShellLink(f);
                    String targetPathString = sl.resolveTarget();
                    File targetFile = new File(targetPathString);
                    // if the target does not exist and the shortcut is to an exe file, add it to the list of shortcuts to delete
                    if (!targetFile.isFile() && targetPathString.endsWith(".exe")) {
                        Platform.runLater(() -> {
                            try{
                                console.appendText("Found: " + f.getName() + "\n");
                                filesToDelete.add(f);
                                list.appendText(f.getName() + "\n");
                                Path fPath = f.toPath();
                                fPath = fPath.toRealPath();
                                list.appendText("Target: " + fPath + "\n\n");
                            } catch(Exception e) {
                                console.appendText("Error: " + f.getName() + " Please check manually\n"
                                +"at " + f.getAbsolutePath() + "\n");
                            }
                            
                        });
                    }
                } catch (Exception e) {
                    console.appendText("Error: " + f.getName() + " Please check manually\n"
                    +"at " + f.getAbsolutePath() + "\n");

                }  
            }
        }
        if(filesToDelete.size() == 0){
            console.appendText("No dead shortcuts found\n\n");
        }
    }

    @FXML
    private void onScanButton(){
        String pathString = pathTextField.getText();
        disableAllButtons();
        console.appendText("Scanning: "+ pathString + "\n");
        files.clear();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try{
                    // check if path is valid
                    File folderToScan = new File(pathString); 
                    if(!folderToScan.exists()){
                        throw new Exception();
                    }
                    listAllFiles(folderToScan);         
                } catch(Exception e){
                    Platform.runLater(() -> {
                        console.appendText("Error: Invalid path\n\n");
                    });
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            console.appendText("\nScan complete\n\n");
            scanShortcuts(files);
            enableAllButtons();
        });
        new Thread(task).start();
    }
    @FXML
    private void onDeleteButton(){
        disableAllButtons();
        int deleteCounter = 0;
        for (File f : filesToDelete) {
            f.delete();
            deleteCounter++;
        }
        filesToDelete.clear();
        enableAllButtons();
        console.appendText("Deleted " + deleteCounter + " shortcuts\n\n");
        list.clear();
        
    }

    @FXML
    private void onFolderButtonClicked(){
        //open folder dialogue 
        DirectoryChooser directoryChooser = new DirectoryChooser();
        // set initial directory to the path in the text field if it exists, otherwise set it to the user's desktop
        if(new File(pathTextField.getText()).exists()){
            directoryChooser.setInitialDirectory(new File(pathTextField.getText()));
        } else {
            directoryChooser.setInitialDirectory(new File(userDesktopPath));
        }
        directoryChooser.setTitle("Select Folder");
        File selectedDirectory = directoryChooser.showDialog(null);
        // if no directory is selected, do nothing
        if(selectedDirectory == null){
            console.appendText("No Directory selected\n\n");
        // if a directory is selected, set the text field to the path of the selected directory
        } else {
            pathTextField.setText(selectedDirectory.getAbsolutePath());
            console.appendText("Selected: " + selectedDirectory.getAbsolutePath() + "\n\n");
        }
        
    }

    @FXML
    private void onUserDesktopSelected(){
        pathTextField.setText(userDesktopPath);
        console.appendText("Selected: " + userDesktopPath + "\n\n");
    }

    @FXML
    private void onSystemDesktopSelected(){
        pathTextField.setText(systemDesktopPath);
        console.appendText("Selected: " + systemDesktopPath + "\n\n");
    }

    @FXML
    private void onUserStartSelected(){
        pathTextField.setText(userStartPath);
        console.appendText("Selected: " + userStartPath + "\n\n");
    }

    @FXML
    private void onSystemStartSelected(){
        pathTextField.setText(systemStartPath);
        console.appendText("Selected: " + systemStartPath + "\n\n");
    }

    private void disableAllButtons(){
        scanButton.setDisable(true);
        deleteButton.setDisable(true);
        folderButton.setDisable(true);
    }

    private void enableAllButtons(){
        scanButton.setDisable(false);
        deleteButton.setDisable(false);
        folderButton.setDisable(false);
    }
}
