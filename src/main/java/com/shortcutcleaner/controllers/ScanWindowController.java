package com.shortcutcleaner.controllers;

import java.nio.file.Path;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import mslinks.ShellLink;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ScanWindowController {

    private static List<File> files = new ArrayList<>();
    private static List<File> filesToDelete = new ArrayList<>();

    @FXML private Button scanButton;
    @FXML private Button deleteButton;
    @FXML private TextArea console;
    @FXML private TextArea list;

    @FXML
    private void initialize(){
        console.setEditable(false);
        list.setEditable(false);
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
                        list.appendText("Target: " + fPath + "\n");
                    }
                } catch (Exception e) {
                    console.appendText("Error: " + f.getName() + " Please check manually\n"
                    +"at " + f.getAbsolutePath() + "\n");

                }  
            }
        }
        if(filesToDelete.size() == 0){
            console.appendText("No shortcuts found\n");
        }
    }

    @FXML
    private void onScanButton(){
        scanButton.setDisable(true);
        deleteButton.setDisable(true);

        Task<Void> scanTask = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                String desktopPath = System.getProperty("user.home") + File.separator +"Desktop";
                File desktop = new File(desktopPath);
                listAllFiles(desktop);
                scanShortcuts(files);

                Runnable enableButtons = new Runnable(){
                    @Override
                    public void run() {
                        scanButton.setDisable(false);
                        deleteButton.setDisable(false);
                    }
                };
                Platform.runLater(enableButtons);
                return null;
            }
        };
        Thread scanThread = new Thread(scanTask);
        scanThread.start();
    }
    @FXML
    private void onDeleteButton(){
        deleteButton.setDisable(true);
        scanButton.setDisable(true);

        Task<Void> deleteTask = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                for (File f : filesToDelete) {
                f.delete();
                }
                
                Runnable deleteCompleteRunnable = new Runnable(){
                    @Override
                    public void run() {
                        filesToDelete.clear();
                        console.appendText("Deleted " + filesToDelete.size() + " shortcuts\n");
                        list.clear();
                        deleteButton.setDisable(false);
                        scanButton.setDisable(false);
                    }
                };
                Platform.runLater(deleteCompleteRunnable);
                return null;
                }
        };
        Thread deleteThread = new Thread(deleteTask);
        deleteThread.start();
    }
}
