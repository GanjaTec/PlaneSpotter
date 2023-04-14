package planespotter.display.models;

import de.gtec.util.threading.Threading;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.DefaultColor;
import planespotter.model.KeyPressListener;
import planespotter.model.nio.client.DataUploader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

public class UploadPane extends JDialog {

    private final JLabel lblHost, lblRunning, lblQueueCount, lblFlow;
    private final JTextField tfHost;

    private final UWPButton btUpload, btLocal;

    private KeyPressListener onEnter;
    private ActionListener onUpload, onDBWriter;

    private ScheduledFuture<?> updateTask;

    private boolean upload;

    public UploadPane(Frame owner, String title, @Nullable KeyPressListener onEnter, @Nullable ActionListener onUpload, @Nullable ActionListener onDBWriter) {
        super(owner, title);
        this.onEnter = onEnter;
        this.onUpload = onUpload;
        this.onDBWriter = onDBWriter;
        this.upload = false;
        setSize(300, 360);
        setLayout(null);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        lblHost = new JLabel("Host:");
        lblHost.setBounds(20, 20, 60, 30);

        tfHost = new JTextField("127.0.0.1");
        tfHost.setBounds(80, 25, 190, 25);

        lblRunning = new JLabel("Running: false");
        lblRunning.setBounds(20, 70, 260, 30);

        lblQueueCount = new JLabel("Queue size: 0");
        lblQueueCount.setBounds(20, 120, 260, 30);

        lblFlow = new JLabel("Flow: 0b/s");
        lblFlow.setBounds(20, 170, 260, 30);

        btUpload = new UWPButton("Start Upload");
        btUpload.setBounds(40, 230, 200, 30);
        btUpload.setEffectColor(Color.WHITE);
        btUpload.setBackground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
        btUpload.setSelectedColor(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());

        btLocal = new UWPButton("Disable local DBWriter");
        btLocal.setBounds(40, 280, 200, 30);
        btLocal.setEffectColor(new Color(250, 30, 20));
        btLocal.setBackground(DefaultColor.DEFAULT_FONT_COLOR.get());
        Color red = new Color(170, 30, 20);
        btLocal.setForeground(red);
        btLocal.setBorder(BorderFactory.createLineBorder(red));
        btLocal.setSelectedColor(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());

        JSeparator[] seps = new JSeparator[]{
                new JSeparator(), new JSeparator(), new JSeparator(), new JSeparator()
        };
        int y = 60;
        for (JSeparator sep : seps) {
            sep.setForeground(Color.BLACK);
            sep.setBounds(10, y, 260, 1);
            y += 50;
            add(sep);
        }

        Stream.of(lblHost, lblQueueCount, lblRunning, lblFlow, tfHost, btUpload, btLocal)
                .forEach(super::add);

        setVisible(false);
    }

    private void updateTask(DataUploader<?> uploader) {
        int size = uploader.getQueueSize();
        int flow = uploader.getFlow();
        int byteFlow = uploader.getByteFlow();
        setRunning(uploader.isRunning());
        setQueueCount(size);
        setFlow(flow, byteFlow);
        repaint();
    }

    public ActionListener getOnDoDBWrite() {
        return onDBWriter;
    }

    public void setOnDoDBWrite(ActionListener onDBWriter) {
        if (this.onDBWriter != null) {
            btLocal.removeActionListener(this.onDBWriter);
        }
        this.onDBWriter = onDBWriter;
        btLocal.addActionListener(onDBWriter);
    }

    public KeyPressListener getOnEnter() {
        return onEnter;
    }

    public void setOnEnter(KeyPressListener onEnter) {
        if (this.onEnter != null) {
            tfHost.removeKeyListener(this.onEnter);
        }
        this.onEnter = onEnter;
        tfHost.addKeyListener(onEnter);
    }

    public ActionListener getOnUpload() {
        return onUpload;
    }

    public void setOnUpload(ActionListener onUpload) {
        btUpload.removeActionListener(this.onUpload);
        this.onUpload = onUpload;
        btUpload.addActionListener(onUpload);
    }

    public String getHost() {
        return tfHost.getText();
    }

    public void setRunning(boolean running) {
        lblRunning.setText("Running: " + running);
    }

    public void setQueueCount(int count) {
        lblQueueCount.setText("Queue Count: " + count);
    }

    public void setFlow(int fps, int bps) {
        String prefix = "Flow: " + fps + " Frame/s, ";
        if (bps >= 1000) {
            int mbps = bps / 1000;
            lblFlow.setText(prefix + mbps + " MB/s");
        } else {
            lblFlow.setText(prefix + bps + " byte/s");
        }
    }

    public void setBtLocalTxt(String txt) {
        btLocal.setText(txt);
    }

    public void setBtUploadTxt(String txt) {
        btUpload.setText(txt);
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public void startUpdating(DataUploader<?> uploader) {
        if (updateTask != null && !updateTask.isDone()) {
            return; // already running
        }
        updateTask = Threading.runRepeated(() -> updateTask(uploader), 0, 100);
    }

    public void stopUpdating() {
        setRunning(false);
        if (updateTask == null) {
            return;
        }
        updateTask.cancel(true);
    }

}
