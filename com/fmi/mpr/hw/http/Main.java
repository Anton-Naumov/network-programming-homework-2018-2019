package com.fmi.mpr.hw.http;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		try {
			HttpServer server = new HttpServer();
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}