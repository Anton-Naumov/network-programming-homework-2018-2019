package com.fmi.mpr.hw.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
	
	private static final int SERVER_PORT = 8080;
	
	private ServerSocket serverSocket;
	private boolean isRunning;
	
	public HttpServer() throws IOException {
		serverSocket = new ServerSocket(SERVER_PORT);
	}
	
	public void start() {
		if (!isRunning) {
			isRunning = true;
			run();
		}
	}

	public void stopServer() throws IOException {
		isRunning = false;
		serverSocket.close();
	}
	
	private void run() {
		while (isRunning) {
			try (Socket newConnection = serverSocket.accept()) {
				
				System.out.printf("New connection from %s. Processing...%n", newConnection.getInetAddress());
				
				HttpRequestHendler client = new HttpRequestHendler(newConnection);
				client.processRequest();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
