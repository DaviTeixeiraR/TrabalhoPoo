package view;

import java.sql.SQLException;

import controler.QuartoController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Quarto;

public class TelaQuartos {// cadastro de quarto

	private TextField tfCodQuarto;
	private TextField tfNumQuarto; // Campo para cadastro quarto
	private TextField tfQtdPessoas; // Campo para quantidade de pessoas
	private TextField tfPreco; // NOVO: Campo para preço do quarto
	private Label lblErro; // Label para exibir mensagens

	private QuartoController quartoCtrl;

	public Node getNode() {
		return getScene().getRoot();
	}

	public Scene getScene() {
		// VBox principal
		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(40));

		root.setStyle("-fx-background-color: linear-gradient(to bottom, #F5F0E8, #F5F0E8);");

		// Título
		Label lblTitulo = new Label("Cadastro - Quarto");
		lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
		lblTitulo.setTextFill(Color.DARKBLUE);

		// HBox Codigo Quarto
		HBox hboxCodQuarto = new HBox(15);
		hboxCodQuarto.setAlignment(Pos.CENTER_LEFT);
		Label lblCodQuarto = new Label("Código do Quarto:");
		lblCodQuarto.setFont(Font.font(14));
		tfCodQuarto = new TextField();
		tfCodQuarto.setPrefWidth(250);
		tfCodQuarto.setPromptText("Digite o código do quarto ");
		hboxCodQuarto.getChildren().addAll(lblCodQuarto, tfCodQuarto);

		// HBox Número Quarto
		HBox hboxNumQuarto = new HBox(15);
		hboxNumQuarto.setAlignment(Pos.CENTER_LEFT);
		Label lblNumQuarto = new Label("Nº Quarto:");
		lblNumQuarto.setFont(Font.font(14));
		tfNumQuarto = new TextField();
		tfNumQuarto.setPrefWidth(250);
		tfNumQuarto.setPromptText("Digite o número do quarto ");
		hboxNumQuarto.getChildren().addAll(lblNumQuarto, tfNumQuarto);

		// HBox Quantidade de Pessoas
		HBox hboxQtd = new HBox(15);
		hboxQtd.setAlignment(Pos.CENTER_LEFT);
		Label lblQtd = new Label("Qtd. Pessoas:");
		lblQtd.setFont(Font.font(14));
		tfQtdPessoas = new TextField();
		tfQtdPessoas.setPrefWidth(250);
		tfQtdPessoas.setPromptText("Ex: 2");
		hboxQtd.getChildren().addAll(lblQtd, tfQtdPessoas);

		// HBox Preço
		HBox hboxPreco = new HBox(15);
		hboxPreco.setAlignment(Pos.CENTER_LEFT);
		Label lblPreco = new Label("Preço Diária:");
		lblPreco.setFont(Font.font(14));
		tfPreco = new TextField();
		tfPreco.setPrefWidth(250);
		tfPreco.setPromptText("Ex: 150.00");
		hboxPreco.getChildren().addAll(lblPreco, tfPreco);

		// Botão Cadastro
		Button btnCadastro = new Button("Cadastrar");
		btnCadastro.setPrefWidth(150);
		btnCadastro.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		btnCadastro.setStyle(
				"-fx-background-color: #00FF00; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
		btnCadastro.setOnAction(e -> {
			try {
				handleLogin();
			} catch (SQLException e1) {

				e1.printStackTrace();
			}
		});

		// Label Erro
		lblErro = new Label();
		lblErro.setTextFill(Color.RED);
		lblErro.setFont(Font.font(12));
		lblErro.setStyle("-fx-padding: 10 0 0 0;");

		root.getChildren().addAll(lblTitulo, hboxCodQuarto, hboxNumQuarto, hboxQtd, hboxPreco, btnCadastro, lblErro);
		Scene scene = new Scene(root, 450, 450);
		return scene;
	}

	private boolean validarCampos() {
		String cadastro = tfNumQuarto.getText().trim();
		String qtd = tfQtdPessoas.getText().trim();
		String preco = tfPreco.getText().trim();
		String codQuarto = tfCodQuarto.getText().trim();
		
		return !codQuarto.isEmpty() && !cadastro.isEmpty() && !qtd.isEmpty() && !preco.isEmpty();
	}

	private void handleLogin() throws SQLException {
		lblErro.setText(""); // Limpa mensagem anterior
		if (validarCampos()) {
			lblErro.setTextFill(Color.GREEN);
			lblErro.setText("Cadastro realizado com sucesso!");
			String codQuarto =  tfCodQuarto.getText().trim();
			String numQuarto = tfNumQuarto.getText().trim();
			int qtdPessoas = Integer.parseInt(tfQtdPessoas.getText().trim());
			double preco = Double.parseDouble(tfPreco.getText().trim());

			Quarto quarto = new Quarto(codQuarto, numQuarto, qtdPessoas, preco, 1);
			quartoCtrl = new QuartoController(quarto);
			quartoCtrl.salvarQuarto();

		} else {
			lblErro.setTextFill(Color.RED);
			lblErro.setText("Por favor, preencha todos os campos!");
			tfNumQuarto.requestFocus(); // Foca no campo livro
		}
	}
}