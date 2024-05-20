import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {


	private ServerSocket serverSocket;
	// listens to incoming connections from clients


	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}


	public void startServer() {
		// responsible for keeping the server running
		try {

			while (!serverSocket.isClosed()) {
				
				Socket socket = serverSocket.accept();
				// this accept method is a blocking method meaning the program is stopped here until a client connects, when the client
				// connects a socket object gets returned which communicates with the client
				System.out.println("A new client has connected");
				ClientHandler clientHandler = new ClientHandler(socket);
				// each object of this class is responsible for handling a client
				// this class implements runnable, which means each instance will be executed by a seperate thread allowing multiple clients 
				// to connect
				
				Thread thread = new Thread(clientHandler);
				// we pass the clientHandler class which implements runnable so each object created will be running in a new thread
				thread.start();

			}

		} catch (IOException e) {
			
		}
	}
	
	public void closeServerSocket() {
		// this method is used so as that if an error occurs we shut down the server socket.
		// the try checks if its null
		
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {

		InetAddress addr = InetAddress.getByName("127.0.0.1");
		ServerSocket serverSocket = new ServerSocket(1234, 50, addr);
		Server server = new Server(serverSocket);
		server.startServer();

	}
	
}
