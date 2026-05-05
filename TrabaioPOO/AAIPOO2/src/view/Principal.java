package view;

import model.Conexao;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Tela_Login;
import javafx.application.Application;

public class Principal extends Application{

    @Override
    public void start(Stage stage) {
        // Cria a tela de login
        Tela_Login telaLogin = new Tela_Login();
        Scene scene = telaLogin.getScene();

        // Configura o Stage (janela)
        stage.setTitle("Sistema de Biblioteca - Login");
        stage.setScene(scene);
        stage.setResizable(false); // impede redimensionamento
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
