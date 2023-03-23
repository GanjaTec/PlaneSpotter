package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.DefaultColor;
import planespotter.display.UserInterface;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class TextDialog extends JDialog {

    private TextDialog(Frame owner) {
        super(owner, "License");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    public static void showDialog(Frame owner, @NotNull String text) {
        TextDialog dialog = new TextDialog(owner);
        JTextPane textPane = dialog.textPane(text);
        dialog.add(dialog.scrollPane(textPane));
        dialog.setVisible(true);
    }

    private JScrollPane scrollPane(JTextPane textPane) {
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setSize(getSize());
        scrollPane.setSize(getWidth() - 13, getHeight() - 20);
        return scrollPane;
    }

    private JTextPane textPane(@NotNull String text) {
        JTextPane pane = new JTextPane();
        pane.setSize(getSize());
        pane.setEditable(false);
        pane.setBackground(Color.WHITE);
        pane.setFont(UserInterface.DEFAULT_FONT);
        pane.setForeground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
        try {
            pane.getStyledDocument().insertString(0, text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return pane;
    }

}
