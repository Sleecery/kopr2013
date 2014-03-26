package sk.upjs.fkonecny;

import java.util.Scanner;

public class Part {

	private String pathFrom;
	private String pathTo;
	private long offset;
	private long partSize;

	public Part(String pathFrom, String pathTo, long offset, long partSize) {
		this.pathFrom = pathFrom;
		this.pathTo = pathTo;
		this.offset = offset;
		this.partSize = partSize;
	}

	@Override
	public String toString() {
		return pathFrom + " " + pathTo + " " + offset + " " + partSize;
	}

	public static Part getPart(String s) {
		Scanner scanner = new Scanner(s);
		return new Part(scanner.next(), scanner.next(), Long.parseLong(scanner
				.next().trim()), Long.parseLong(scanner.next().trim()));
	}

	public String getPathFrom() {
		return pathFrom;
	}

	public void setPathFrom(String pathFrom) {
		this.pathFrom = pathFrom;
	}

	public String getPathTo() {
		return pathTo;
	}

	public void setPathTo(String pathTo) {
		this.pathTo = pathTo;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getPartSize() {
		return partSize;
	}

	public void setPartSize(long partSize) {
		this.partSize = partSize;
	}

}
