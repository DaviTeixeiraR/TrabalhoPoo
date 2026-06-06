package view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import controler.DadosPesController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.DadosPessoais;
import model.ThemeManager;
import util.Validador;

/**
 * Tela de Clientes — CRUD completo.
 * Layout split: tabela à esquerda (listagem) + formulário à direita (add/edit).
 */
public class TelaDadosPessoais {

    // ── Tabela ─────────────────────────────────────────────────
    private TableView<DadosPessoais> tabela;

    // ── Campos do formulário ───────────────────────────────────
    private TextField  tfCpf;
    private TextField  tfNome;
    private TextField  tfEmail;
    private TextField  tfTelefone;
    private DatePicker dpNascimento;

    // ── Estado do formulário ───────────────────────────────────
    private DadosPessoais clienteSelecionado = null; // null = modo adição

    // ── Elementos dinâmicos do formulário ─────────────────────
    private Label  lblFormTitulo;
    private Label  lblFeedback;
    private Button btnPrimario;   // "Cadastrar" ou "Salvar Alterações"
    private Button btnExcluir;
    private Button btnCancelar;

    // ─────────────────────────────────────────────────────────
    // PONTO DE ENTRADA
    // ─────────────────────────────────────────────────────────

    public Node getNode() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: " + ThemeManager.bg() + ";");
        VBox.setVgrow(root, Priority.ALWAYS);

