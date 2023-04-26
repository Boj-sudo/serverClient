package serverClient;
import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
	
	private ServerSocket server;
	private boolean isReady;

	public Server(int port) {
		try {
			server = new ServerSocket(port);
			System.out.println("Server is created on port " + port);
			isReady = true;
			
			while(isReady) {
				System.out.println("Waiting to accept client request");
				Socket client_socket = server.accept();
				Thread thread = new Thread(new Handler(client_socket));
				thread.start();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server(2030);
	}
}
