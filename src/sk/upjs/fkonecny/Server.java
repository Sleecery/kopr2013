package sk.upjs.fkonecny;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private static Socket socket;
	private static ServerSocket serverSocket;
	private static ExecutorService executorService;

	public static void main(String[] args) throws IOException {
		executorService = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(5000);
		System.out.println("Server socket ready on : "
				+ serverSocket.getLocalSocketAddress());
		
		while (true) {
			acceptConnection();
		}
	}

	private static void acceptConnection() {
		try {
			socket = serverSocket.accept();
			System.out.println("New connection from " + socket.getPort()
					+ " detected");
			Connection connection = new Connection(socket);
			executorService.execute(connection);
		} catch (IOException e) {
			//TODO
		}
	}
}
