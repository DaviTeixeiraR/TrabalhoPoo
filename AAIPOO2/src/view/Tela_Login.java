package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Tela de Login do sistema.
 * Credenciais fixas: usuário = admin / senha = 1234
 * Após login bem-sucedido, chama Principal.mostrarSistema().
 */
public class Tela_Login {

    private TextField     tfUsuario;
    private PasswordField pfSenha;
    private Label         lblErro;

    public Scene getScene() {
        // ── Root ──
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1E2A4A, #2E4070);");

        // ── Card central ──
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 50, 45, 50));
        card.setMaxWidth(400);
        card.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 16;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 6);"
        );

        // Ícone + título
        Label lblIcon = new Label("🏨");
        lblIcon.setFont(Font.font(44));
        lblIcon.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("Hotel Manager");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));

        Label lblSub = new Label("Faça login para continuar");
        lblSub.setFont(Font.font("Arial", 13));
        lblSub.setTextFill(Color.web("#888"));

        // Separador visual
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #EEE;");

        // Campo usuário
        VBox boxUsuario = new VBox(6);
        Label lblU = new Label("Usuário");
        lblU.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblU.setTextFill(Color.web("#444"));
        tfUsuario = new TextField();
        tfUsuario.setPromptText("Digite seu usuário");
        tfUsuario.setFont(Font.font(14));
        tfUsuario.setStyle(estiloField());
        boxUsuario.getChildren().addAll(lblU, tfUsuario);

        // Campo senha
        VBox boxSenha = new VBox(6);
        Label lblS = new Label("Senha");
        lblS.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblS.setTextFill(Color.web("#444"));
        pfSenha = new PasswordField();
        pfSenha.setPromptText("Digite sua senha");
        pfSenha.setFont(Font.font(14));
        pfSenha.setStyle(estiloField());
        // Enter no campo senha aciona o login
        pfSenha.setOnAction(e -> handleLogin());
        boxSenha.getChildren().addAll(lblS, pfSenha);

        // Label de erro
        lblErro = new Label();
        lblErro.setFont(Font.font("Arial", 12));
        lblErro.setWrapText(true);

        // Botão entrar
        Button btnEntrar = new Button("Entrar no Sistema");
        btnEntrar.setPrefWidth(Double.MAX_VALUE);
        btnEntrar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnEntrar.setStyle(
            "-fx-background-color: #C9A84C;"
          + "-fx-text-fill: white;"
          + "-fx-background-radius: 8;"
          + "-fx-padding: 12 20;"
          + "-fx-cursor: hand;"
        );
        btnEntrar.setOnMouseEntered(e ->
            btnEntrar.setStyle(
                "-fx-background-color: #B8932B; -fx-text-fill: white;"
              + "-fx-background-radius: 8; -fx-padding: 12 20; -fx-cursor: hand;"
            )
        );
        btnEntrar.setOnMouseExited(e ->
            btnEntrar.setStyle(
                "-fx-background-color: #C9A84C; -fx-text-fill: white;"
              + "-fx-background-radius: 8; -fx-padding: 12 20; -fx-cursor: hand;"
            )
        );
        btnEntrar.setOnAction(e -> handleLogin());

        // Dica de credenciais
        Label lblDica = new Label("💡  Usuário: admin  |  Senha: 1234");
        lblDica.setFont(Font.font("Arial", 11));
        lblDica.setTextFill(Color.web("#AAA"));

        card.getChildren().addAll(
            lblIcon, lblTitulo, lblSub, sep,
            boxUsuario, boxSenha, lblErro, btnEntrar, lblDica
        );

        BorderPane.setAlignment(card, Pos.CENTER);
        BorderPane.setMargin(card, new Insets(60));
        root.setCenter(card);

        // Sem tamanho fixo: o Stage já está setMaximized(true), a Scene se adapta.
        return new Scene(root);
    }

    // ── Lógica de login ───────────────────────────────────────

    private void handleLogin() {
        lblErro.setText("");
        String usuario = tfUsuario.getText().trim();
        String senha   = pfSenha.getText().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblErro.setTextFill(Color.web("#E74C3C"));
            lblErro.setText("⚠  Preencha usuário e senha!");
            return;
        }

        // Credenciais válidas: admin / 1234
        if (usuario.equals("admin") && senha.equals("1234")) {
            lblErro.setTextFill(Color.web("#27AE60"));
            lblErro.setText("✔  Login realizado! Carregando...");
            // Navega para o sistema principal
            Principal.mostrarSistema();
        } else {
            lblErro.setTextFill(Color.web("#E74C3C"));
            lblErro.setText("✖  Usuário ou senha incorretos!");
            pfSenha.clear();
            pfSenha.requestFocus();
        }
    }

    private String estiloField() {
        return "-fx-background-color: #F8F9FA;"
             + "-fx-border-color: #DDE3EC;"
             + "-fx-border-radius: 6;"
             + "-fx-background-radius: 6;"
             + "-fx-padding: 10 12;"
             + "-fx-font-size: 13px;";
    }
}