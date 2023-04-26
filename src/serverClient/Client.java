package serverClient;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		// Create the clientPane, setup the scene and the stage
		primaryStage.setTitle("PDF return application");
		
		ClientPane root = new ClientPane();
		
		Scene scene = new Scene(root, 600, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		// launch the javafx application
		launch(args);
	}
}
