import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.lang.Thread;
/**
 * Created by Saimanoj & Jayachandra on 11/7/2016.
 */
public class Server {

    private class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            listenForClientMessages(this.socket);
        }
    }

    private ServerSocket serverSocket;
    private HashMap<String, Socket> socketMap;

    public Server(String port) throws IOException {
        this.serverSocket = new ServerSocket(Integer.parseInt(port));
        System.out.println("===========================================================================");
        System.out.println("Server successfully started and is running on port " + this.serverSocket.getLocalPort());
        System.out.println("---------------------------------------------");
        System.out.println("Welcome to Internet Chat Application | "+"[Server]"+":Ready");
        System.out.println("=============================================");
        this.socketMap = new HashMap<String, Socket>();
    }

    private void listenForNewClients() {
        try {
            Socket socket = null;
            while (true) {
                socket = this.serverSocket.accept();
                Handler t = new Handler(socket);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("[ERROR] Server shutdown!");
            }
            System.out.println("[ERROR] Server shutdown!");
        }
    }

    private void listenForClientMessages(Socket socket) {
        String clientName = null;
        DataInputStream reader = null;
        DataOutputStream writer = null;
        try {
            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
            clientName = reader.readUTF();
            if(socketMap.containsKey(clientName)){
                sendMessage(writer,"Server","message","Username='"+clientName+"' already taken",null,0);
                return;
            }
            socketMap.put(clientName, socket);
            System.out.println("Client '" + clientName + "' joined the chat!");
            System.out.println("------------------------------------       ");
            System.out.println("Live Clients:["+socketMap.size()+"] | Clients:"+socketMap.keySet().toString());
            System.out.println("===========================================");
            while (true) {
                String msgSendType = reader.readUTF();
                String msgType = reader.readUTF();
                String sendToUser = null;
                if (!msgSendType.equals("broadcast")) {
                    sendToUser = reader.readUTF();
                }
                String message = null, fileName = null;
                File tempFile = null;
                long fileSize = 0;
                if(msgType.contentEquals("message")) {
                    message = reader.readUTF();
                }
                else if(msgType.contentEquals("file")) {
                    fileName = reader.readUTF();
                    fileSize = reader.readLong();
                    tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
                    byte[] buffer = new byte[1024];
                    long temp = fileSize;
                    while(temp > 0) {
                        int size = reader.read(buffer);
                        bufferedOutputStream.write(buffer, 0, size);
                        temp = temp - size;
                    }
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                switch (msgSendType) {
                    case "broadcast":
                        for (String name : socketMap.keySet()) {
                            if (!name.contentEquals(clientName)) {
                                DataOutputStream dataOutputStream = new DataOutputStream(socketMap.get(name).getOutputStream());
                                sendMessage(dataOutputStream, clientName, msgType, message, tempFile, fileSize);
                            }
                        }
                        break;
                    case "unicast": {
                        DataOutputStream dataOutputStream = new DataOutputStream(socketMap.get(sendToUser).getOutputStream());
                        sendMessage(dataOutputStream, clientName, msgType, message, tempFile, fileSize);
                        break;
                    }
                    case "blockcast":
                        for (String name : socketMap.keySet()) {
                            if (!name.contentEquals(clientName) && !name.contentEquals(sendToUser)) {
                                DataOutputStream dataOutputStream = new DataOutputStream(socketMap.get(name).getOutputStream());
                                sendMessage(dataOutputStream, clientName, msgType, message, tempFile, fileSize);
                            }
                        }
                        break;
                }
                if(tempFile != null && tempFile.exists())
                    tempFile.delete();
            }
        } catch (IOException e) {
            if(clientName != null)
                socketMap.remove(clientName);
            try {
                if (reader != null)
                    reader.close();
                socket.close();
            } catch (IOException e1) {
                System.out.println("Connection to client " + clientName + " closed!");
            }
            System.out.println("Connection to client " + clientName + " closed!");
        }
    }

    private void sendMessage(DataOutputStream outputStream, String fromUserName, String msgType, String message, File tempFile, long fileSize) throws IOException {
        outputStream.writeUTF(fromUserName);
        outputStream.writeUTF(msgType);
        if(msgType.contentEquals("message")) {
            outputStream.writeUTF(message);
        }
        else if(msgType.contentEquals("file")) {
            outputStream.writeUTF(tempFile.getName());
            outputStream.writeLong(fileSize);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(tempFile));
            byte[] buffer = new byte[1024];
            while(fileSize > 0) {
                int size = bufferedInputStream.read(buffer);
                outputStream.write(buffer, 0, size);
                fileSize = fileSize - size;
            }
            bufferedInputStream.close();
        }
        outputStream.flush();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("[ERROR]  : Failed to start the server, Please start the server with a valid server port");
			System.err.println("[Format] : java Server <server_port>");
            System.err.println("[Example]: java Server 8000");
            System.exit(1);
        }
        Server server = new Server(args[0]);
        server.listenForNewClients();
    }
}
