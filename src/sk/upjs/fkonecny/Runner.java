package sk.upjs.fkonecny;

import java.util.Scanner;

public class Runner {

	public static void main(String[] args) {

		String address = "0.0.0.0";
		int port = 5000;
		String pathFrom = "I:/workspace/Kopr/test/from/me.exe";
		String pathTo = "I:/workspace/Kopr/test/to/me.exe";
		int threadCount =5;
		Download download = new Download(address,port,pathFrom,pathTo,threadCount);
		download.main(null);
	}		
}
