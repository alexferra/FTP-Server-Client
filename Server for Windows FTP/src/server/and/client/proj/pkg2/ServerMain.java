/*
 * Java server for Windows FTP client
 * Alejandro Ferragut 4968001
 * TBC 4713 U02 – Project 2
 *
 */
package server.and.client.proj.pkg2;

import java.net.*;
import java.io.*;

public class ServerMain {

    //this is to avoid users to access the entire C: disk
    static String serverDirectory = "C:\\server";

    /**
     * creates a serverSocket on port 21, the server supports multiple
     * connections via threads
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
//initiate serverSocket
            serverSocket = new ServerSocket(21);

        } catch (IOException e) {
            System.err.println("Could not listen on port: 21.");
            System.exit(1);
        }

        System.out.println("Waiting for connection.....");

        boolean runing = true;
        Socket sok = null;

        //server loop:
        while (runing) {
            try {
                sok = serverSocket.accept();

                System.out.println("connected");

                new Thread(new multiThreadedRunnable(sok, serverDirectory)).start();

            } catch (Exception e) {
                System.err.println("expetion e");
            }

        }

        //for one client only:
//        Socket sok = serverSocket.accept();
//        ServerClient client = new ServerClient(sok);
//        client.runCommand();
        serverSocket.close();
    }//end main

}
