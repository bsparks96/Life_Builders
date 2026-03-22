/**
 * 
 */
/**
 * 
 */
module LifeBuilders_FrontEnd {
	requires javafx.controls;
    requires javafx.fxml;
	requires javafx.base;
	requires javafx.graphics;
	requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
	requires java.net.http;

    opens application to javafx.graphics, javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to com.fasterxml.jackson.databind;
}