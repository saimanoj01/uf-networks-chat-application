# uf-networks-chat-application
CNT5106C - Computer Networks - Internet Chat Application
*********************************************************


Introduction :
------------

This is an internet chat application with Server and a Client program. The server manages the Client group allowing any number of clients to join the group with a username at any time. The chat system supports the following functionalities.
(1) Broadcast: Any client is able to send a text to the server, which will relay it to all other clients for display.
(2) Broadcast: Any client is able to send a file of any type to the group via the server.
(3) Unicast: Any client is able to send a private message to a specific other client via the server.
(4) Unicast: Any client is able to send a private file of any type to a specific other client via the server.
(5) Blockcast: Any client is able to send a text to all other clients except for one via the sever.



Project Contributers :
--------------------

(1) UFID : 5963-9690, SAI MANOJ BANDI	
(2) UFID : 0124-9405, JAYACHANDRA YARLAGADDA 



How to run? :
-----------
Prerequisites: Server.class, Client.class Files

(1) How to start the Server?
	java Server <server_port>
	Example : java Server 8000

(2) How to start the Client?
	java Client <client_name> <server_port>
	Example : java Client testClient 8000

(3) How to send a message?
        broadcast message "<message>"
        unicast   message "<message>" <username>
        blockcast message "<message>" <username>		
	Example : unicast message "Hello World!" testClient

(4) How to send a file?
        broadcast file "<filepath>"
        unicast   file "<filepath>" <username>
        blockcast file "<filepath>" <username>
	Example : blockcast file "D:\\temp\\data.txt" testClient
Once the file is received on the client side, a new directory will be created in the name of the client and the file is saved in that directory.


What the application provides?:
--------------------------------
Server: Command line interface - Sai Manoj B
1. Makes sure of the communication between clients. File and Message flows.
2. Shows info of no of live clients and their names. Info of clients getting connected and disconnected.
3. Makes sure of the unique identity of clients using usernames.

Client: Command line interface - Jaya chandra Y
1. Send and Receive from messages/file other clients by connecting to server
2. Error checking for Chat Format. 
3. Local and Global File sending.
