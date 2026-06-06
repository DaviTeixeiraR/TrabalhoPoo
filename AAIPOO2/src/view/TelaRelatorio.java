package view;

import controler.ReservaController;
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
import model.Reserva;
import model.ThemeManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tela de Relatório de Reservas.
 *
 * Oferece 3 filtros combinados:
 *  1. Status de Pagamento  — ComboBox (Todos / Pago / Pendente)
 *  2. Período de Check-in  — dois DatePickers (De / Até)
 *  3. Nome do Cliente      — campo de busca por texto
 *
 * Os filtros são aplicados automaticamente ao alterar qualquer controle.
 * Um sumário ao rodapé exibe totais com base nos registros filtrados.
 */
public class TelaRelatorio {

    private TableView<Reserva> tabela;
    private List<Reserva>      todasReservas;

    // Controles de filtro
    private ComboBox<String> cbStatus;
    private DatePicker       dpDe;
    private DatePicker       dpAte;
    private TextField        tfCpf;     // filtro por CPF (chave única)

    // Labels do sumário
    private Label lblTotalReservas;
    private Label lblTotalDiarias;
    private Label lblReceita;

    // ── Ponto de entrada ──────────────────────────────────────

    public Node getNode() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: " + ThemeManager.bg() + ";");

        root.getChildren().addAll(
            criarCabecalho(),
            criarPainelFiltros(),
            criarTabela(),
            criarSumario()
        );

