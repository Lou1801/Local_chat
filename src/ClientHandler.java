import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{


	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	// keeps track of clients, when a message is sent it iterates through the arraylist to send the messsage to each client
	private Socket socket;
	// establishes connection between the client and server
	private BufferedReader bufferedReader;
	// reads data sent from the client
	private BufferedWriter bufferedWriter;
	// sends data to our client, so the messages are sent from other clients
	private String clientUsername;
	// username(s) for clients


	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			// this is the object being made for this class, so set the socket of it equal to what is passed in the constructor of startServer.
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// character stream because it ends with 'Writer' and we need this because we are sending messages. The default 'getOutputStream'
			// is for bytes.
			// output stream is used to send data
			// input stream is used to read data
			// the buffer makes our communication more efficient
			this.clientUsername = bufferedReader.readLine();
			// This is the first thing sent from the client, it reads until the newline character which is when the user will press enter.
			clientHandlers.add(this);

			// adds the client to the arraylist, so they're part of the groupchat
			broadcastMessage("Server: " + clientUsername + " has entered the chat");
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String messageFromClient;
		// this method (thread) waits for messages, if we didn't have multiple threads running in this program it would be halted here, just waiting.

		while (socket.isConnected()) {
			try {
				messageFromClient = bufferedReader.readLine();
				if(messageFromClient == null) throw new IOException();
				if (messageFromClient.equals("!priv")) {
					privMessage(this);
				} else if(messageFromClient.startsWith("!priv")) {
					String strippedMessage = messageFromClient.substring(6);
					// cutting away 6 characters "!priv " (!priv + one space)
					int lastIndexOfUsername = strippedMessage.indexOf(" ");
					// Because !priv <username> <message>   <--- the first space is going to be cut away by the substr in the line above,
					// next one is after username
					String username = strippedMessage.substring(0,lastIndexOfUsername);
					String message = strippedMessage.substring(lastIndexOfUsername+1);
					privMessageUser(this,username,message);
				} else
					broadcastMessage(this.clientUsername + ": " + messageFromClient);
				// we put this into the different thread because we don't want the program to halt here


			}	catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
				break;
				//exit to stop running the 'closeEverything' method
			}
			
		}
		
	}

	private void privMessageUser(ClientHandler clhd, String username, String message) {
		for (ClientHandler clientHandler : clientHandlers) {
			if(clientHandler.clientUsername.equals(username)) {
				try {
					clientHandler.bufferedWriter.write(clhd.clientUsername + "(private): "+message);
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
					clhd.bufferedWriter.write(clhd.clientUsername +"(private "+ username +"): "+message);
					clhd.bufferedWriter.newLine();
					clhd.bufferedWriter.flush();
				} catch (IOException e) {
					closeEverything(socket, bufferedReader, bufferedWriter);
				}
			}
		}
	}

	private void privMessage(ClientHandler clhd){
		String messageToSend = "List of connected Clients: ";
		for (ClientHandler clientHandler : clientHandlers) {
			messageToSend += clientHandler.clientUsername + " ";
		}
		try {
			clhd.bufferedWriter.write(messageToSend);
			clhd.bufferedWriter.newLine();
			clhd.bufferedWriter.flush();

		}catch (IOException e){
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	public void broadcastMessage(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			//             ^^^^^^^^^^^^^
			// for each clientHandler in the arraylist this object represents the clientHandler for each iteration
			try {
				//if (!clientHandler.clientUsername.equals(clientUsername)) {
					// broadcast the message to all users apart from the one who sent it

						clientHandler.bufferedWriter.write(messageToSend);
						clientHandler.bufferedWriter.newLine();
						clientHandler.bufferedWriter.flush();
						// buffer is not sent unless it is full so this ensures its manually flushed
				
			} catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
				
			}
		}
	}



	public void removeClientHandler() {
		clientHandlers.remove(this);
		//removes current clientHandler from the arraylist, we don't want to keep sending once they have left
		broadcastMessage("Server: " + clientUsername + " has left the chat.");
		//this method notifies when a user has left the chat
	}
	
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		removeClientHandler();
		// if the user closes they are also disconnecting so call other method
		// the output and input streams reader and writer don't need to be closed because they are wrapped around bufferedReader and bufferedWriter
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
				// makes sure its not equal to null so as that when we call the close method we don't get a null pointer exception
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
				
			}
			if (socket != null) {
				socket.close();
				// closes input and output stream as well
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
