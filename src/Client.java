import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;


public class Client {

	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;

	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
			
		}
		
	}
	
	public void sendMessage() {
		try {

			bufferedWriter.write(username);
			// this is first because its the first thing the user is asked, so we need to return it back to them
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			Scanner scanner = new Scanner(System.in);
			while (socket.isConnected()) {
				String messageToSend = scanner.nextLine();

			//	if (Objects.equals(messageToSend, "!priv")) {
			//		System.out.println(messageToSend);
			//		privMessage();
			//	} else {

					bufferedWriter.write(messageToSend);
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}


	public void listenForMessage() {
		// done on a new thread as listening for messages is a blocking operation
		new Thread(new Runnable() {
			public void run() {
				String msgFromGroupChat;
				
				while (socket.isConnected()) {
					try {
						msgFromGroupChat = bufferedReader.readLine();
						System.out.println(msgFromGroupChat);
					} catch (IOException e) {
						closeEverything(socket, bufferedReader, bufferedWriter);
					}
				}
			}
		}).start();
		// calling start on the thread
		
	}
	
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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

	
	public static void main(String[] args) throws IOException {

		Integer randomNum = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your name for the group chat: ");
		String username = scanner.nextLine() + "[" + randomNum + "]";

		Scanner scanner1 = new Scanner(System.in);
		System.out.println("Enter the address of the Server you want to connect to: ");
		String address = scanner1.nextLine();

		Scanner scanner2 = new Scanner(System.in);
		System.out.println("Enter the port number you want to connect through: ");
		String portNumber = scanner2.nextLine();

		Socket socket = new Socket(address, Integer.parseInt(portNumber));
		Client client = new Client(socket, username);
		client.listenForMessage();
		client.sendMessage();
		//these are both blocking methods because they use while loops, however because they are separate threads they can run simultaneously 
	}
	
}
