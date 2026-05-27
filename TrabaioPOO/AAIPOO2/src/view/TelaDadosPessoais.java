package view;

import java.sql.SQLException;
import java.time.LocalDate;

import controler.DadosPesController;
import controler.QuartoController; // Nota: Se mudar para PessoaController, lembre de atualizar aqui
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.DadosPessoais; // Nota: Se criar o model Pessoa, substitua aqui

public class TelaDadosPessoais { // cadastro de pessoas

	private TextField tfNumCPF; // Campo para cadastro cpf
	private TextField tfNomePessoa; // Campo para nome
	private TextField tfTelefone; // Campo para telefone
	private TextField tfEmail;
	private DatePicker tfDtNascimento;
	private Label lblErro; // Label para exibir mensagens

	private DadosPesController dadosPesCtrl;

	public Scene getScene() {
		// VBox principal
		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(40));

		root.setStyle("-fx-background-color: linear-gradient(to bottom, #F5F0E8, #F5F0E8);");

		// Título
		Label lblTitulo = new Label("Cadastro - Cliente");
		lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
		lblTitulo.setTextFill(Color.DARKBLUE);

		// HBox CPF
		HBox hboxCPF = new HBox(15);
		hboxCPF.setAlignment(Pos.CENTER_LEFT);
		Label lblCPF = new Label("CPF:");
		lblCPF.setFont(Font.font(14));
		tfNumCPF = new TextField();
		tfNumCPF.setPrefWidth(250);
		tfNumCPF.setPromptText("Digite o CPF (somente números)");
		hboxCPF.getChildren().addAll(lblCPF, tfNumCPF);

		// HBox Nome
		HBox hboxNome = new HBox(15);
		hboxNome.setAlignment(Pos.CENTER_LEFT);
		Label lblNome = new Label("Nome:");
		lblNome.setFont(Font.font(14));
		tfNomePessoa = new TextField();
		tfNomePessoa.setPrefWidth(250);
		tfNomePessoa.setPromptText("Digite o nome completo");
		hboxNome.getChildren().addAll(lblNome, tfNomePessoa);

		// HBox E-mail
		HBox hboxEmail = new HBox(15);
		hboxNome.setAlignment(Pos.CENTER_LEFT);
		Label lblEmail = new Label("Email:");
		lblNome.setFont(Font.font(14));
		tfEmail = new TextField();
		tfEmail.setPrefWidth(250);
		tfEmail.setPromptText("Digite o seu e-mail");
		hboxEmail.getChildren().addAll(lblEmail, tfEmail);

		// HBox Telefone
		HBox hboxTelefone = new HBox(15);
		hboxTelefone.setAlignment(Pos.CENTER_LEFT);
		Label lblTelefone = new Label("Telefone:");
		lblTelefone.setFont(Font.font(14));
		tfTelefone = new TextField();
		tfTelefone.setPrefWidth(250);
		tfTelefone.setPromptText("Ex: (11) 99999-9999");
		hboxTelefone.getChildren().addAll(lblTelefone, tfTelefone);
		
		 // HBox Data de Nascimento
        HBox hboxDtNascimento = new HBox(15);
        hboxDtNascimento.setAlignment(Pos.CENTER_LEFT);
        Label lblDtNascimento = new Label("Data de Nascimento:");
        lblDtNascimento.setFont(Font.font(14));
        tfDtNascimento = new DatePicker();
        tfDtNascimento.setPrefWidth(250);
        tfDtNascimento.setPromptText("Selecione a data");
        hboxDtNascimento.getChildren().addAll(lblDtNascimento, tfDtNascimento);
		
		// Botão Cadastro
		Button btnCadastro = new Button("Cadastrar");
		btnCadastro.setPrefWidth(150);
		btnCadastro.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		btnCadastro.setStyle(
				"-fx-background-color: #00FF00; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
		btnCadastro.setOnAction(e -> {
			try {
				handleLogin(); // O método foi mantido com esse nome, mas faz a lógica do cadastro
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});

		// Label Erro
		lblErro = new Label();
		lblErro.setTextFill(Color.RED);
		lblErro.setFont(Font.font(12));
		lblErro.setStyle("-fx-padding: 10 0 0 0;");

		// Adicionando os novos componentes na árvore visual do JavaFX
		root.getChildren().addAll(lblTitulo, hboxCPF, hboxNome, hboxTelefone, btnCadastro, lblErro);

		Scene scene = new Scene(root, 450, 450);
		return scene;
	}

	private boolean validarCampos() {
		String cpf = tfNumCPF.getText().trim();
		String nome = tfNomePessoa.getText().trim();
		String telefone = tfTelefone.getText().trim();
		String email = tfEmail.getText().trim();
		LocalDate DtNascimento = tfDtNascimento.getValue();

		return !cpf.isEmpty() && !nome.isEmpty() && !telefone.isEmpty() && !email.isEmpty() && DtNascimento != null;
	}

	private void handleLogin() throws SQLException {
		lblErro.setText(""); // Limpa mensagem anterior
		if (validarCampos()) {
			lblErro.setTextFill(Color.GREEN);
			lblErro.setText("Cadastro realizado com sucesso!");

			String cpf = tfNumCPF.getText().trim();
			String nome = tfNomePessoa.getText().trim();
			String telefone = tfTelefone.getText().trim();

			// ATENÇÃO: Mantive a chamada original do Quarto, mas você provavelmente vai
			// querer substituir isso por um modelo 'Pessoa' e um 'PessoaController'.
			DadosPessoais dadosPessoais = new DadosPessoais(cpf, nomeCliente, telefone, email, DtNascimento);
			dadosPesCtrl = new DadosPesController(dadosPesCtrl);
			DadosPesController.salvarDadosPessoais();

		} else {
			lblErro.setTextFill(Color.RED);
			lblErro.setText("Por favor, preencha todos os campos obrigatórios!");
			tfNumCPF.requestFocus(); // Foca no primeiro campo vazio (CPF)
		}
	}
}