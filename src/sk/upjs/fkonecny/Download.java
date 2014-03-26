package sk.upjs.fkonecny;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JFileChooser;

class Download {
	private static String address;
	private static int port;
	private static String pathFrom;
	private static String pathTo;
	private static int threadCount;
	private static List<Part> parts;
	private static AtomicInteger runningThreads = new AtomicInteger();
	private static boolean stop = false;
	private static long fileLength;
	private static AtomicLong downloaded = new AtomicLong();
	private static long[] offsets = new long[0];
	private static File inf;
	private static boolean paused = false;
	
	public Download(String address, int port, String pathFrom, String pathTo,
			int threadCount) {
		this.address = address;
		this.port = port;
		this.pathFrom = pathFrom;
		this.pathTo = pathTo;
		this.threadCount = threadCount;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		address = "127.0.0.1";
		port = 5000;
		pathFrom = "I:/workspace/Kopr/test/from/me.exe";
		//pathFrom = scanner.nextLine();
		//"/media/filip/New Volume/workspace/Kopr/test/from/java.exe";
		//pathFrom = "I:/workspace/Kopr/test/from/me.exe";
		pathTo = "I:/workspace/Kopr/test/to/me.exe";
		//pathTo = scanner.nextLine();
		threadCount =5;
		//threadCount = scanner.nextInt();
		
		ExecutorService executor = Executors.newCachedThreadPool();
		List<DownloadThread> downloadThreads = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			DownloadThread downloadThread = new DownloadThread(address, port);
			downloadThreads.add(downloadThread);
		}
		try {
			fileLength = downloadThreads.get(0).getLength(pathFrom);
			inf = new File(pathTo + ".txt");
			if (inf.exists()) {
				parts = getParts(inf);
				inf.delete();
			} else {
				parts = getParts(fileLength);
			}
			for (int i = 0; i < threadCount; i++) {
				downloadThreads.get(i).setPart(parts.get(i));
				executor.execute(downloadThreads.get(i));
				runningThreads.incrementAndGet();
			}

			waitForIt();

			System.out.println("Download finished");

			executor.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private static void waitForIt() throws InterruptedException, IOException {
		Scanner scanner = new Scanner(System.in);
		String req = null;
		while (runningThreads.get() != 0) {
			if (scanner.hasNextLine()) {
				req = scanner.nextLine();
				if (req.equals("e"))
					stop();
				
				if (req.equals("p"))
					pause();
			}
			
			if (stop) {
				while(offsets.length !=5)
					Thread.sleep(100);
				Arrays.sort(offsets);
				RandomAccessFile raf = new RandomAccessFile(inf, "rw");
				for (int i = 0; i < offsets.length; i++) {
					raf.writeBytes(offsets[i] + " ");
				}
				raf.close();
			}
			
			Thread.sleep(100);
		}

	
	}

	private static void pause() {
	
		paused = paused ? false : true;
		
		if (paused) {
			System.out.println("paused");
		} else {
			System.out.println("restored");
		}
	}

	private static void stop() {
		stop = true;
	}

	private static List<Part> getParts(File inf) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(inf, "r");

		List<Part> parts = new ArrayList<>();
		long partSize = fileLength / threadCount;
		String line = raf.readLine();
		raf.close();

		String[] offsets = line.split(" ");
		long offset = 0;
		for (int i = 0; i < threadCount; i++) {
			offset = Long.parseLong(offsets[i]);
			long end =  i * partSize - offset;
			Part part = new Part(pathFrom, pathTo, offset, (i+1) * partSize - offset);
			parts.add(part);
		}
		long lastPartSize = fileLength - offset;
		parts.get(threadCount - 1).setPartSize(lastPartSize);
		
		for (int i = 0; i < parts.size(); i++) {
			System.out.println(parts.get(i));
		}

		return parts;
	}

	private static List<Part> getParts(long fileLength) {
		List<Part> parts = new ArrayList<>();
		long partSize = fileLength / threadCount;
		for (int i = 0; i < threadCount; i++) {
			Part part = new Part(pathFrom, pathTo, partSize * i, partSize);
			parts.add(part);
		}
		long lastPartSize = fileLength - partSize * (threadCount - 1);
		parts.get(threadCount - 1).setPartSize(lastPartSize);
		
		for (int i = 0; i < parts.size(); i++) {
			System.out.println(parts.get(i));
		}

		return parts;
	}

	public static void removeConnection() {
		if (runningThreads.decrementAndGet() == 0)
			System.out.println("press enter");

	}

	public static boolean stopped() {
		return stop;
	}
	
	public static boolean paused() {
		return paused;
	}

	public static synchronized void saveOffset(long filePointer) {
		long[] temp = Arrays.copyOf(offsets, offsets.length + 1);
		temp[offsets.length] = filePointer;
		offsets = temp;
	}

	public static void addDownloaded(long count) {
		downloaded.addAndGet(count);
	}

}