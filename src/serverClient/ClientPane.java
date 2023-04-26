package serverClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ClientPane extends GridPane {

	// Connection:
	private Socket client_socket = null;
	
	// Text streams:
	private PrintWriter print_writer = null;
	private BufferedReader buffered_reader = null;
	
	// Binary streams:
	private DataInputStream data_input = null;
	private DataOutputStream data_output = null;
	
	// Byte streams:
	private InputStream input_s = null;
	private OutputStream output_s = null;
	
	public ClientPane() {
		GridPane grid_pane = new GridPane();
		grid_pane.setAlignment(Pos.CENTER);
		grid_pane.setVgap(10);
		grid_pane.setHgap(10);
		
		ObservableList<String> file_list = FXCollections.observableArrayList();
		
		Button btn_connect = new Button("Connect");
		grid_pane.add(btn_connect, 1, 0);
		
		Label lbl_username = new Label("Username:");
		grid_pane.add(lbl_username, 2, 0);
		
		TextField txt_username = new TextField();
		grid_pane.add(txt_username, 3, 0);
		
		Label lbl_password = new Label("Password:");
		grid_pane.add(lbl_password, 4, 0);
		
		TextField txt_password = new TextField();
		grid_pane.add(txt_password, 5, 0);
		
		Button btn_login = new Button("Login");
		grid_pane.add(btn_login, 6, 0);
		
		Button btn_list = new Button("List");
		grid_pane.add(btn_list, 1, 1);
		
		Button btn_pdfret = new Button("PDFRET");
		grid_pane.add(btn_pdfret, 2, 1);
		
		Button btn_logout = new Button("Logout");
		grid_pane.add(btn_logout, 6, 1);
		
		ListView<String> list_view = new ListView<>(file_list);
		list_view.setPrefHeight(250);
		grid_pane.add(list_view, 1, 2, 6, 1);
		
		TextArea text_area = new TextArea("Server responses: \r\n");
		text_area.setPrefHeight(200);
		grid_pane.add(text_area, 1, 3, 6, 1);
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(grid_pane);
		this.getChildren().add(vbox);
		
		// Use buttons to send commands
		btn_connect.setOnAction((event) ->{
			// Create client connection
			connect("localhost", 2030);
		});
		
		btn_login.setOnAction((event) ->{
			sendCommand("AUTH " + txt_username.getText() + " " + txt_password.getText());
			text_area.appendText(readResponse() + "\r\n");
		});
		
		btn_list.setOnAction((event) ->{
			sendCommand("LIST");
			String str_response = readResponse();
			String[] arr_response = str_response.split("#");
			
			for(int i = 0; i < arr_response.length; i++) {
				file_list.add(arr_response[i]);
			}
			
			list_view.refresh();
		});
		
		btn_pdfret.setOnAction((event) ->{
			// Get the file from the list:
			String file_name = list_view.getSelectionModel().getSelectedItem();
			System.out.println("Full selected file name: " + file_name);
			int selected_id = list_view.getSelectionModel().getSelectedIndex() + 1;
			sendCommand("PDFRET " + selected_id);
			
			String response = readResponse();
			String[] arr_response = response.split("\\s");
			int size = Integer.parseInt(arr_response[1]);
			text_area.appendText(response + "\r\n");
			
			// When receiving a file:
			File fileToReceive = new File("data/client/" + file_name);
			FileOutputStream file_s = null;
			try {
				file_s = new FileOutputStream(fileToReceive);
				byte[] buffer = new byte[1024];
				int n = 0;
				int total_bytes = 0;
				
				while(total_bytes != size) {
					n = data_input.read(buffer, 0, buffer.length);
					file_s.write(buffer, 0, n);
					file_s.flush();
					total_bytes += n;
				}
				
				System.out.println("File saved on client side");
			} catch(FileNotFoundException ex) {
				ex.printStackTrace();
			} catch(IOException ex) {
				ex.printStackTrace();
			} finally {
				if(file_s != null) {
					try {
						file_s.close();
					} catch(IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		btn_logout.setOnAction((event) ->{
			sendCommand("LOGOUT");
			String response = readResponse();
			text_area.appendText(response);
			
			// Try to close the streams:
			try {
				print_writer.close();
				buffered_reader.close();
				
				data_input.close();
				data_output.close();
				
				input_s.close();
				output_s.close();
				
				client_socket.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	// Send the commands
	private void sendCommand(String command) {
		print_writer.println(command);
		print_writer.flush();
	}
	
	// Read responses from the client
	private String readResponse() {
		String response = "";
		
		try {
			response = buffered_reader.readLine();
			System.out.println("Response from the server: " + response);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		return response;
	}
	
	private void connect(String host, int port) {
		try {
			client_socket = new Socket(host, port);
			
			// Set up the streams:
			input_s = client_socket.getInputStream();
			output_s = client_socket.getOutputStream();
			
			buffered_reader = new BufferedReader(new InputStreamReader(input_s));
			print_writer = new PrintWriter(output_s);
			
			data_input = new DataInputStream(input_s);
			data_output = new DataOutputStream(output_s);
			
			System.out.println("Client connected to the server and streams are created");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}
