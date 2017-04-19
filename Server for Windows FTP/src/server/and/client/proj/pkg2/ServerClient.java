/*
 * Java server for Windows FTP client
 * Alejandro Ferragut 4968001
 * TBC 4713 U02 – Project 2
 *
 */
package server.and.client.proj.pkg2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.StringTokenizer;

/**
 *
 * @author Ale
 */
public class ServerClient {

    private String ip, port, ipPortInfo;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Socket clientSocket = null;
    private String currentDirectory;

    /**
     * constructor
     *
     * @param serverSocket
     * @param serverDirectory - the directory from the server to work on
     * @throws IOException
     */
    public ServerClient(Socket serverSocket, String serverDirectory) throws IOException {

        clientSocket = serverSocket;
        currentDirectory = serverDirectory;

        System.out.println("Connection successful");
        System.out.println("Waiting for input.....");

        out = new PrintWriter(clientSocket.getOutputStream(),
                true);

        in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
    }

    /**
     * it will create a file if one does not exist already for the server
     *
     * @param input directory name to be created under C:\\ disk
     * @param path - all the server files will be inside here
     */
    public void createDirectory(String input, String path) {


        /*if the user doesn't have a space allocated in the server, one will be
         alocated OR incase all users access the same info: separate file created*/
        File main = new File(path);
        setcurrentDirectory(path);

        if (!main.exists()) {//if it doesn't exist
            if (main.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        /*all files created are inside the user's space in the server
         OR all files created inside server's folder, avoids users to access
         other unwanted files in server*/
        File file = new File(path + "\\" + input);

        if (!file.exists()) {

            if (file.mkdir()) {

                sendClient("Directory is created!");

            } else {
                sendClient("Failed to create directory!");
            }
        } else {
            sendClient("550 Create directory operation failed.");
        }

    }//end createDirectory

    /**
     * It will allow the user to go back , or into a new directory. Won't allow
     * user to access C:\\ only the server file
     *
     * @param current - Directory
     * @param change - desired new folder
     */
    public void changeDirectory(String current, String change) {

        //check if user wants to go back, do not allow to access C:\\ 
        if (change.equals("..")) {// if cd .. go back one directory 
            currentDirectory = "C:\\server";

        } else {

            //also include go to default directory: cd \  goes to C:\\server
            currentDirectory += "\\" + change;

        }

        sendClient("changing directory to: "
                + currentDirectory + "\n you can type cd .. to go to main directory");
    }//end changeDirectory

    /**
     * extracts the IPv6 and port number from the provided string and stores
     * them in IP and port variables
     *
     * @param info
     */
    public void IPv6andPortInfo(String info) {
        int start, end;

        start = 3; // format is |2|::1|65256| , always 1 or 2 first

        end = info.indexOf("|", start);

        ip = info.substring(start, end);

        start = end + 1;
        end = info.indexOf("|", start);

        port = info.substring(start, end);

    }//end IPv6andPortInfo

    /**
     *
     * @param filename - to be transfered over a new socket
     * @throws UnknownHostException
     * @throws IOException
     */
    public void getFile(String filename) throws UnknownHostException, IOException {

        String fileToSend = getCurrentDirectory() + "\\" + filename;  //change to variable

        // ServerClient.IPv6andPortInfo(filename);
        InetAddress address = InetAddress.getByName(ip);

        SocketAddress sokAddress = new InetSocketAddress(address, Integer.parseInt(port));

        Socket sock = new Socket();
        sock.connect(sokAddress);

        FileInputStream fInS = null;
        BufferedInputStream bInS = null;
        OutputStream outS = null;

        File myFile = new File(fileToSend);
        byte[] fileArray = new byte[(int) myFile.length()];
        fInS = new FileInputStream(myFile);
        bInS = new BufferedInputStream(fInS);
        bInS.read(fileArray, 0, fileArray.length);

        outS = sock.getOutputStream();

        System.out.println("Sending " + fileToSend + "(" + fileArray.length + " bytes)");

        outS.write(fileArray, 0, fileArray.length);
        outS.flush();

        sendClient("226 done file transfer");
        sock.close();

    }//end getFile

    /**
     * stores the given file into the server
     *
     * @param filename
     * @throws IOException
     */
    public void putFile(String filename) throws IOException {

        FileOutputStream fOutStream = null;
        BufferedOutputStream bOutStream = null;
        int readBytes;
        int current = 0;

        InetAddress address = InetAddress.getByName(ip);
        SocketAddress sokAddress = new InetSocketAddress(address, Integer.parseInt(port));
        Socket sock = new Socket();

        //hard wired for purpose of the project, 600kb max file
        int fileSize = 6022386;

        sock.connect(sokAddress);

        try {

            System.out.println("connecting");

            //receiving file
            byte[] fileArray = new byte[fileSize];
            InputStream is = sock.getInputStream();
            fOutStream = new FileOutputStream(currentDirectory + "\\" + filename);
            bOutStream = new BufferedOutputStream(fOutStream);
            readBytes = is.read(fileArray, 0, fileArray.length);
            current = readBytes;

            do {
                readBytes
                        = is.read(fileArray, current, (fileArray.length - current));
                if (readBytes >= 0) {
                    current += readBytes;
                }
            } while (readBytes > -1);

            bOutStream.write(fileArray, 0, current);
            bOutStream.flush();
            System.out.println("File " + filename
                    + " downloaded (" + current + " bytes read)");
        } catch (Exception e) {
            System.out.println("Exception (2): " + e.getMessage());
        } finally {
            if (fOutStream != null) {
                fOutStream.close();
            }
            if (bOutStream != null) {
                bOutStream.close();
            }

        }
        sock.close();
        sendClient("226 done file transfer");

    }//end putFile

    /**
     * println() to client
     *
     * @param info - to be sent to client
     */
    public void sendClient(String info) {

        if (out != null) {

            out.println(info);

        } else {
            System.err.println("not initialized - sendClient method\n ");

        }
    }//end sendClient

    /**
     * decides if user enters correct username . According to instructions it
     * will allow any client name
     *
     * @param user - typed by client
     * @param out - PrintWriter to client
     * @return true if its a user in the server
     */
    public boolean usernameValidation(String user, PrintWriter out) {

        //336	Username okay, need password.
        sendClient("336 Correct Username");

        //saves the username for later use:
        StringTokenizer tok = new StringTokenizer(user);
        tok.nextToken();
        username = tok.nextToken();

        return true;

    }//end usernameValidation

    /**
     * will allow a client as long as they typed the same username and password
     *
     * @param password
     * @param out
     * @return
     */
    public boolean passwordValidation(String password, PrintWriter out) {

        //need to verify that matches user
        if (password.equals("PASS " + username)) {
            /*230 - This status code appears after the client sends the correct
             password. It indicates that the password has successfully logged on.*/
            sendClient("230 Logged in sucessful");

            return true;
        }

        sendClient("Invalid password  - Typed: " + password);
        return false;

    }//end passwordValidation

    /**
     * closes the client connection
     *
     * @throws IOException
     */
    public void closeClientConnection() throws IOException {

        out.close();
        in.close();
        clientSocket.close();
    }

    /**
     *
     *
     *
     * Client loop
     *
     *
     *
     *
     *
     * this is where the client's loop will run
     *
     * @throws java.io.IOException
     */
    public void runCommand() throws IOException {
        String inputLine;

        sendClient("220 Please input your Username: "
                + "\nRemember user can only access: C:\\server");

        while ((inputLine = in.readLine()) != null) {

            //breakes input into command string and filename/directoryname 
            StringTokenizer tok = new StringTokenizer(inputLine);
            String command = tok.nextToken();

            //breakes input into command string and filename/directoryname               
            String filename = null;

            //will output everything received from client:
            System.out.println("Server: " + inputLine);

            if (inputLine.equals("quit")) {
                sendClient("QUIT");
                break;//exist if user inputs quit
            }

            switch (command) {

                //user loggin:
                case ("USER"):

                    usernameValidation(inputLine, getOut());
                    break;

                //check for PASSWORD:
                case ("PASS"):

                    if (!passwordValidation(inputLine, getOut())) {
                        //wrong password             
                        exit(0);
                    }
                    break;

                //mkdir-create a new directory :
                case ("XMKD"):
                    //second word from command, avoid for one line commands                       
                    filename = tok.nextToken();

                    /*change serverDirectoy to username and then every 
                     user will have their own directory/file*/
                    createDirectory(filename, currentDirectory);
                    break;
                //cd- change directory :
                case ("CWD"):
                    //second word from command, avoid for one line commands                       
                    filename = tok.nextToken();

                    changeDirectory(currentDirectory, filename);

                    break;

                /*ls- list all the files in the user directory :
                 this was not required for the project, completed
                 to ease server navigation.  */
                case ("NLST"):

                    File file = new File(currentDirectory);

                    File[] files = file.listFiles();
                    //make sure theres atleast 1 folder
                    if (files.length > 0) {
                        for (int i = 0; i < files.length; i++) {
                            sendClient("file names: " + files[i].getName());

                        }
                    } else {
                        sendClient("Folder is empty");
                        System.out.println("RAN THIS: " + inputLine);
                    }
                    break;
                // return the working directory of the cient:
                case ("XPWD"):
                    sendClient("Your Current directory is: " + currentDirectory);

                    break;
                // delete file from server : 
                case ("DELE"):
                    if (currentDirectory != null) {
                        filename = tok.nextToken();//gets filename
                        try {
                            File fileDeletion
                                    = new File(currentDirectory + "\\" + filename);
                            Files.delete(fileDeletion.toPath());
                            System.out.println("file deleted");
                            sendClient("sucessfuly deleted");
                        } catch (Exception e) {
                            sendClient("Error while deleting file");
                        }

                    } else {
                        sendClient("Invalid directory to delete");
                    }
                    break;

                //needed for ls , get and put:
                case ("EPRT"):
                    sendClient("200");
                    filename = tok.nextToken();//gets filename, in this case IPv6

                    setIpPortInfo(filename);
                    IPv6andPortInfo(ipPortInfo);
                    break;

                //retrive file from server ( get filename ):
                case ("RETR"):

                    /*150	File status okay; about to open data connection.*/
                    sendClient("150");
                    filename = tok.nextToken();//gets filename
                    getFile(filename);

                    break;

                //store file in server ( put filename):
                case ("STOR"):

                    /*150	File status okay; about to open data connection.*/
                    sendClient("150");

                    //gets filename, cuts command from the whole string:
                    filename = inputLine.substring(command.length()
                            + 1, inputLine.length());

                    putFile(filename);

                    break;

                /*default case: return what the user typed
                 (for non implemented commands) :*/
                default:
                    sendClient(inputLine);
                    break;

            }

        }//end while

        //close the client connection:
        closeClientConnection();
    }//end command

    /**
     *
     *
     *
     *
     *
     * getters and setters
     *
     *
     *
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }//end IPv6

    public void setcurrentDirectory(String s) {
        currentDirectory = s;

    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    /*getters and setters*/
    public String getIpPortInfo() {
        return ipPortInfo;
    }

    public void setIpPortInfo(String ipPortInfo) {
        this.ipPortInfo = ipPortInfo;
    }

    public PrintWriter getOut() {
        return out;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}//end class
