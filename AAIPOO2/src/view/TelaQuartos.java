package view;

import java.sql.SQLException;

import controler.QuartoController;
import dao.TipoQuartoDao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Conexao;
import model.Quarto;
import model.ThemeManager;
import model.TipoQuarto;
import util.Validador;

/**
 * Tela de Quartos — CRUD completo.
 * Layout split: tabela à esquerda (listagem) + formulário à direita (add/edit).
 */
public class TelaQuartos {

    // ── Tabela ─────────────────────────────────────────────────
    private TableView<Quarto> tabela;

    // ── Campos do formulário ───────────────────────────────────
    private TextField            tfNumQuarto;
    private ComboBox<TipoQuarto> cbTipo;

    // ── Estado do formulário ───────────────────────────────────
    private Quarto quartoSelecionado = null; // null = modo adição

    // ── Elementos dinâmicos do formulário ─────────────────────
    private Label  lblFormTitulo;
    private Label  lblFeedback;
    private Button btnPrimario;
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
        Label lblTitulo = new Label("🔑  Quartos");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Label lblSub = new Label("Cadastre, edite ou exclua quartos do hotel.");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web(ThemeManager.subtitleColor()));

        VBox titulo = new VBox(4, lblTitulo, lblSub);

        // Split
        HBox main = new HBox(20);
        VBox.setVgrow(main, Priority.ALWAYS);
        main.getChildren().addAll(criarPainelTabela(), criarPainelForm());

        root.getChildren().addAll(titulo, main);

        carregarQuartos();
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

        Label lblSec = new Label("QUARTOS CADASTRADOS");
        lblSec.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        lblSec.setTextFill(Color.web(ThemeManager.sidebarLabel()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAtualizar = new Button("↻ Atualizar");
        btnAtualizar.setFont(Font.font("Arial", 11));
        btnAtualizar.setStyle(estiloSecundario());
        btnAtualizar.setOnAction(e -> carregarQuartos());

        Button btnNovo = new Button("+ Novo Quarto");
        btnNovo.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        btnNovo.setStyle(estiloBtnAzul());
        btnNovo.setOnMouseEntered(e -> btnNovo.setStyle(estiloBtnAzulHover()));
        btnNovo.setOnMouseExited(e  -> btnNovo.setStyle(estiloBtnAzul()));
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
        TableColumn<Quarto, Integer> colNum = new TableColumn<>("Nº Quarto");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numeroQuarto"));
        colNum.setCellFactory(col -> celulaInteiro());
        colNum.setMinWidth(90);

        TableColumn<Quarto, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("nomeTipo"));
        colTipo.setCellFactory(col -> celulaTexto());
        colTipo.setMinWidth(160);

        // Status derivado das reservas ativas — verde/laranja/vermelho conforme período
        // Tooltip exibe todas as reservas ativas quando há mais de uma
        TableColumn<Quarto, String> colStatus = new TableColumn<>("Status / Período");
        colStatus.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getStatusDisplay())
        );
        colStatus.setMinWidth(200);
        colStatus.setCellFactory(col -> new TableCell<Quarto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setTooltip(null);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(item);
                setStyle("-fx-font-weight: bold;");
                if (item.equals("Disponível")) {
                    setTextFill(Color.web("#27AE60"));
                } else if (item.startsWith("Ocupado")) {
                    setTextFill(Color.web("#E74C3C"));
                } else if (item.startsWith("Reservado")) {
                    setTextFill(Color.web("#E67E22"));
                } else {
                    setTextFill(Color.web("#C9A84C"));
                }
                // Tooltip com todas as reservas ativas se houver mais de uma
                Quarto q = getTableView().getItems().get(getIndex());
                if (q != null && q.getReservasAtivas().size() > 1) {
                    java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    StringBuilder sb = new StringBuilder("Todas as reservas ativas:\n");
                    for (java.time.LocalDate[] r : q.getReservasAtivas()) {
                        sb.append("• ").append(r[0].format(fmt))
                          .append(" a ").append(r[1].format(fmt)).append("\n");
                    }
                    setTooltip(new Tooltip(sb.toString().trim()));
                }
            }
        });

        TableColumn<Quarto, String> colPreco = new TableColumn<>("Preço/Noite");
        colPreco.setCellValueFactory(cd ->
            new SimpleStringProperty(String.format("R$ %,.2f", cd.getValue().getPrecoBase()))
        );
        colPreco.setCellFactory(col -> celulaTexto());
        colPreco.setMinWidth(110);

        tabela.getColumns().addAll(colNum, colTipo, colStatus, colPreco);
    }

    /** Célula de texto com cor definida pelo tema atual. */
    private TableCell<Quarto, String> celulaTexto() {
        return new TableCell<Quarto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(ThemeManager.tableCellTextColor());
            }
        };
    }

    /** Célula de inteiro com cor definida pelo tema atual. */
    private TableCell<Quarto, Integer> celulaInteiro() {
        return new TableCell<Quarto, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setTextFill(ThemeManager.tableCellTextColor());
            }
        };
    }

    // ─────────────────────────────────────────────────────────
    // PAINEL DIREITO — FORMULÁRIO
    // ─────────────────────────────────────────────────────────

    private VBox criarPainelForm() {
        VBox panel = new VBox(14);
        panel.setPrefWidth(350);
        panel.setMinWidth(330);
        panel.setPadding(new Insets(25, 28, 25, 28));
        panel.setStyle(ThemeManager.cardStyle());

        lblFormTitulo = new Label("➕  Novo Quarto");
        lblFormTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblFormTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Separator sep = new Separator();

        // Nº do Quarto
        VBox boxNum = campo("Nº do Quarto: *  (1 – 9999)");
        tfNumQuarto = new TextField();
        tfNumQuarto.setPromptText("Ex: 101");
        tfNumQuarto.setFont(Font.font(13));
        tfNumQuarto.setStyle(ThemeManager.fieldStyle());
        tfNumQuarto.setOnKeyTyped(e -> resetarFeedback());
        boxNum.getChildren().add(tfNumQuarto);

        // Tipo do Quarto
        VBox boxTipo = campo("Tipo do Quarto: *");
        cbTipo = new ComboBox<>();
        cbTipo.setPromptText("Selecione o tipo");
        cbTipo.setPrefWidth(Double.MAX_VALUE);
        ThemeManager.applyComboStyle(cbTipo);
        carregarTipos();
        boxTipo.getChildren().add(cbTipo);

        // Feedback
        lblFeedback = new Label();
        lblFeedback.setFont(Font.font("Arial", 12));
        lblFeedback.setWrapText(true);

        // Botão primário
        btnPrimario = new Button("Cadastrar Quarto");
        btnPrimario.setPrefWidth(Double.MAX_VALUE);
        btnPrimario.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnPrimario.setStyle(estiloBtnAzul());
        btnPrimario.setOnMouseEntered(e -> btnPrimario.setStyle(
            quartoSelecionado == null ? estiloBtnAzulHover() : estiloBtnVerdeHover()));
        btnPrimario.setOnMouseExited(e  -> btnPrimario.setStyle(
            quartoSelecionado == null ? estiloBtnAzul() : estiloBtnVerde()));
        btnPrimario.setOnAction(e -> handleSalvar());

        // Botão excluir
        btnExcluir = new Button("Excluir Quarto");
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
            boxNum, boxTipo,
            lblFeedback, btnPrimario, btnExcluir, btnCancelar
        );

        return panel;
    }

    // ─────────────────────────────────────────────────────────
    // LÓGICA CRUD
    // ─────────────────────────────────────────────────────────

    private void carregarQuartos() {
        tabela.setItems(FXCollections.observableArrayList(
            new QuartoController().listar()
        ));
        resetarFormParaAdicao();
    }

    private void carregarTipos() {
        try {
            Conexao.conectar();
            TipoQuartoDao dao = new TipoQuartoDao(Conexao.conexao);
            cbTipo.getItems().setAll(dao.listar());
        } catch (SQLException e) {
            // ComboBox fica vazio; o usuário verá o erro ao tentar salvar
        } finally {
            Conexao.desconectar();
        }
    }

    private void handleSalvar() {
        // Validações
        String erroNum = Validador.validarNumeroQuarto(tfNumQuarto.getText());
        if (erroNum != null) { mostrarErro(erroNum); tfNumQuarto.requestFocus(); return; }

        if (cbTipo.getValue() == null) {
            mostrarErro("Selecione o tipo do quarto.");
            return;
        }

        int        num  = Integer.parseInt(tfNumQuarto.getText().trim());
        TipoQuarto tipo = cbTipo.getValue();

        if (quartoSelecionado == null) {
            // Modo adição
            Quarto novo = new Quarto(num, tipo.getIdTipoQuarto(), 1); // status 1 = Disponível
            if (new QuartoController(novo).inserir()) {
                mostrarSucesso("✔  Quarto " + num + " cadastrado com sucesso!");
                carregarQuartos();
            } else {
                mostrarErro("Falha ao cadastrar. O número já pode estar em uso.");
            }
        } else {
            // Modo edição
            Quarto atualizado = new Quarto();
            atualizado.setCodQuarto(quartoSelecionado.getCodQuarto()); // PK
            atualizado.setNumeroQuarto(num);
            atualizado.setIdTipoQuarto(tipo.getIdTipoQuarto());

            if (new QuartoController(atualizado).atualizar()) {
                mostrarSucesso("✔  Quarto atualizado com sucesso!");
                carregarQuartos();
            } else {
                mostrarErro("Falha ao salvar alterações.");
            }
        }
    }

    private void handleExcluir() {
        if (quartoSelecionado == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText(null);
        confirm.setContentText(
            "Excluir o Quarto " + quartoSelecionado.getNumeroQuarto() + " ("
            + quartoSelecionado.getNomeTipo() + ")?\n\nEsta ação não pode ser desfeita."
        );

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                QuartoController ctrl = new QuartoController();
                if (ctrl.deletar(quartoSelecionado.getCodQuarto())) {
                    carregarQuartos();
                } else if ("HAS_HISTORY".equals(ctrl.getUltimoErro())) {
                    mostrarErro("Não é possível excluir: este quarto possui histórico de reservas. "
                        + "Quartos com reservas registradas não podem ser removidos para preservar os dados financeiros do hotel.");
                } else {
                    mostrarErro("Erro inesperado ao excluir o quarto.");
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    // TRANSIÇÕES DE MODO
    // ─────────────────────────────────────────────────────────

    private void preencherFormParaEdicao(Quarto q) {
        quartoSelecionado = q;

        lblFormTitulo.setText("✏  Editar Quarto");

        tfNumQuarto.setText(String.valueOf(q.getNumeroQuarto()));

        // Seleciona o tipo correspondente no ComboBox
        for (TipoQuarto t : cbTipo.getItems()) {
            if (t.getIdTipoQuarto() == q.getIdTipoQuarto()) {
                cbTipo.setValue(t);
                break;
            }
        }

        btnPrimario.setText("Salvar Alterações");
        btnPrimario.setStyle(estiloBtnVerde());

        btnExcluir.setVisible(true);
        btnExcluir.setManaged(true);
        btnCancelar.setVisible(true);
        btnCancelar.setManaged(true);

        resetarFeedback();
    }

    private void resetarFormParaAdicao() {
        quartoSelecionado = null;
        tabela.getSelectionModel().clearSelection();

        lblFormTitulo.setText("➕  Novo Quarto");

        tfNumQuarto.clear();
        cbTipo.getSelectionModel().clearSelection();

        btnPrimario.setText("Cadastrar Quarto");
        btnPrimario.setStyle(estiloBtnAzul());

        btnExcluir.setVisible(false);
        btnExcluir.setManaged(false);
        btnCancelar.setVisible(false);
        btnCancelar.setManaged(false);

        resetarFeedback();
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

    private String estiloBtnAzul()          { return btn("#2980B9"); }
    private String estiloBtnAzulHover()     { return btn("#1F6691"); }
    private String estiloBtnVerde()         { return btn("#27AE60"); }
    private String estiloBtnVerdeHover()    { return btn("#1E8449"); }
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
