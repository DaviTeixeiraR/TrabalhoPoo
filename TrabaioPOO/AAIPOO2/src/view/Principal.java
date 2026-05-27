package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Principal extends Application {

	@Override
	public void start(Stage stage) {
		// Cria a tela de login
//		Tela_Login telaLogin = new Tela_Login();
//		Scene scene = telaLogin.getScene();
       TelaQuartos telaQuartos = new TelaQuartos();
        Scene scene = telaQuartos.getScene();
//		TelaDadosPessoais telaDadosPessoais = new TelaDadosPessoais();
//		Scene scene = telaDadosPessoais.getScene();
		
		// Configura o Stage (janela)
		stage.setTitle("Sistema do Hotel - Login");
		stage.setScene(scene);
		stage.setResizable(false); // impede redimensionamento
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
