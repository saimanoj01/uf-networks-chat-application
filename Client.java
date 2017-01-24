import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Saimanoj & Jayachandra on 11/7/2016.
 */
public class Client {
    // Nested class which runs separate thread to receive messages
    private class GetMessages extends Thread {
        public void run() {
            getMessages();
        }
    }
    private String clientName;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    // Client constructor which takes in the name of the client and the server port.
    public Client(String clientName, String serverPort) throws IOException{
            this.clientName = clientName;
            this.socket = new Socket("localhost", Integer.parseInt(serverPort));
            System.out.println("===========================================================================");
            System.out.println("Client:'"+clientName+"' successfully connected to the Server running on port " + serverPort);
            inputStream = new DataInputStream(this.socket.getInputStream());
            outputStream = new DataOutputStream(this.socket.getOutputStream());
            // Send the client name once the connection is established.
            outputStream.writeUTF(clientName);
			System.out.println("---------------------------------------------");
            System.out.println("Welcome to Internet Chat Application | ["+clientName+"]:Ready");
			System.out.println("---------------------------------------------");
			System.out.println("[Chat Format]:\n\tbroadcast message/file  \"<message/filename>\"\n\tunicast   message/file  \"<message/filename>\" <client_name>\n\tblockcast message/file  \"<message/filename>\" <client_name>");
            System.out.println("===========================================================================");
            outputStream.flush();
    }

    /*
     Method to send messages to other clients.
     Format of the message.
     Example: unicast message "Hello World!" testUser1
     */
    private void sendMessages() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String command = reader.readLine();
                String[] tokens = command.toLowerCase().split(" ");
                if (tokens.length < 3) {
                    System.err.println("[ERROR]: Invalid Command \'"+ command +"\'\n[Format]:\n\tbroadcast message/file  \"<message/filename>\"\n\tunicast   message/file  \"<message/filename>\" <client_name>\n\tblockcast message/file  \"<message/filename>\" <client_name>");
                    continue;
                }
                String msgSendType = tokens[0];
                String msgType = tokens[1];
                String message = null;
                Pattern p = Pattern.compile(".*[\\\"\\\'](.*)[\\\"\\\'].*");
                Matcher m = p.matcher(command);
                if (m.find()) {
                    message = m.group(1);
                    if (message.length() == 0) {
                        System.out.println("[ERROR]: Message/Filename is empty");
                        continue;
                    }
                } else {
                    System.out.println("[ERROR]: Please include the message/filename in quotes");
                    continue;
                }
                String sendToUser = null;
                if (!msgSendType.contentEquals("broadcast")) {
                    sendToUser = tokens[tokens.length - 1];
                }

                if(msgType.equals("message")) {
                    outputStream.writeUTF(msgSendType);
                    outputStream.writeUTF(msgType);
                    if (sendToUser != null) {
                        outputStream.writeUTF(sendToUser);
                    }
                    outputStream.writeUTF(message);
                    outputStream.flush();
                    System.out.println("[SUCCESS] Message sent successfully!");
                }
                else if(msgType.equals("file")) {
					String localpath = System.getProperty("user.dir");
					File file = new File(message);
					if(!file.isAbsolute()) {
					    file = new File(localpath + File.separator + message);
                    }
                    if(!file.exists() || !file.isFile()) {
                        System.out.println("[ERROR] Path: \'" + message + "\' not found!!" +" Please enter a valid file path.");
                        continue;
                    }
                    outputStream.writeUTF(msgSendType);
                    outputStream.writeUTF(msgType);
                    if (sendToUser != null) {
                        outputStream.writeUTF(sendToUser);
                    }
                    outputStream.writeUTF(file.getName());
                    outputStream.writeLong(file.length());
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
                    byte[] buffer = new byte[1024];
                    int size;
                    while((size = bufferedInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, size);
                    }
                    outputStream.flush();
                    bufferedInputStream.close();
                    System.out.println("[SUCCESS] File:\'"+file.getName()+"\' sent successfully!");
                }
            } catch (IOException e) {
                if(socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(1);
            }
        }
    }

    /*
     Method to receive data from other clients.
     Format of the message.
     Example: fromUserName message \n Hello World
     */
    private void getMessages() {
        while(true) {
            try {
                String fromUserName = inputStream.readUTF();
                String msgType = inputStream.readUTF();
                if(msgType.contentEquals("message")) {
					if(fromUserName.contentEquals("Server")){
                        System.out.println("[Server]: "+ inputStream.readUTF()+", try another one");
                        System.exit(1);
					}
                    String message = inputStream.readUTF();
                    System.out.println("["+ fromUserName + "]" + ": " + message);
                }
                else if(msgType.contentEquals("file")) {
                    String fileName = inputStream.readUTF();
                    long fileSize = inputStream.readLong();
                    String directoryName = System.getProperty("user.dir") + File.separator + clientName;
                    File dir = new File(directoryName);
                    if(!dir.exists()) {
                        dir.mkdir();
                    }
                    File file = new File(dir.getAbsolutePath(), fileName);
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath()));
                    byte[] buffer = new byte[1024];
                    while(fileSize > 0) {
                        int size = inputStream.read(buffer);
                        outputStream.write(buffer, 0, size);
                        fileSize = fileSize - size;

                    }
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("["+ fromUserName + "]" + ": "+fileName+"; \nFile received and saved to \'" + file.getAbsolutePath()+"\'");
                }
            } catch (IOException e) {
 				System.out.println("[ERROR] Problem with server connection. Shutting down!");
                if(socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) throws IOException{
        if (args.length != 2) {
            System.err.println("[ERROR]  : Failed to start the client, client command not invoked properly");
            System.err.println("[Format] : java Client <client_name> <server_port>");
            System.err.println("[Example]: java Client testClient 8000");
            System.exit(1);
        }
        Client client = new Client(args[0], args[1]);
        GetMessages getMessages = client.new GetMessages();
        Thread t = new Thread(getMessages);
        t.start();
        client.sendMessages();
    }
}