        // Cabeçalho
        Label lblTitulo = new Label("👤  Clientes");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Label lblSub = new Label("Cadastre, edite ou exclua clientes do sistema.");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web(ThemeManager.subtitleColor()));

        VBox titulo = new VBox(4, lblTitulo, lblSub);

        // Split
        HBox main = new HBox(20);
        VBox.setVgrow(main, Priority.ALWAYS);
        main.getChildren().addAll(criarPainelTabela(), criarPainelForm());

        root.getChildren().addAll(titulo, main);

        carregarClientes();
        return root;
    }

    // ─────────────────────────────────────────────────────────
    // PAINEL ESQUERDO — TABELA
    // ─────────────────────────────────────────────────────────

    private VBox criarPainelTabela() {
        VBox panel = new VBox(12);
        HBox.setHgrow(panel, Priority.ALWAYS);
        panel.setPadding(new Insets(20));
        panel.setStyle(ThemeManager.cardStyle());

        Label lblSec = new Label("CLIENTES CADASTRADOS");
        lblSec.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        lblSec.setTextFill(Color.web(ThemeManager.sidebarLabel()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAtualizar = new Button("↻ Atualizar");
        btnAtualizar.setFont(Font.font("Arial", 11));
        btnAtualizar.setStyle(estiloSecundario());
        btnAtualizar.setOnAction(e -> carregarClientes());

        Button btnNovo = new Button("+ Novo Cliente");
        btnNovo.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        btnNovo.setStyle(estiloBtnVerde());
        btnNovo.setOnMouseEntered(e -> btnNovo.setStyle(estiloBtnVerdeHover()));
        btnNovo.setOnMouseExited(e  -> btnNovo.setStyle(estiloBtnVerde()));
        btnNovo.setOnAction(e -> resetarFormParaAdicao());

        HBox header = new HBox(8, lblSec, spacer, btnAtualizar, btnNovo);
        header.setAlignment(Pos.CENTER_LEFT);

        tabela = new TableView<>();
        tabela.setStyle(ThemeManager.tableStyle());
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        configurarColunas();

        // Clique em linha → preenche formulário de edição
        tabela.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newVal) -> { if (newVal != null) preencherFormParaEdicao(newVal); }
        );

        panel.getChildren().addAll(header, tabela);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private void configurarColunas() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<DadosPessoais, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colCpf.setCellFactory(col -> celulaTexto());
        colCpf.setMinWidth(120);

        TableColumn<DadosPessoais, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colNome.setCellFactory(col -> celulaTexto());
        colNome.setMinWidth(180);

        TableColumn<DadosPessoais, String> colEmail = new TableColumn<>("E-mail");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setCellFactory(col -> celulaTexto());
        colEmail.setMinWidth(190);

        TableColumn<DadosPessoais, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTelefone.setCellFactory(col -> celulaTexto());
        colTelefone.setMinWidth(120);

        TableColumn<DadosPessoais, String> colNasc = new TableColumn<>("Nascimento");
        colNasc.setCellValueFactory(cd -> {
            LocalDate d = cd.getValue().getDataNascimento();
            return new SimpleStringProperty(d != null ? d.format(fmt) : "");
        });
        colNasc.setCellFactory(col -> celulaTexto());
        colNasc.setMinWidth(110);

        tabela.getColumns().addAll(colCpf, colNome, colEmail, colTelefone, colNasc);
    }

    /**
     * Célula de texto com cor definida pelo tema atual.
     * Necessário porque o Modena CSS sobrescreve text-fill nas TableCells
     * mesmo quando o ThemeManager define a cor no TableView pai.
     */
    private TableCell<DadosPessoais, String> celulaTexto() {
        return new TableCell<DadosPessoais, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(ThemeManager.tableCellTextColor());
            }
        };
    }

    // ─────────────────────────────────────────────────────────
    // PAINEL DIREITO — FORMULÁRIO
    // ─────────────────────────────────────────────────────────

    private VBox criarPainelForm() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(370);
        panel.setMinWidth(350);
        panel.setPadding(new Insets(25, 28, 25, 28));
        panel.setStyle(ThemeManager.cardStyle());

        lblFormTitulo = new Label("➕  Novo Cliente");
        lblFormTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblFormTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Separator sep = new Separator();

        // Campos
        VBox boxCpf = campo("CPF: *");
        tfCpf = textField("Somente números (11 dígitos)");
        boxCpf.getChildren().add(tfCpf);

        VBox boxNome = campo("Nome Completo: *");
        tfNome = textField("Nome e sobrenome");
        boxNome.getChildren().add(tfNome);

        VBox boxEmail = campo("E-mail: *");
        tfEmail = textField("exemplo@email.com");
        boxEmail.getChildren().add(tfEmail);

        VBox boxTelefone = campo("Telefone: *");
        tfTelefone = textField("Ex: (11) 99999-9999");
        boxTelefone.getChildren().add(tfTelefone);

        VBox boxData = campo("Nascimento: *  (mínimo 16 anos)");
        dpNascimento = new DatePicker();
        dpNascimento.setPromptText("Selecione a data");
        dpNascimento.setPrefWidth(Double.MAX_VALUE);
        ThemeManager.applyDatePickerStyle(dpNascimento);
        dpNascimento.setOnAction(e -> resetarFeedback());
        boxData.getChildren().add(dpNascimento);

        // Feedback
        lblFeedback = new Label();
        lblFeedback.setFont(Font.font("Arial", 12));
        lblFeedback.setWrapText(true);

        // Botão primário
        btnPrimario = new Button("Cadastrar Cliente");
        btnPrimario.setPrefWidth(Double.MAX_VALUE);
        btnPrimario.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnPrimario.setStyle(estiloBtnVerde());
        btnPrimario.setOnMouseEntered(e -> btnPrimario.setStyle(
            clienteSelecionado == null ? estiloBtnVerdeHover() : estiloBtnAzulHover()));
        btnPrimario.setOnMouseExited(e  -> btnPrimario.setStyle(
            clienteSelecionado == null ? estiloBtnVerde() : estiloBtnAzul()));
        btnPrimario.setOnAction(e -> handleSalvar());

        // Botão excluir
        btnExcluir = new Button("Excluir Cliente");
        btnExcluir.setPrefWidth(Double.MAX_VALUE);
        btnExcluir.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnExcluir.setStyle(estiloBtnVermelho());
        btnExcluir.setOnMouseEntered(e -> btnExcluir.setStyle(estiloBtnVermelhoHover()));
        btnExcluir.setOnMouseExited(e  -> btnExcluir.setStyle(estiloBtnVermelho()));
        btnExcluir.setOnAction(e -> handleExcluir());
        btnExcluir.setVisible(false);
        btnExcluir.setManaged(false);

        // Botão cancelar
        btnCancelar = new Button("Cancelar edição");
        btnCancelar.setPrefWidth(Double.MAX_VALUE);
        btnCancelar.setFont(Font.font("Arial", 12));
        btnCancelar.setStyle(estiloSecundario());
        btnCancelar.setOnAction(e -> resetarFormParaAdicao());
        btnCancelar.setVisible(false);
        btnCancelar.setManaged(false);

        panel.getChildren().addAll(
            lblFormTitulo, sep,
            boxCpf, boxNome, boxEmail, boxTelefone, boxData,
            lblFeedback, btnPrimario, btnExcluir, btnCancelar
        );

        return panel;
    }

    // ─────────────────────────────────────────────────────────
    // LÓGICA CRUD
    // ─────────────────────────────────────────────────────────

    private void carregarClientes() {
        tabela.setItems(FXCollections.observableArrayList(
            new DadosPesController().listar()
        ));
        resetarFormParaAdicao();
    }

    private void handleSalvar() {
        if (clienteSelecionado == null) {
            // Modo adição
            String erro = validar(false);
            if (erro != null) { mostrarErro(erro); return; }

            DadosPessoais novo = new DadosPessoais(
                tfCpf.getText().trim(),
                tfNome.getText().trim(),
                tfTelefone.getText().trim(),
                tfEmail.getText().trim(),
                dpNascimento.getValue()
            );

            if (new DadosPesController(novo).inserir()) {
                mostrarSucesso("✔  Cliente cadastrado com sucesso!");
                carregarClientes();
            } else {
                mostrarErro("Falha ao cadastrar. O CPF já pode estar em uso.");
            }
        } else {
            // Modo edição
            String erro = validar(true);
            if (erro != null) { mostrarErro(erro); return; }

            DadosPessoais atualizado = new DadosPessoais(
                clienteSelecionado.getCpf(), // PK inalterada
                tfNome.getText().trim(),
                tfTelefone.getText().trim(),
                tfEmail.getText().trim(),
                dpNascimento.getValue()
            );

            if (new DadosPesController(atualizado).atualizar()) {
                mostrarSucesso("✔  Alterações salvas!");
                carregarClientes();
            } else {
                mostrarErro("Falha ao salvar alterações.");
            }
        }
    }

    private void handleExcluir() {
        if (clienteSelecionado == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText(null);
        confirm.setContentText(
            "Excluir o cliente \"" + clienteSelecionado.getNomeCliente()
            + "\" (CPF: " + clienteSelecionado.getCpf() + ")?\n\nEsta ação não pode ser desfeita."
        );

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                DadosPesController ctrl = new DadosPesController();
                if (ctrl.deletar(clienteSelecionado.getCpf())) {
                    carregarClientes();
                } else if ("HAS_HISTORY".equals(ctrl.getUltimoErro())) {
                    mostrarErro("Não é possível excluir: este cliente possui histórico de hospedagem. "
                        + "Clientes com reservas registradas não podem ser removidos para preservar os dados financeiros do hotel.");
                } else {
                    mostrarErro("Erro inesperado ao excluir o cliente.");
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    // TRANSIÇÕES DE MODO
    // ─────────────────────────────────────────────────────────

    private void preencherFormParaEdicao(DadosPessoais c) {
        clienteSelecionado = c;

        lblFormTitulo.setText("✏  Editar Cliente");

        tfCpf.setText(c.getCpf());
        tfCpf.setDisable(true); // CPF é PK — não pode ser alterado
        tfNome.setText(c.getNomeCliente() != null ? c.getNomeCliente() : "");
        tfEmail.setText(c.getEmail() != null ? c.getEmail() : "");
        tfTelefone.setText(c.getTelefone() != null ? c.getTelefone() : "");
        dpNascimento.setValue(c.getDataNascimento());

        btnPrimario.setText("Salvar Alterações");
        btnPrimario.setStyle(estiloBtnAzul());

        btnExcluir.setVisible(true);
        btnExcluir.setManaged(true);
        btnCancelar.setVisible(true);
        btnCancelar.setManaged(true);

        resetarFeedback();
    }

    private void resetarFormParaAdicao() {
        clienteSelecionado = null;
        tabela.getSelectionModel().clearSelection();

        lblFormTitulo.setText("➕  Novo Cliente");

        tfCpf.clear();
        tfCpf.setDisable(false);
        tfNome.clear();
        tfEmail.clear();
        tfTelefone.clear();
        dpNascimento.setValue(null);

        btnPrimario.setText("Cadastrar Cliente");
        btnPrimario.setStyle(estiloBtnVerde());

        btnExcluir.setVisible(false);
        btnExcluir.setManaged(false);
        btnCancelar.setVisible(false);
        btnCancelar.setManaged(false);

        resetarFeedback();
    }

    // ─────────────────────────────────────────────────────────
    // VALIDAÇÃO
    // ─────────────────────────────────────────────────────────

    private String validar(boolean modoEdicao) {
        String erro;
        if (!modoEdicao) {
            erro = Validador.validarCpf(tfCpf.getText().trim());
            if (erro != null) { tfCpf.requestFocus(); return erro; }
        }
        erro = Validador.validarNome(tfNome.getText().trim());
        if (erro != null) { tfNome.requestFocus(); return erro; }

        erro = Validador.validarEmail(tfEmail.getText().trim());
        if (erro != null) { tfEmail.requestFocus(); return erro; }

        erro = Validador.validarTelefone(tfTelefone.getText().trim());
        if (erro != null) { tfTelefone.requestFocus(); return erro; }

        erro = Validador.validarNascimento(dpNascimento.getValue(), 16);
        if (erro != null) { dpNascimento.requestFocus(); return erro; }

        return null;
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS DE CONSTRUÇÃO
    // ─────────────────────────────────────────────────────────

    private VBox campo(String labelTexto) {
        Label lbl = new Label(labelTexto);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(ThemeManager.labelColor()));
        return new VBox(5, lbl);
    }

    private TextField textField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font(13));
        tf.setStyle(ThemeManager.fieldStyle());
        tf.setOnKeyTyped(e -> resetarFeedback());
        return tf;
    }

    // ─────────────────────────────────────────────────────────
    // FEEDBACK
    // ─────────────────────────────────────────────────────────

    private void mostrarErro(String msg) {
        lblFeedback.setTextFill(Color.web("#E74C3C"));
        lblFeedback.setText("⚠  " + msg);
    }

    private void mostrarSucesso(String msg) {
        lblFeedback.setTextFill(Color.web("#27AE60"));
        lblFeedback.setText(msg);
    }

    private void resetarFeedback() {
        lblFeedback.setText("");
    }

    // ─────────────────────────────────────────────────────────
    // ESTILOS
    // ─────────────────────────────────────────────────────────

    private String estiloBtnVerde()         { return btn("#27AE60"); }
    private String estiloBtnVerdeHover()    { return btn("#1E8449"); }
    private String estiloBtnAzul()          { return btn("#2980B9"); }
    private String estiloBtnAzulHover()     { return btn("#1F6691"); }
    private String estiloBtnVermelho()      { return btn("#E74C3C"); }
    private String estiloBtnVermelhoHover() { return btn("#C0392B"); }

    private String btn(String cor) {
        return "-fx-background-color: " + cor + "; -fx-text-fill: white;"
             + "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;";
    }

    private String estiloSecundario() {
        return "-fx-background-color: " + ThemeManager.card() + ";"
             + "-fx-border-color: " + ThemeManager.fieldBorder() + ";"
             + "-fx-border-radius: 6; -fx-padding: 8 14; -fx-cursor: hand;"
             + "-fx-text-fill: " + ThemeManager.labelColor() + ";";
    }
}
