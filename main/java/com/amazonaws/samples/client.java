package com.amazonaws.samples;

import java.io.*;
import java.net.Socket;

public class client implements Runnable {
	final BufferedReader in; 
	final PrintWriter out;

	Socket s;

	// constructor 
	public client(Socket s, BufferedReader in, PrintWriter out) { 
		this.in = in; 
		this.out = out; 
		this.s = s; 
	}

	public void run() { 
		String received = "";
		while (true)  { 
			try {

				try {  
					received = this.in.readLine();
					System.out.println("received : " + received);

				} catch (IOException e) {
					e.printStackTrace();
				}

				if(received == null) {
					break;
				}

				//Registering a new user
				if(received.startsWith("#register")){
					int i = received.indexOf("@");
					String id = received.substring(9, i);
					String password = received.substring(i + 1);
					server.addUser(id, password);
					sendMessageToMobile("##");
				} 

				//Logging in as registered user
				else if(received.startsWith("#login")) {
					int i = received.indexOf("@");
					String id = received.substring(6, i);
					String password = received.substring(i + 1);
					server.getPlayers();
					if (server.userValidation(id, password)) {
						sendMessageToMobile(server.players);
					} else {
						sendMessageToMobile("1");
					}
				}

				//Adding players to database
				else if(received.startsWith("#add")) {
					String player = received.substring(4);
					server.addPlayer(player, "No");
				}

				//Removing players from database
				else if(received.startsWith("#remove")) {
					int i = received.indexOf(" ");
					String player = received.substring(7, i);
					server.removePlayer(player);
				}

				//Changing "paid" status of player
				else if(received.startsWith("#paid")) {
					int i = received.indexOf("@");
					String player = received.substring(5, i);
					String paid = received.substring(i + 1);
					server.removePlayer(player);
					server.addPlayer(player, paid);
				}

			}catch (Exception e) {          
				e.printStackTrace(); 
			} 
		}
		try
		{ 
			// closing resources 
			this.in.close(); 
			this.out.close(); 
			this.s.close(); 

		}catch(IOException e){ 
			e.printStackTrace(); 
		}
	}

	//Method to send responses and database items to mobile
	public void sendMessageToMobile(final String str) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "utf-8"), true);

					if (!str.isEmpty()){
						out.println(str);
						out.flush();
						System.out.println("sent to mobile: "+ str);
					}          
				}
				catch (IOException e) {
					e.printStackTrace();
				}    
			}
		}).start();
	}
}
