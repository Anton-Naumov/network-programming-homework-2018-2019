package com.fmi.mpr.hw.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BufferedInputStream {

	private static final int BUFFER_SIZE = 1024;
	
	private InputStream inputStream;
	private byte[] buffer;
	private int bufferPos = -1;
	private int currBufferSize = 0;
	
	public BufferedInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		this.buffer = new byte[BUFFER_SIZE];
	}
	
	public List<Byte> readLine() throws IOException {
		List<Byte> line = new ArrayList<>();
		boolean checkForNewLine = false;
		while (true) {
			line.add(getNextByte());
			
			if (checkForNewLine) {
				if (line.get(line.size() - 1) == (byte) '\n') {
					return line;
				} else {
					checkForNewLine = false;
				}
			}
			if (line.get(line.size() - 1) == (byte) '\r') {
				checkForNewLine = true;
			}
		}
	}
	
	private byte getNextByte() throws IOException {
		if (bufferPos == -1 || bufferPos == currBufferSize) {
			currBufferSize = inputStream.read(buffer, 0, BUFFER_SIZE);
			bufferPos = 0;
			if (currBufferSize == -1) {
				throw new IOException("The buffer is empty!");
			}
		}
	
		return buffer[bufferPos++];
	}

}
