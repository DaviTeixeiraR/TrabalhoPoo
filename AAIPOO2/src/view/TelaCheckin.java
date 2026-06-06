package view;

import controler.ReservaController;
import dao.DadosPesDao;
import dao.QuartoDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Conexao;
import model.DadosPessoais;
import model.Quarto;
import model.Reserva;
import model.ThemeManager;
import util.Validador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Tela de Check-in (Nova Reserva).
 * Permite buscar o cliente por CPF, selecionar quarto e definir datas.
 */
public class TelaCheckin {

    private TextField tfCpf;
    private Label     lblNomeCliente;
    private ComboBox<Quarto> cbQuartos;
    private DatePicker dpCheckin;
    private DatePicker dpCheckout;
    private Label     lblValorTotal;
    private Label     lblErro;

    private Quarto quartoPreSelecionado;

    public TelaCheckin() {}

    public TelaCheckin(Quarto quartoSelecionado) {
        this.quartoPreSelecionado = quartoSelecionado;
    }

    public Node getNode() {
        ScrollPane scroll = new ScrollPane(criarConteudo());
        scroll.setFitToWidth(true);
        scroll.setStyle(ThemeManager.scrollStyle());
        return scroll;
    }

    private VBox criarConteudo() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + ThemeManager.bg() + ";");

        // ── Card Central ──
        VBox card = new VBox(20);
        card.setPadding(new Insets(35, 40, 35, 40));
        card.setMaxWidth(600);
        card.setStyle(ThemeManager.cardStyle());

        // ── Cabeçalho ──
        Label lblTitulo = new Label("📋  Novo Check-in");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Label lblSub = new Label("Registre a entrada de um cliente associando a um quarto disponível.");
        lblSub.setFont(Font.font("Arial", 13));
        lblSub.setTextFill(Color.web(ThemeManager.subtitleColor()));
        lblSub.setWrapText(true);

        Separator sep = new Separator();

        // ── Seção Cliente ──
        VBox secCliente = new VBox(10);
        Label lblCpf = new Label("CPF do Cliente:");
        lblCpf.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblCpf.setTextFill(Color.web(ThemeManager.labelColor()));

        HBox boxBuscaCpf = new HBox(10);
        tfCpf = new TextField();
        tfCpf.setPromptText("Digite apenas números");
        tfCpf.setPrefWidth(200);
        tfCpf.setFont(Font.font(14));
        tfCpf.setStyle(ThemeManager.fieldStyle());

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnBuscar.setStyle(
            "-fx-background-color: #34495E; -fx-text-fill: white;"
          + "-fx-background-radius: 6; -fx-padding: 10 18; -fx-cursor: hand;"
        );
        btnBuscar.setOnAction(e -> buscarCliente());
        boxBuscaCpf.getChildren().addAll(tfCpf, btnBuscar);

        lblNomeCliente = new Label("Cliente não selecionado");
        lblNomeCliente.setTextFill(Color.web("#E74C3C"));
        lblNomeCliente.setFont(Font.font("Arial", 12));
        secCliente.getChildren().addAll(lblCpf, boxBuscaCpf, lblNomeCliente);

        // ── Seção Quarto ──
        VBox secQuarto = new VBox(10);
        Label lblQuarto = new Label("Quarto Disponível:");
        lblQuarto.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblQuarto.setTextFill(Color.web(ThemeManager.labelColor()));

        cbQuartos = new ComboBox<>();
        cbQuartos.setPrefWidth(Double.MAX_VALUE);
        ThemeManager.applyComboStyle(cbQuartos);
        carregarQuartos();
        cbQuartos.setOnAction(e -> calcularValorTotal());
        secQuarto.getChildren().addAll(lblQuarto, cbQuartos);

        // ── Seção Datas ──
        HBox secDatas = new HBox(20);

        VBox boxCheckin = new VBox(10);
        Label lblCheckin = new Label("Check-in:");
        lblCheckin.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblCheckin.setTextFill(Color.web(ThemeManager.labelColor()));
        dpCheckin = new DatePicker(LocalDate.now());
        dpCheckin.setPrefWidth(250);
        ThemeManager.applyDatePickerStyle(dpCheckin);
        dpCheckin.setOnAction(e -> { recarregarQuartos(); calcularValorTotal(); });
        boxCheckin.getChildren().addAll(lblCheckin, dpCheckin);

        VBox boxCheckout = new VBox(10);
        Label lblCheckout = new Label("Check-out Previsto:");
        lblCheckout.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblCheckout.setTextFill(Color.web(ThemeManager.labelColor()));
        dpCheckout = new DatePicker(LocalDate.now().plusDays(1));
        dpCheckout.setPrefWidth(250);
        ThemeManager.applyDatePickerStyle(dpCheckout);
        dpCheckout.setOnAction(e -> { recarregarQuartos(); calcularValorTotal(); });
        boxCheckout.getChildren().addAll(lblCheckout, dpCheckout);
        secDatas.getChildren().addAll(boxCheckin, boxCheckout);

        // ── Resumo de Valores ──
        HBox secValor = new HBox();
        secValor.setAlignment(Pos.CENTER_RIGHT);
        secValor.setPadding(new Insets(10, 0, 5, 0));

        Label lblTitValor = new Label("Valor Total Previsto: ");
        lblTitValor.setFont(Font.font("Arial", 14));
        lblTitValor.setTextFill(Color.web(ThemeManager.subtitleColor()));

        lblValorTotal = new Label("R$ 0,00");
        lblValorTotal.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblValorTotal.setTextFill(Color.web("#27AE60"));
        secValor.getChildren().addAll(lblTitValor, lblValorTotal);

        // ── Feedback ──
        lblErro = new Label();
        lblErro.setFont(Font.font("Arial", 12));
        lblErro.setWrapText(true);

        // ── Botão Confirmar ──
        Button btnSalvar = new Button("Confirmar Check-in");
        btnSalvar.setPrefWidth(Double.MAX_VALUE);
        btnSalvar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnSalvar.setStyle(
            "-fx-background-color: #27AE60; -fx-text-fill: white;"
          + "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnSalvar.setOnMouseEntered(e -> btnSalvar.setOpacity(0.85));
        btnSalvar.setOnMouseExited(e  -> btnSalvar.setOpacity(1.0));
        btnSalvar.setOnAction(e -> confirmarCheckin());

        calcularValorTotal();

        card.getChildren().addAll(
            lblTitulo, lblSub, sep,
            secCliente, secQuarto, secDatas,
            secValor, lblErro, btnSalvar
        );

        root.getChildren().add(card);
        return root;
    }

    // ── Lógica ──────────────────────────────────────────────────

    private void buscarCliente() {
        String cpf = tfCpf.getText().trim();
        if (cpf.isEmpty()) {
            lblNomeCliente.setText("⚠  Digite um CPF.");
            lblNomeCliente.setTextFill(Color.web("#E74C3C"));
            return;
        }
        try {
            Conexao.conectar();
            DadosPesDao dao = new DadosPesDao(Conexao.conexao);
            DadosPessoais cliente = dao.buscarPorCpf(cpf);
            if (cliente != null) {
                lblNomeCliente.setText("✔  Cliente: " + cliente.getNomeCliente());
                lblNomeCliente.setTextFill(Color.web("#27AE60"));
            } else {
                lblNomeCliente.setText("✖  Cliente não encontrado. Faça o cadastro primeiro.");
                lblNomeCliente.setTextFill(Color.web("#E74C3C"));
            }
        } catch (SQLException e) {
            lblNomeCliente.setText("Erro no BD: " + e.getMessage());
            lblNomeCliente.setTextFill(Color.web("#E74C3C"));
        } finally {
            Conexao.desconectar();
        }
    }

    private void carregarQuartos() {
        recarregarQuartos();
    }

    private void recarregarQuartos() {
        LocalDate checkin  = dpCheckin  != null ? dpCheckin.getValue()  : LocalDate.now();
        LocalDate checkout = dpCheckout != null ? dpCheckout.getValue() : LocalDate.now().plusDays(1);
        if (checkin == null)  checkin  = LocalDate.now();
        if (checkout == null) checkout = checkin.plusDays(1);

        Quarto selecionadoAtual = cbQuartos != null ? cbQuartos.getValue() : null;
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            List<Quarto> disponiveis = dao.listarDisponiveisParaPeriodo(checkin, checkout);
            cbQuartos.getItems().setAll(disponiveis);

            // Reaplica seleção anterior se ainda disponível no novo período
            if (selecionadoAtual != null) {
                for (Quarto q : cbQuartos.getItems()) {
                    if (q.getCodQuarto() == selecionadoAtual.getCodQuarto()) {
                        cbQuartos.setValue(q);
                        break;
                    }
                }
            } else if (quartoPreSelecionado != null) {
                for (Quarto q : cbQuartos.getItems()) {
                    if (q.getCodQuarto() == quartoPreSelecionado.getCodQuarto()) {
                        cbQuartos.setValue(q);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            if (lblErro != null) lblErro.setText("Erro ao carregar quartos.");
        } finally {
            Conexao.desconectar();
        }
    }

    private void calcularValorTotal() {
        Quarto quarto = cbQuartos.getValue();
        LocalDate in  = dpCheckin.getValue();
        LocalDate out = dpCheckout.getValue();
        if (quarto != null && in != null && out != null) {
            long dias = ChronoUnit.DAYS.between(in, out);
            if (dias <= 0) dias = 1;
            lblValorTotal.setText(String.format("R$ %,.2f", dias * quarto.getPrecoBase()));
        } else {
            lblValorTotal.setText("R$ 0,00");
        }
    }

    private void confirmarCheckin() {
        lblErro.setText("");
        String cpf    = tfCpf.getText().trim();
        Quarto quarto = cbQuartos.getValue();
        LocalDate in  = dpCheckin.getValue();
        LocalDate out = dpCheckout.getValue();

        // ── Validações ────────────────────────────────────────────
        if (cpf.isEmpty() || !lblNomeCliente.getText().startsWith("✔")) {
            mostrarErro("Busque e confirme um cliente válido antes de prosseguir.");
            return;
        }
        if (quarto == null) {
            mostrarErro("Selecione um quarto disponível.");
            return;
        }

        String erroCheckin = Validador.validarCheckin(in);
        if (erroCheckin != null) { mostrarErro(erroCheckin); return; }

        String erroCheckout = Validador.validarCheckout(in, out);
        if (erroCheckout != null) { mostrarErro(erroCheckout); return; }

        ReservaController ctrl = new ReservaController();

        if (ctrl.existeReservaDuplicada(cpf, quarto.getCodQuarto(), in, out)) {
            mostrarErro("Este cliente já possui uma reserva idêntica para este quarto e período.");
            return;
        }

        if (ctrl.existeConflito(quarto.getCodQuarto(), in, out)) {
            mostrarErro("Este quarto já está reservado para o período selecionado. Escolha outro quarto ou período.");
            recarregarQuartos();
            return;
        }

        Reserva reserva = new Reserva(cpf, quarto.getCodQuarto(), in, out);
        ctrl = new ReservaController(reserva);

        if (ctrl.salvarReserva()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Check-in realizado com sucesso! Quarto marcado como Ocupado.");
            alert.showAndWait();
            Principal.navegarPara("dashboard");
        } else {
            mostrarErro("Falha ao registrar check-in.");
        }
    }

    private void mostrarErro(String msg) {
        lblErro.setTextFill(Color.web("#E74C3C"));
        lblErro.setText("⚠  " + msg);
    }
}
