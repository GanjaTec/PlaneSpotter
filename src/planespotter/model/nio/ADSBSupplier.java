package planespotter.model.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ADSBSupplier implements Supplier{

    private final String host;
    private final int port;

    public ADSBSupplier(String ip, int port) {
    this.host = ip;
    this.port = port;

    }

    @Override
    public void supply() {
        getCon();

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

    public void decode () {

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
