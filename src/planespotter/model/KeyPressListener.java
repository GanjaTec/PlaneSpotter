package planespotter.model;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@FunctionalInterface
public interface KeyPressListener extends KeyListener {

    @Override default void keyReleased(KeyEvent e) {};

    @Override default void keyTyped(KeyEvent e) {};
}
