package sk.upjs.fkonecny;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;

public class DownloadThread implements Runnable {

	private Socket socket = null;
	private Part part;
	private long received;
	private BufferedReader inFromServer;
	private DataOutputStream outToServer;
	private File file;
	private boolean run = true;
	private RandomAccessFile raf;

	public DownloadThread(String address, int port) {
		try {
			socket = new Socket(address, port);
			inFromServer = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			outToServer = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		file = new File(part.getPathTo());
		try {
			outToServer.writeBytes("start\n");
			outToServer.writeBytes(part.toString() + "\n");
			byte[] b = new byte[1024];
			raf = new RandomAccessFile(file, "rw");
			InputStream inputStream = socket.getInputStream();
			raf.seek(part.getOffset());
			int count = 0;
			System.out.println("Thread started");
			while (count != -1) {
				while (Download.paused())
					Thread.sleep(100);
				count = inputStream.read(b);
				Download.addDownloaded(count);

				if (count == 0) {
					Thread.sleep(100);
				} else if (count == -1) {
					return;
				} else {
					raf.write(b, 0, count);
					if (Download.stopped()) {
						Download.saveOffset(raf.getFilePointer());
						System.out.println(part);
						return;
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Connection closed on port " + socket.getPort());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				raf.close();
				outToServer.close();
				inFromServer.close();
				socket.close();
				Download.removeConnection();
				try {
					this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public long getLength(String pathFrom) throws IOException {
		outToServer.writeBytes("length " + pathFrom + "\n");
		String length = inFromServer.readLine();
		return Long.parseLong(length);
	}
}
