package sk.upjs.fkonecny;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class Connection implements Runnable {

	private Socket socket;
	private BufferedReader bufferedReader;
	private DataOutputStream dataOutputStream;
	private RandomAccessFile raf;
	private boolean running = true;
	
	public Connection(Socket socket) {
		this.socket = socket;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (running) {
				String request = bufferedReader.readLine();
				switch (request.split(" ")[0]) {
				case "start":
					Part part = getPart();
					send(part);
					break;
				case "length":
					sendLength(request.split(" ")[1]);
					break;
				}
			}
		} catch (IOException e) {
			// System.out.println("Connection closed on port " +
			// socket.getPort());
		} finally {
			try {
				System.out.println("connection closed");
				socket.close();
				bufferedReader.close();
				dataOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void sendLength(String string) {
		File file = new File(string);
		try {
			dataOutputStream.writeBytes(file.length() + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void send(Part part) {
		File file = new File(part.getPathFrom());
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(part.getOffset());
			byte[] b = new byte[1024];
			int count = 0;
			int size = 0;
			while ((count = raf.read(b, 0, 1024)) != -1) {
				dataOutputStream.write(b, 0, count);
				size = size + count;
				if (size > part.getPartSize()) {
					return;
				}
			}
		} catch (SocketException e) {
			// TODO
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				raf.close();
				running = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Part getPart() {
		try {
			String s = bufferedReader.readLine();
			return Part.getPart(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
