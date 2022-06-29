package planespotter.model.nio;

import org.opensky.example.ExampleDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ADSBSupplier implements Supplier{

    private final String host;
    private final int port;

    private boolean running;

    public ADSBSupplier(String ip, int port, boolean run) {
        this.host = ip;
        this.port = port;
        this.running = run;

    }

    @Override
    public void supply() {
        getCon();

        try {
            System.out.println("Reader ready? " + in.ready());
            while (this.running) {
                while (in.ready()) {
                    //String encoded = in.readLine();
                    in.lines()
                            .filter(s -> s.matches("^\\*[A-F0-9]+\\;$"))
                            .map(s -> s.replace("*", "").replace(";", ""))
                            .forEach(this::decode);


                    toFrame();
                    groupFrames();

                }
            }

        } catch (IOException ex) {
            System.out.println("Nothing to be read on the Stream!");
        }
        //printOutput();
        //toFrame();
        //groupFrames();

    }

    private Socket clientSocket;
    private BufferedReader in;
    private int startingDelay = 400;
    public void getCon() {

        try {
            System.out.println("Trying to get Connection...");
            clientSocket = new Socket(this.host, this.port);
            System.out.print("Connection Established!\nStarting InputStream...");
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (!in.ready()) {
                System.out.println(".");
                TimeUnit.MILLISECONDS.sleep(startingDelay);
            }
            System.out.println("InputStream started!");

        } catch (IOException ex) {
            System.out.println("Host not found!");
        } catch (InterruptedException ex) {
            System.out.println("Interrupted while sleeping!");
        }

    }

    public void decode (String raw) {
        ExampleDecoder decoder = new ExampleDecoder();
        long time = System.currentTimeMillis();


        decoder.decodeMsg(time, raw, null);
    }

    public void toFrame(){

    }

    public void groupFrames() {

    }



    public void printOutput() {
        try {
            System.out.println("Reader ready? " + in.ready());
            while (true) {
                while (in.ready()) {
                    System.out.println(in.readLine());

                }
            }

        } catch (IOException ex) {
            System.out.println("Nothing to be read on the Stream!");
        }
    }


}
