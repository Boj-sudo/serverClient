package serverClient;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Handler implements Runnable{

	// Connection
	private Socket connectionToClient;
	
	// Text streams:
	private PrintWriter print_writer = null;
	private BufferedReader buffered_reader = null;
	
	// Binary streams:
	private DataInputStream data_input = null;
	private DataOutputStream data_output = null;
	
	// Bytes stream:
	private InputStream input_s = null;
	private OutputStream output_s = null;
	
	private boolean processing;
	private boolean authenticated;
	
	public Handler(Socket newConnectionToClient) {
		this.connectionToClient = newConnectionToClient;
		try {
			input_s = connectionToClient.getInputStream();
			output_s = connectionToClient.getOutputStream();
			print_writer = new PrintWriter(output_s);
			buffered_reader = new BufferedReader(new InputStreamReader(input_s));
			data_input = new DataInputStream(input_s);
			data_output = new DataOutputStream(output_s);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void run() {
		System.out.println("Start processing commands");
		processing = true;
		
		try {
			while(processing) {
				String request_line = buffered_reader.readLine();
				System.out.println("Got requested line: " + request_line);
				StringTokenizer str_tokenizer = new StringTokenizer(request_line);
				String str_commands = str_tokenizer.nextToken();
				
				if(str_commands.equals("AUTH")) {
					String str_username = str_tokenizer.nextToken();
					String str_password = str_tokenizer.nextToken();
					
					if(matchUser(str_username, str_password)) {
						sendMessage("200 successfully logged in");
						authenticated = true;
						System.out.println("logged in");
					} else {
						sendMessage("500 unsuccessfully logged in");
						authenticated = false;
					}
				} 
				else if(str_commands.equals("LIST")) {
					System.out.println("LIST command received");
					
					if(authenticated == true) {
						ArrayList<String> fileArrList = getFileList();
						String strToSend = "";
						
						for(String s: fileArrList) {
							strToSend += s + "#";
						}
						
						sendMessage(strToSend);
						System.out.println("String to send: " + strToSend);
					} else {
						sendMessage("500 not authenticated");
					}
				}
				else if(str_commands.equals("PDFRET")) {
					if(authenticated == true) {
						String file_id = str_tokenizer.nextToken();
						String file_name = IdToFile(file_id);
						File pdfFile = new File("data/server/" + file_name);
						
						if(pdfFile.exists()) {
							System.out.println("file found");
							sendMessage("200 " + pdfFile.length() + " bytes");
							// When sending a file
							FileInputStream file_in = new FileInputStream(pdfFile);
							byte[] buffer = new byte[1024];
							int n = 0;
							
							while((n = file_in.read(buffer)) > 0) {
								data_output.write(buffer, 0, n);
								data_output.flush();
							}
							
							file_in.close();
							System.out.println("File sent to client");
						}
					}
				}
				else if(str_commands.equals("LOGOUT")) {
					if(authenticated) {
						authenticated = false;
						sendMessage("200 logged out");
						
						data_output.close();
						data_input.close();
						
						print_writer.close();
						buffered_reader.close();
						
						connectionToClient.close();
						
						processing = false;
					} else {
						sendMessage("500 not authenticated");
					}
				} else {
					// Invalid command
				}
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			System.out.println("finally");
		}
	}
	
	private void sendMessage(String message) {
		print_writer.println(message);
		print_writer.flush();
	}
	
	private boolean matchUser(String username, String password) {
		boolean found = false;
		File userFile = new File("data/server/users.txt");
		
		try {
			Scanner scan = new Scanner(userFile);
			while(scan.hasNextLine() && !found) {
				String line = scan.nextLine();
				String lineSec[] = line.split("\\s");
				
				if(username.equals(lineSec[0]) && password.equals(lineSec[1])) {
					found = true;
				}
			}
			
			scan.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		return found;
	}
	
	private String IdToFile(String id) {
		String result = "";
		File list_file = new File("data/server/PdfList.txt");
		
		try {
			Scanner scan = new Scanner(list_file);
			String line = "";
			
			while(scan.hasNext()) {
				line = scan.nextLine();
				StringTokenizer str_tokenizer = new StringTokenizer(line);
				String str_id = str_tokenizer.nextToken();
				String str_name = str_tokenizer.nextToken();
				
				if(str_id.equals(id)) {
					result = str_name;
				}
			}
			
			scan.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		return result;
	}
	
	private ArrayList<String> getFileList() {
		ArrayList<String> result = new ArrayList<String>();
		File listFile = new File("data/server/PdfList.txt");
		
		try {
			Scanner scan = new Scanner(listFile);
			
			while(scan.hasNext()) {
				result.add(scan.nextLine());
			}
			
			scan.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		return result;
	}
}
