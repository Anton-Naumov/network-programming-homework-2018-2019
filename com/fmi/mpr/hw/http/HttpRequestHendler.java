package com.fmi.mpr.hw.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestHendler {
	
	private Socket socket;
	private String requestMethod;
	private String path;
	private BufferedInputStream reader;
	
	private Map<String, String> contTypeForExtention = new HashMap<String, String>()
	{{
	     put("jpg", "image/jpg");
	     put("png", "image/png");
	     put("gif", "image/gif");
	     put("mp4", "video/mp4");
	     put("css", "text/css");
	     put("html", "text/html");
	     put("csv", "text/csv");
	     put("xml", "text/xml");
	     put("html", "text/html");
	     put("txt", "text/plain");
	}};
	 
	public HttpRequestHendler(Socket socket) throws IOException {
		this.socket = socket;
		reader = new BufferedInputStream(socket.getInputStream());
	}
	
	public void processRequest() throws IOException {
		processRequestHeaders();
		
		if ("GET".equals(requestMethod) && "/".equals(this.path)) {
			sendWelcomeForm();
		} else if ("GET".equals(requestMethod) && this.path.matches("\\/\\w+\\.\\w+")) {
			sendFile(this.path.substring(1));
		} else if ("GET".equals(requestMethod) && this.path.matches("\\/\\?fileName=\\w+\\.\\w+")) {
			sendFile(this.path.substring(11));
		} else if ("POST".equals(requestMethod)) {
			uploadFile();
		} else {
			sendError("400 Bad Request", "Invalid Path: \"" + this.path + "\"");
		}
	}

	private void processRequestHeaders() throws IOException {
		StringBuilder firstLine = new StringBuilder(64);
		reader.readLine().stream().forEach(b -> firstLine.append((char) b.byteValue()));
		
		String[] firstLineParts = firstLine.toString().split(" ");
		this.requestMethod = firstLineParts[0];
		this.path = firstLineParts[1];
		
		// skip all the other headers
		List<Byte> lineBytes;
		while (true) {			
			lineBytes = reader.readLine();
			lineBytes.stream().forEach(b -> System.out.print((char) b.byteValue()));
			if (lineBytes.size() == 2) {
				break;
			}
		}
	}
	
	private void sendWelcomeForm() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("index.html"));
				PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.0 200 OK");
			ps.println();
			
			String line;
		    while ((line = br.readLine()) != null) {
		       ps.println(line);
		    }
		}
	}
	
	private void sendError(String typeOfError, String errorMessage) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("error.html"));
				PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.1 " + typeOfError);
			ps.println();
			
			String line;
		    while ((line = br.readLine()) != null) {
		    	if (line.trim().equals(":errorMassage")) {
		    		ps.println(errorMessage);
		    	} else {
		    		ps.println(line);
		    	}
		    }
		}
	}
	
	private void sendFile(String fileName) throws IOException {
		try (PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.0 200 OK");
			ps.println("Content-Type: " + contTypeForExtention.get(fileName.split("\\.")[1]));
			ps.println();
			
			File file = new File(fileName);
			if (!file.exists()) {
				sendError("404 Not Found", "File with name \"" + fileName + "\"" + " not found!");
			}

			try (FileInputStream fis = new FileInputStream(file)) {
				
				int bytesRead = 0;
				byte[] buffer = new byte[8192];
				
				while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
					ps.write(buffer, 0, bytesRead);
				}
			}
		}
		System.out.println("Image send!");
	}
	
	private void uploadFile() throws IOException {
		StringBuilder boarder = new StringBuilder(64);
		reader.readLine().stream().forEach(b -> boarder.append((char) b.byteValue()));
		boarder.insert(boarder.length() - 2, "--");
		List<Byte> bytes = null;
		bytes = reader.readLine();
		bytes.stream().forEach(b -> System.out.print((char) b.byteValue()));
		bytes = reader.readLine();
		bytes.stream().forEach(b -> System.out.print((char) b.byteValue()));
		bytes = reader.readLine();
		bytes.stream().forEach(b -> System.out.print((char) b.byteValue()));
		
		byte[] byteLine = null;
		
		try (FileOutputStream fos = new FileOutputStream(new File("new_panda.jpg"))) {
			while (true) {
				bytes = reader.readLine();
				
				byteLine = new byte[bytes.size()];
				for (int i = 0; i < bytes.size(); i++) {
					byteLine[i] = bytes.get(i).byteValue();
				}
				System.out.println(new String(byteLine));
				
				if (new String(byteLine).equals(boarder.toString())) {
					break;
				} else {
					fos.write(byteLine, 0, byteLine.length);
				}
			}	
		}
		System.out.println("Image recieved!");
	}

}
