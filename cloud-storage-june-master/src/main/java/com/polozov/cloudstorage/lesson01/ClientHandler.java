package com.polozov.cloudstorage.lesson01;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream())
		) {
			System.out.printf("Client %s connected\n", socket.getInetAddress());
			while (true) {
				String command = in.readUTF();
				if ("upload".equals(command)) {
					try {
						File file = new File("server"  + File.separator + in.readUTF());
						if (!file.exists()) {
							 file.createNewFile();
						}
						FileOutputStream fos = new FileOutputStream(file);

						long size = in.readLong();
						byte[] buffer = new byte[8 * 1024];

						for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
							int read = in.read(buffer);
							fos.write(buffer, 0, read);
						}
						System.out.println(command+" "+file.getName()+" completed");
						out.writeUTF("Uploading "+file.getName() +" completed");
						fos.close();
					} catch (Exception e) {
						out.writeUTF("FATAL ERROR");
					}
				}

				else if ("download".equals(command)) {
					// TODO: 14.06.2021
					{
						try {
							File file = new File("server"  + File.separator + in.readUTF());
							if (file.exists()) {
								out.writeUTF("exists");
								long fileLength = file.length();
								FileInputStream fis = new FileInputStream(file);
								out.writeLong(fileLength);
								int read = 0;
								byte[] buffer = new byte[8 * 1024];
								while ((read = fis.read(buffer)) != -1) {
									out.write(buffer, 0, read);
								}
								out.flush();
								String status = in.readUTF();
								fis.close();
								System.out.println("Downloading status: " + status);
							}
							else{
								out.writeUTF("File "+file.getName() + " not found on a server");
								System.out.println("File "+file.getName() + " not found");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				else if ("exit".equals(command)) {
					System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
					break;
				}
				else {
					System.out.println(command);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
