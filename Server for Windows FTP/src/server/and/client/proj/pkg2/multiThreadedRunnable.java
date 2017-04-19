/*
 * Java server for Windows FTP client
 * Alejandro Ferragut 4968001
 * TBC 4713 U02 – Project 2
 *
 */
package server.and.client.proj.pkg2;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Ale
 */
public class multiThreadedRunnable implements Runnable {

    private Socket sock;
    private String serverDirectory;

    /**
     * constructor for multiThreadedRunnable class to start a new client
     *
     * @param sock - Socket
     * @param serverDirectory - directory assigned to the client
     */
    public multiThreadedRunnable(Socket sock, String serverDirectory) {
        this.sock = sock;
        this.serverDirectory = serverDirectory;

    }

    /**
     * override functions from Runnable, will create a new ServerClient class
     *
     */
    @Override
    public void run() {

        try {
            ServerClient myClient = new ServerClient(sock, serverDirectory);

            myClient.runCommand();//this closes also 
        } catch (IOException ex) {
            System.err.println(" one of the clients unable to connect\n" + ex.getMessage());

        }

    }

}
