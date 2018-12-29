package com.fmi.mpr.hw.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class HttpClient {
	
	private Socket socket;
	private StringBuilder request;
	private String requestMethod;
	private String path;
	private BufferedReader reader;
	
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
	 
	public HttpClient(Socket socket) throws IOException {
		this.socket = socket;
		request = new StringBuilder();
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void processRequest() throws IOException {
		getRequest();
		
		if ("/".equals(this.path) && "GET".equals(requestMethod)) {
			sendForm();
		} else if ("GET".equals(requestMethod) && !this.path.contains("?")) {
			sendFile(this.path.substring(1));
		} else if ("GET".equals(requestMethod)) {
			sendFile(this.path.substring(11));
		} else if ("POST".equals(requestMethod)) {
			uploadFile();
		}
	}

	private void getRequest() throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null) {
			request.append(line + "\n");
			if (line.trim().isEmpty()) {
				break;
			}
		}

		String[] requestLines = request.toString().split("\n");
		String[] firstLine = requestLines[0].split(" ");
		this.requestMethod = firstLine[0];
		this.path = firstLine[1];
	}
	
	private void sendForm() throws IOException {
		try (PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.0 200 OK");
			ps.println();
			ps.println("<!DOCTYPE html>\n" +
						"<html>\n" +
						"<head>\n" +
					    "<title>MyApp</title>\n" +
						"</head>" +
						"<body>" +
						"    <form>" +
						"	 File name: <input type=\"text\" name=\"fileName\">" +
						"        <br>" +
						"        <input type=\"submit\" value=\"View File\">" +
						"    </form>" +
						"    <br>" +
						"    <br>" +
						"    <form method=\"post\" enctype=\"multipart/form-data\">" +
						"        <input type=\"file\" name=\"fileContent\" accept=\"image/png, image/jpg\">" +
						"        <br>" +
						"        <input type=\"submit\" value=\"Upload File\">" +
						"    </form>" +
						"</body>" +
						"</html>");
		}
	}
	
	private void sendFile(String fileName) throws IOException {
		try (PrintStream ps = new PrintStream(socket.getOutputStream(), true)) {
			ps.println("HTTP/1.0 200 OK");
			ps.println("Content-Type: " + contTypeForExtention.get(fileName.split("\\.")[1]));
			ps.println();
			
			try (FileInputStream fis = new FileInputStream(new File(fileName))) {
				
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
		String boarder = reader.readLine();
		reader.readLine();
		reader.readLine();
		reader.readLine();
		
		try (FileOutputStream fos = new FileOutputStream(new File("new_panda.jpg"))) {
			
			int newChar;
			StringBuilder line = new StringBuilder(boarder.length());
			while ((newChar = reader.read()) != -1) {
				line.append((char) newChar);
				if (line.length() == boarder.length() || newChar == '\n') {
					if (line.toString().equals(boarder)) {
						break;
					}
					fos.write((line.toString()).getBytes());
					line.delete(0, line.length());
				}
				
			}
		}
		System.out.println("Image recieved!");
	}

}
