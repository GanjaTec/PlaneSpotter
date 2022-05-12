package planespotter.display;

import org.jetbrains.annotations.Nullable;
import planespotter.constants.GUIConstants;
import planespotter.controller.Controller;
import planespotter.model.FileMaster;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

import static planespotter.constants.GUIConstants.*;

/**
 * @name Logger
 * @author jml04
 * version 1.0
 *
 * class Logger represents the logger for console this.output
 */
public class Logger extends JFrame implements ComponentListener {

    private final JScrollPane scrollPane;
    private final JTextPane out;
    private final Object mainref;

    /**
     * true when logger is logging (writing to text pane)
     */
    private boolean writing;

    public Logger (@Nullable Object ref) {
        super();
        this.mainref = Objects.requireNonNullElse(ref, this);
        super.setTitle("Logger");
        super.setType(Type.NORMAL);
        super.setSize(700, 400);
        super.setLocationRelativeTo(null);
        super.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        super.setBackground(DEFAULT_BG_COLOR);
        super.setLayout(null);
        super.setAlwaysOnTop(false);
        super.setIconImage(PAPER_PLANE_ICON.getImage());
        super.addComponentListener(this);

        this.out = new JTextPane();
        this.out.setBounds(0, 0, super.getWidth()-25, super.getHeight()-45);
        this.out.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
        this.out.setForeground(DEFAULT_FONT_COLOR);
        this.out.setBorder(MENU_BORDER);
        this.out.setCaretColor(DEFAULT_MAP_ICON_COLOR);
        this.out.setEditable(false);
        this.out.setFont(new Font("Consolas", Font.PLAIN, 12));
        var caret = (DefaultCaret) this.out.getCaret();
        caret.setBlinkRate(10);
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);


        this.scrollPane = new JScrollPane(out);
        this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane.setBounds(5, 5, super.getWidth()-25, super.getHeight()-45);
        var verticalScrollBar = this.scrollPane.getVerticalScrollBar();
        verticalScrollBar.setBounds(verticalScrollBar.getX()+10, verticalScrollBar.getY(),
                                verticalScrollBar.getWidth()-10, verticalScrollBar.getHeight());
        this.scrollPane.setVerticalScrollBar(verticalScrollBar);
        this.scrollPane.setAutoscrolls(true);

        super.add(this.scrollPane);
        super.setVisible(true);
        this.sign(2);
        this.writing = false;
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void errorLog (String txt, @Nullable Object ref) {
        this.logInColor(txt, new Color(240, 40, 20), ref);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void sucsessLog (String txt, @Nullable Object ref) {
        this.logInColor(txt, new Color(40, 220, 70), ref);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void infoLog (String txt, @Nullable Object ref) {
        this.logInColor(txt, new Color(230, 230, 70), ref);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void log (String txt, @Nullable Object ref) {
        this.logInColor(txt, DEFAULT_FONT_COLOR, ref);
    }

    /**
     *
     * @param txt
     * @param col
     * @param ref
     */
    private synchronized void logInColor (String txt, Color col, Object ref) {
        this.sign(0);
        var now = LocalDateTime.now();
        var dateFormat = new DateTimeFormatterBuilder().appendPattern("dd.MM.yyyy").toFormatter();
        var timeFormat = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter();
        var date = now.format(dateFormat);
        var time = now.format(timeFormat);
        this.text(date, DEFAULT_FONT_COLOR);
        this.sign(4);
        this.text(time, DEFAULT_FONT_COLOR);
        this.sign(3); // TODO make Sign values to Enum
        if (ref == null) {
            this.text(mainref.getClass().getSimpleName(), DEFAULT_FONT_COLOR);
        } else {
            this.text(ref.getClass().getSimpleName(), DEFAULT_FONT_COLOR);
        }
        this.sign(1);
        this.text(" " + txt + "\n", col);
        this.sign(2);
    }

    /**
     *
     * @param val
     */
    private void sign (int val) {
        switch (val) {
            case 0 -> this.text("[", DEFAULT_MAP_ICON_COLOR);
            case 1 -> this.text("]", DEFAULT_MAP_ICON_COLOR);
            case 2 -> this.text("> ", DEFAULT_MAP_ICON_COLOR);
            case 3 -> this.text("@", DEFAULT_MAP_ICON_COLOR);
            case 4 -> this.text(", ", DEFAULT_MAP_ICON_COLOR);
        }
    }

    /**
     *
     * @param text
     * @param color
     */
    private void text (String text, Color color) {
        var doc = this.out.getStyledDocument();
        var style = out.addStyle("new", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void close () {
        var loggedText = out.getText();
        new FileMaster().saveLogFile(loggedText);
        this.dispose();
    }

    // overrides / implemented methods

    @Override
    public void componentResized(ComponentEvent e) {
        var parent = e.getComponent();
        scrollPane.setBounds(5, 5, parent.getWidth()-25, parent.getHeight()-45);
        out.setBounds(0, 0, parent.getWidth()-25, parent.getHeight()-45);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }
    @Override
    public void componentShown(ComponentEvent e) {
    }
    @Override
    public void componentHidden(ComponentEvent e) {
    }

}
