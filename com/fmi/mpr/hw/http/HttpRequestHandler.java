package com.fmi.mpr.hw.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestHandler {
	
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
	 
	public HttpRequestHandler(Socket socket) throws IOException {
		this.socket = socket;
		reader = new BufferedInputStream(socket.getInputStream());
	}
	
	public void processRequest() throws IOException {
		getInfoFromRequestHeaders();
		
		if ("GET".equals(requestMethod) && "/".equals(this.path)) {
			sendHttpResponse("index.html", "200 OK", "");
		} else if ("GET".equals(requestMethod) && this.path.matches("\\/[\\w-_ !]+\\.\\w+")) {
			sendFile(this.path.substring(1));
		} else if ("GET".equals(requestMethod) && this.path.matches("\\/\\?fileName=[\\w-_ !]+\\.\\w+")) {
			sendFile(this.path.substring(11));
		} else if ("POST".equals(requestMethod)) {
			uploadFile();
		} else {
			sendHttpResponse("simple.html", "400 Bad Request", "Invalid Path: \"" + this.path + "\"");
		}
	}

	private void getInfoFromRequestHeaders() throws IOException {
		StringBuilder firstLine = new StringBuilder(64);
		reader.readLine().stream().forEach(b -> firstLine.append((char) b.byteValue()));
		
		String[] firstLineParts = firstLine.toString().split(" ");
		this.requestMethod = firstLineParts[0];
		this.path = firstLineParts[1];
		
		// skip all the other headers
		List<Byte> lineBytes;
		while (true) {			
			lineBytes = reader.readLine();
			if (lineBytes.size() == 2) {
				break;
			}
		}
	}
	
	private void sendFile(String fileName) throws IOException {
		File file = new File("files\\" + fileName);
		if (!file.exists()) {
			sendHttpResponse("simple.html", "404 Not Found", "File with name \"" + fileName + "\"" + " not found!");
			return;
		}
	
		try (PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.0 200 OK");
			ps.println("Content-Type: " + contTypeForExtention.get(fileName.split("\\.")[1]));
			ps.println();

			try (FileInputStream fis = new FileInputStream(file)) {
				int bytesRead = 0;
				byte[] buffer = new byte[8192];
				while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
					ps.write(buffer, 0, bytesRead);
				}
			}
		}
		System.out.println("File send!");
	}
	
	private void uploadFile() throws IOException {
		StringBuilder border = new StringBuilder(64);
		reader.readLine().stream().forEach(b -> border.append((char) b.byteValue()));
		border.insert(border.length() - 2, "--");

		StringBuilder lineWithFileName = new StringBuilder(64);
		reader.readLine().stream().forEach(b -> lineWithFileName.append((char) b.byteValue()));
		
		Matcher matcher = Pattern.compile("[\\w-_ !]+\\.\\w+").matcher(lineWithFileName.toString());
		String fileName = "new_file";
		if (matcher.find()) {
			fileName = matcher.group(0);
		}
		
		reader.readLine();
		reader.readLine();
		
		List<Byte> bytes = null;
		byte[] byteLine = null;
		
		try (FileOutputStream fos = new FileOutputStream(new File("files\\" + fileName))) {
			while (true) {
				bytes = reader.readLine();
				
				byteLine = new byte[bytes.size()];
				for (int i = 0; i < bytes.size(); i++) {
					byteLine[i] = bytes.get(i).byteValue();
				}
				
				if (new String(byteLine).equals(border.toString())) {
					break;
				} else {
					fos.write(byteLine, 0, byteLine.length);
				}
			}	
		}
		System.out.println("File recieved!");
		sendHttpResponse("simple.html", "200 OK", "File with name \"" + fileName + "\" uploaded!");
	}
	
	private void sendHttpResponse(String htmlFileName, String status, String message) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(htmlFileName));
				PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.0 " + status);
			ps.println();
			
			String line;
		    while ((line = br.readLine()) != null) {
		    	if (line.trim().equals(":message")) {
		    		ps.println(message);
		    	} else {
		    		ps.println(line);
		    	}
		    }
		}
	}

}
