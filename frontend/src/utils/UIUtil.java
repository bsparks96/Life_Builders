package utils;

import javafx.scene.Node;
import javafx.scene.control.Control;


public class UIUtil {

    public static void setVisible(Node node, boolean visible) {
    	if (node == null) return;
    	node.setVisible(visible);
        node.setManaged(visible);
    }

    public static void setEnabled(Control control, boolean enabled) {
    	if (control == null) return;
    	control.setDisable(!enabled);
    }
}