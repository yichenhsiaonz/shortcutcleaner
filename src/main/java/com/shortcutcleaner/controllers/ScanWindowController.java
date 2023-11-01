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

    @FXML
    private void initialize(){
        console.setEditable(false);
        list.setEditable(false);
        String desktopPath = System.getProperty("user.home") + File.separator +"Desktop";
        pathTextField.setText(desktopPath);
    }

    private void listAllFiles(File file){
        for (File f : file.listFiles()) {
            try{
if (f.isDirectory()) {
                System.out.println("Directory: " + f.getName());
                listAllFiles(f);
            }else {
                files.add(f);
            }
            } catch(Exception e) {
                console.appendText("Error: " + f.getName() + " Please check manually\n"
                +"at " + f.getAbsolutePath() + "\n");
            }
            
        }
    }

    private void scanShortcuts(List<File> files){
        for (File f : files) {
            System.out.println(f.getName());
            if (f.getName().endsWith(".lnk")) {
                try {
                    ShellLink sl = new ShellLink(f);
                    String targetPathString = sl.resolveTarget();
                    File targetFile = new File(targetPathString);
                    if (!targetFile.isFile() && targetPathString.endsWith(".exe")) {
                        console.appendText("Found: " + f.getName() + "\n");
                        filesToDelete.add(f);
                        list.appendText(f.getName() + "\n");
                        Path fPath = f.toPath();
                        fPath = fPath.toRealPath();
                        list.appendText("Target: " + fPath + "\n\n");
                    }
                } catch (Exception e) {
                    console.appendText("Error: " + f.getName() + " Please check manually\n"
                    +"at " + f.getAbsolutePath() + "\n");

                }  
            }
        }
        if(filesToDelete.size() == 0){
            console.appendText("No shortcuts found\n\n");
        }
    }

    @FXML
    private void onScanButton(){
        String pathString = pathTextField.getText();
        scanButton.setDisable(true);
        deleteButton.setDisable(true);
        folderButton.setDisable(true);
        console.appendText("Scanning: "+ pathString + "\n");
        try{
            files.clear();
            File folderToScan = new File(pathString); 
            if(!folderToScan.exists()){
                throw new Exception();
            }
            listAllFiles(folderToScan);      
            scanShortcuts(files);
        } catch(Exception e){
            console.appendText("Error: Invalid path\n\n");
        }
                
        scanButton.setDisable(false);
        deleteButton.setDisable(false);
        folderButton.setDisable(false);
    }
    @FXML
    private void onDeleteButton(){
        deleteButton.setDisable(true);
        scanButton.setDisable(true);
        int deleteCounter = 0;
        for (File f : filesToDelete) {
            f.delete();
            deleteCounter++;
        }
        filesToDelete.clear();
        deleteButton.setDisable(false);
        scanButton.setDisable(false);
        console.appendText("Deleted " + deleteCounter + " shortcuts\n\n");
        list.clear();
        
    }

    @FXML
    private void onFolderButtonClicked(){
        //open folder dialogue 
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File selectedDirectory = directoryChooser.showDialog(null);
        if(selectedDirectory == null){
            console.appendText("No Directory selected\n\n");
        } else {
            pathTextField.setText(selectedDirectory.getAbsolutePath());
            console.appendText("Selected: " + selectedDirectory.getAbsolutePath() + "\n\n");
        }
        
    }

    @FXML
    private void onDesktopSelected(){
        String desktopPath = System.getProperty("user.home") + File.separator +"Desktop";
        pathTextField.setText(desktopPath);
        console.appendText("Selected: " + desktopPath + "\n\n");
    }

    @FXML
    private void onUserStartSelected(){
        String userStartPath = System.getenv("APPDATA") + File.separator + "Microsoft" + File.separator + "Windows" + File.separator + "Start Menu" + File.separator + "Programs";
        pathTextField.setText(userStartPath);
        console.appendText("Selected: " + userStartPath + "\n\n");
    }

    @FXML
    private void onSystemStartSelected(){
        String systemStartPath = System.getenv("ALLUSERSPROFILE") + File.separator +"Microsoft" + File.separator + "Windows" + File.separator + "Start Menu" + File.separator + "Programs";
        pathTextField.setText(systemStartPath);
        console.appendText("Selected: " + systemStartPath + "\n\n");
    }
}
