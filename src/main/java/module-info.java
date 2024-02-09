module com.shortcutcleaner {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires mslinks;
    
    opens com.shortcutcleaner to javafx.fxml;
    opens com.shortcutcleaner.controllers to javafx.fxml;

    exports com.shortcutcleaner.controllers;
    exports com.shortcutcleaner;
}
