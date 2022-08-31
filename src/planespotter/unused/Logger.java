package planespotter.unused;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.DefaultColor;
import planespotter.model.io.FileWizard;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

import static planespotter.constants.DefaultColor.*;
import static planespotter.constants.Images.PAPER_PLANE_ICON;

/**
 * @name Logger
 * @author jml04
 * version 1.0
 *
 * class Logger represents a logger which logs
 * on a JTextPane in an external JFrame
 */
public class Logger extends JFrame implements ComponentListener {

    private final JScrollPane scrollPane;
    private final JTextPane out;
    private final Object mainRef;

    /**
     * true when logger is logging (writing to text pane)
     */
    public <R> Logger(@Nullable R ref) {
        super();
        this.mainRef = Objects.requireNonNullElse(ref, this);
        super.setTitle("Logger");
        super.setType(Type.NORMAL);
        super.setSize(700, 400);
        super.setLocationRelativeTo(null);
        super.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        super.setBackground(DEFAULT_BG_COLOR.get());
        super.setLayout(null);
        super.setAlwaysOnTop(false);
        super.setIconImage(PAPER_PLANE_ICON.get().getImage());
        super.addComponentListener(this);

        this.out = new JTextPane();
        this.out.setBounds(0, 0, super.getWidth()-25, super.getHeight()-45);
        this.out.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.out.setForeground(DEFAULT_FONT_COLOR.get());
        this.out.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
        this.out.setCaretColor(DEFAULT_MAP_ICON_COLOR.get());
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
        super.setVisible(false);
        this.sign(2);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void errorLog(@NotNull String txt, @Nullable Object ref) {
        this.logInColor(txt, new Color(240, 40, 20), ref);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void successLog(@NotNull String txt, @Nullable Object ref) {
        this.logInColor(txt, new Color(40, 220, 70), ref);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void infoLog(@NotNull String txt, @Nullable Object ref) {
        this.logInColor(txt, new Color(230, 230, 70), ref);
    }

    /**
     *
     * @param txt
     * @param ref
     */
    public void log(@NotNull String txt, @Nullable Object ref) {
        this.logInColor(txt, DEFAULT_FONT_COLOR.get(), ref);
    }

    /**
     *
     *
     * @param txt
     * @param ref
     */
    public void debug(@NotNull String txt, @Nullable Object ref) {
        this.logInColor("[DEBUG] " + txt, DEFAULT_BORDER_COLOR.get(), ref);
    }

    /**
     *
     *
     * @param txt
     * @param col
     * @param ref
     */
    private synchronized void logInColor(String txt, Color col, Object ref) {
        this.sign(0);
        var now = LocalDateTime.now();
        var dateFormat = new DateTimeFormatterBuilder().appendPattern("dd.MM.yyyy").toFormatter();
        var timeFormat = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter();
        var date = now.format(dateFormat);
        var time = now.format(timeFormat);
        this.text(date, DEFAULT_FONT_COLOR.get());
        this.sign(4);
        this.text(time, DEFAULT_FONT_COLOR.get());
        this.sign(3); // TODO make Sign values to Enum
        if (ref == null) {
            this.text(mainRef.getClass().getSimpleName(), DEFAULT_FONT_COLOR.get());
        } else {
            this.text(ref.getClass().getSimpleName(), DEFAULT_FONT_COLOR.get());
        }
        this.sign(1);
        this.text(" " + txt + "\n", col);
        this.sign(2);
    }

    /**
     *
     * @param val is the sign index:
     *            0: [
     *            1: ]
     *            2: >
     *            3: @
     *            4: ,
     */
    private void sign(int val) {
        switch (val) {
            case 0 -> this.text("[", DEFAULT_MAP_ICON_COLOR.get());
            case 1 -> this.text("]", DEFAULT_MAP_ICON_COLOR.get());
            case 2 -> this.text("> ", DEFAULT_MAP_ICON_COLOR.get());
            case 3 -> this.text("@", DEFAULT_MAP_ICON_COLOR.get());
            case 4 -> this.text(", ", DEFAULT_MAP_ICON_COLOR.get());
        }
    }

    /**
     *
     * @param text
     * @param color
     */
    private void text(String text, Color color) {
        var doc = this.out.getStyledDocument();
        var style = out.addStyle("new", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void close(boolean saveLog) {
        if (saveLog) {
            String loggedText = out.getText();
            FileWizard.getFileWizard().saveLogFile("logged", loggedText);
        }
        super.dispose();
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