        carregarDados();
        return root;
    }

    // ── Cabeçalho ─────────────────────────────────────────────

    private VBox criarCabecalho() {
        Label lblTitulo = new Label("📊  Relatório de Reservas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Label lblSub = new Label("Filtre e analise o histórico completo de hospedagens.");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web(ThemeManager.subtitleColor()));

        VBox box = new VBox(4, lblTitulo, lblSub);
        return box;
    }

    // ── Painel de Filtros ──────────────────────────────────────

    private VBox criarPainelFiltros() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(15, 20, 15, 20));
        painel.setStyle(ThemeManager.cardStyle());

        // Rótulo da seção
        Label lblSec = new Label("FILTROS");
        lblSec.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        lblSec.setTextFill(Color.web(ThemeManager.sidebarLabel()));

        // ── Filtro 1: Status de Pagamento ──
        VBox boxStatus = new VBox(5);
        Label lblSt = new Label("Status de Pagamento");
        lblSt.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblSt.setTextFill(Color.web(ThemeManager.labelColor()));
        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Todos", "Pago", "Pendente");
        cbStatus.setValue("Todos");
        cbStatus.setPrefWidth(175);
        ThemeManager.applyComboStyle(cbStatus);
        cbStatus.setOnAction(e -> aplicarFiltros());
        boxStatus.getChildren().addAll(lblSt, cbStatus);

        // ── Filtro 2: Período de Check-in (De) ──
        VBox boxDe = new VBox(5);
        Label lblDe = new Label("Check-in a partir de");
        lblDe.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblDe.setTextFill(Color.web(ThemeManager.labelColor()));
        dpDe = new DatePicker();
        dpDe.setPromptText("Data inicial");
        dpDe.setPrefWidth(160);
        ThemeManager.applyDatePickerStyle(dpDe);
        dpDe.setOnAction(e -> aplicarFiltros());
        boxDe.getChildren().addAll(lblDe, dpDe);

        // ── Filtro 2b: Período de Check-in (Até) ──
        VBox boxAte = new VBox(5);
        Label lblAte = new Label("Check-in até");
        lblAte.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblAte.setTextFill(Color.web(ThemeManager.labelColor()));
        dpAte = new DatePicker();
        dpAte.setPromptText("Data final");
        dpAte.setPrefWidth(160);
        ThemeManager.applyDatePickerStyle(dpAte);
        dpAte.setOnAction(e -> aplicarFiltros());
        boxAte.getChildren().addAll(lblAte, dpAte);

        // ── Filtro 3: CPF do Cliente ──
        VBox boxCliente = new VBox(5);
        Label lblCli = new Label("CPF do Cliente");
        lblCli.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblCli.setTextFill(Color.web(ThemeManager.labelColor()));
        tfCpf = new TextField();
        tfCpf.setPromptText("Digite o CPF...");
        tfCpf.setPrefWidth(190);
        tfCpf.setStyle(ThemeManager.fieldStyle());
        tfCpf.setOnKeyTyped(e -> aplicarFiltros());
        boxCliente.getChildren().addAll(lblCli, tfCpf);

        // ── Botão Limpar ──
        VBox boxBtn = new VBox(5);
        Label lblEsp = new Label(" "); // espaçador para alinhar verticalmente
        lblEsp.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        Button btnLimpar = new Button("✕  Limpar Filtros");
        btnLimpar.setFont(Font.font("Arial", 12));
        btnLimpar.setStyle(
            "-fx-background-color: " + ThemeManager.card() + ";"
          + "-fx-border-color: " + ThemeManager.fieldBorder() + ";"
          + "-fx-border-radius: 6; -fx-padding: 9 16; -fx-cursor: hand;"
          + "-fx-text-fill: " + ThemeManager.labelColor() + ";"
        );
        btnLimpar.setOnMouseEntered(e -> btnLimpar.setOpacity(0.8));
        btnLimpar.setOnMouseExited(e  -> btnLimpar.setOpacity(1.0));
        btnLimpar.setOnAction(e -> limparFiltros());
        boxBtn.getChildren().addAll(lblEsp, btnLimpar);

        HBox linha = new HBox(14, boxStatus, boxDe, boxAte, boxCliente, boxBtn);
        linha.setAlignment(Pos.BOTTOM_LEFT);

        painel.getChildren().addAll(lblSec, linha);
        return painel;
    }

    // ── Tabela ─────────────────────────────────────────────────

    private VBox criarTabela() {
        tabela = new TableView<>();
        tabela.setStyle(ThemeManager.tableStyle());
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        configurarColunas();

        VBox panel = new VBox(tabela);
        VBox.setVgrow(panel, Priority.ALWAYS);
        panel.setPadding(new Insets(8));
        panel.setStyle(
            "-fx-background-color: " + ThemeManager.card() + ";"
          + "-fx-background-radius: 8;"
          + "-fx-effect: dropshadow(gaussian, " + ThemeManager.cardShadow() + ", 10, 0, 0, 2);"
        );
        return panel;
    }

    @SuppressWarnings("unchecked")
    private void configurarColunas() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<Reserva, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colId.setCellFactory(col -> celulaInteiro());
        colId.setMinWidth(50); colId.setMaxWidth(60);

        TableColumn<Reserva, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colCliente.setCellFactory(col -> celulaTexto());
        colCliente.setMinWidth(180);

        TableColumn<Reserva, String> colQuarto = new TableColumn<>("Quarto");
        colQuarto.setCellValueFactory(new PropertyValueFactory<>("numeroQuarto"));
        colQuarto.setCellFactory(col -> celulaTexto());
        colQuarto.setMinWidth(70); colQuarto.setMaxWidth(90);

        TableColumn<Reserva, String> colCheckin = new TableColumn<>("Check-in");
        colCheckin.setCellValueFactory(cd -> {
            LocalDate d = cd.getValue().getDataCheckin();
            return new SimpleStringProperty(d != null ? d.format(fmt) : "");
        });
        colCheckin.setCellFactory(col -> celulaTexto());
        colCheckin.setMinWidth(100); colCheckin.setMaxWidth(120);

        TableColumn<Reserva, String> colCheckout = new TableColumn<>("Check-out");
        colCheckout.setCellValueFactory(cd -> {
            LocalDate d = cd.getValue().getDataCheckout();
            return new SimpleStringProperty(d != null ? d.format(fmt) : "");
        });
        colCheckout.setCellFactory(col -> celulaTexto());
        colCheckout.setMinWidth(100); colCheckout.setMaxWidth(120);

        TableColumn<Reserva, Long> colDias = new TableColumn<>("Diárias");
        colDias.setCellValueFactory(new PropertyValueFactory<>("diarias"));
        colDias.setCellFactory(col -> celulaLong());
        colDias.setMinWidth(65); colDias.setMaxWidth(80);

        TableColumn<Reserva, String> colValor = new TableColumn<>("Valor Total");
        colValor.setCellValueFactory(cd ->
            new SimpleStringProperty(String.format("R$ %,.2f", cd.getValue().getValorTotal()))
        );
        colValor.setCellFactory(col -> celulaTexto());
        colValor.setMinWidth(110);

        // Forma de pagamento — "—" quando reserva ainda está pendente
        TableColumn<Reserva, String> colForma = new TableColumn<>("Forma Pgto.");
        colForma.setCellValueFactory(cd -> {
            String forma = cd.getValue().getFormaPagamento();
            return new SimpleStringProperty(forma != null ? forma : "—");
        });
        colForma.setMinWidth(115);
        colForma.setCellFactory(col -> new TableCell<Reserva, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); return; }
                setText(item);
                // Pendente aparece em tom discreto; forma de pagamento em cor normal
                if ("—".equals(item)) {
                    setTextFill(Color.web(ThemeManager.mutedText()));
                    setStyle("-fx-font-style: italic;");
                } else {
                    setTextFill(ThemeManager.tableCellTextColor());
                    setStyle("");
                }
            }
        });

        // Status tem cores semânticas próprias (verde/vermelho)
        TableColumn<Reserva, String> colStatus = new TableColumn<>("Pagamento");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("nomeStatus"));
        colStatus.setMinWidth(95); colStatus.setMaxWidth(115);
        colStatus.setCellFactory(col -> new TableCell<Reserva, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(item);
                setStyle("-fx-font-weight: bold;");
                setTextFill("Pago".equals(item)
                    ? Color.web("#27AE60")
                    : Color.web("#E74C3C"));
            }
        });

        tabela.getColumns().addAll(
            colId, colCliente, colQuarto,
            colCheckin, colCheckout, colDias, colValor, colForma, colStatus
        );
    }

    /** Célula de texto com cor definida pelo tema atual. */
    private TableCell<Reserva, String> celulaTexto() {
        return new TableCell<Reserva, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(ThemeManager.tableCellTextColor());
            }
        };
    }

    private TableCell<Reserva, Integer> celulaInteiro() {
        return new TableCell<Reserva, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setTextFill(ThemeManager.tableCellTextColor());
            }
        };
    }

    private TableCell<Reserva, Long> celulaLong() {
        return new TableCell<Reserva, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setTextFill(ThemeManager.tableCellTextColor());
            }
        };
    }

    // ── Sumário ────────────────────────────────────────────────

    private HBox criarSumario() {
        HBox sumario = new HBox();
        sumario.setAlignment(Pos.CENTER);
        sumario.setPadding(new Insets(12, 24, 12, 24));
        sumario.setSpacing(0);
        sumario.setStyle(
            "-fx-background-color: " + ThemeManager.card() + ";"
          + "-fx-background-radius: 8;"
          + "-fx-effect: dropshadow(gaussian, " + ThemeManager.cardShadow() + ", 6, 0, 0, 2);"
        );

        lblTotalReservas = criarItemSumario("Reservas: —", false);
        lblTotalDiarias  = criarItemSumario("Total de Diárias: —", false);
        lblReceita       = criarItemSumario("Receita Total: —", true);

        sumario.getChildren().addAll(
            lblTotalReservas,
            criarSep(),
            lblTotalDiarias,
            criarSep(),
            lblReceita
        );
        return sumario;
    }

    private Label criarItemSumario(String texto, boolean destaque) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, destaque ? 14 : 13));
        lbl.setTextFill(Color.web(destaque ? "#27AE60" : ThemeManager.titleColor()));
        lbl.setPadding(new Insets(0, 28, 0, 28));
        return lbl;
    }

    private Label criarSep() {
        Label sep = new Label("|");
        sep.setFont(Font.font("Arial", 16));
        sep.setTextFill(Color.web(ThemeManager.fieldBorder()));
        return sep;
    }

    // ── Lógica ─────────────────────────────────────────────────

    private void carregarDados() {
        ReservaController ctrl = new ReservaController();
        todasReservas = ctrl.listarTodasReservas();
        tabela.setItems(FXCollections.observableArrayList(todasReservas));
        atualizarSumario(todasReservas);
    }

    /**
     * Aplica os três filtros em combinação (lógica AND).
     * Filtro 1 — Status: "Todos" desativa o filtro; "Pago"/"Pendente" filtram por nomeStatus.
     * Filtro 2 — Período: filtra por data de check-in dentro do intervalo [De, Até].
     * Filtro 3 — CPF: busca parcial nos dígitos do CPF (chave única do cliente).
     */
    private void aplicarFiltros() {
        if (todasReservas == null) return;

        String   statusFiltro = cbStatus.getValue();
        LocalDate de          = dpDe.getValue();
        LocalDate ate         = dpAte.getValue();
        // Remove formatação para comparar apenas os dígitos digitados
        String   cpfFiltro    = tfCpf.getText().trim().replaceAll("[^0-9]", "");

        List<Reserva> filtradas = todasReservas.stream()
            .filter(r -> {
                // Filtro 1: status de pagamento
                if (!"Todos".equals(statusFiltro)) {
                    String st = r.getNomeStatus();
                    if (st == null || !st.equals(statusFiltro)) return false;
                }
                // Filtro 2: período de check-in
                LocalDate ci = r.getDataCheckin();
                if (de  != null && (ci == null || ci.isBefore(de)))  return false;
                if (ate != null && (ci == null || ci.isAfter(ate)))   return false;
                // Filtro 3: CPF do cliente (comparação por dígitos, busca parcial)
                if (!cpfFiltro.isEmpty()) {
                    String cpf = r.getCpf();
                    if (cpf == null) return false;
                    String digitos = cpf.replaceAll("[^0-9]", "");
                    if (!digitos.contains(cpfFiltro)) return false;
                }
                return true;
            })
            .collect(Collectors.toList());

        tabela.setItems(FXCollections.observableArrayList(filtradas));
        atualizarSumario(filtradas);
    }

    /** Remove todos os filtros e restaura a lista completa. */
    private void limparFiltros() {
        cbStatus.setValue("Todos");
        dpDe.setValue(null);
        dpAte.setValue(null);
        tfCpf.clear();
        if (todasReservas != null) {
            tabela.setItems(FXCollections.observableArrayList(todasReservas));
            atualizarSumario(todasReservas);
        }
    }

    /** Atualiza os três labels do rodapé com base na lista atualmente exibida. */
    private void atualizarSumario(List<Reserva> lista) {
        int    total         = lista.size();
        long   totalDiarias  = lista.stream().mapToLong(Reserva::getDiarias).sum();
        double receita       = lista.stream().mapToDouble(Reserva::getValorTotal).sum();

        lblTotalReservas.setText("Reservas: " + total);
        lblTotalDiarias.setText("Total de Diárias: " + totalDiarias);
        lblReceita.setText(String.format("Receita Total: R$ %,.2f", receita));
    }
}
