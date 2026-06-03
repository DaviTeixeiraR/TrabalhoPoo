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
    
    // Construtor padrão
    public TelaCheckin() {}
    
    // Construtor quando vem da tela de Seleção de Quartos
    public TelaCheckin(Quarto quartoSelecionado) {
        this.quartoPreSelecionado = quartoSelecionado;
    }

    public Node getNode() {
        ScrollPane scroll = new ScrollPane(criarConteudo());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F4F8; -fx-background: #F0F4F8;");
        return scroll;
    }

    private VBox criarConteudo() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #F0F4F8;");

        // ── Card Central ──
        VBox card = new VBox(20);
        card.setPadding(new Insets(35, 40, 35, 40));
        card.setMaxWidth(600);
        card.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 12;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);"
        );

        // Título
        Label lblTitulo = new Label("📋  Novo Check-in");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));
        
        Label lblSub = new Label("Registre a entrada de um cliente associando a um quarto disponível.");
        lblSub.setFont(Font.font("Arial", 13));
        lblSub.setTextFill(Color.web("#666"));

        Separator sep = new Separator();

        // ── Seção Cliente (Busca por CPF) ──
        VBox secCliente = new VBox(10);
        Label lblCpf = new Label("CPF do Cliente:");
        lblCpf.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        HBox boxBuscaCpf = new HBox(10);
        tfCpf = new TextField();
        tfCpf.setPromptText("Digite apenas números");
        tfCpf.setPrefWidth(200);
        tfCpf.setStyle(estiloField());
        
        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-cursor: hand;");
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
        
        cbQuartos = new ComboBox<>();
        cbQuartos.setPrefWidth(Double.MAX_VALUE);
        cbQuartos.setStyle(estiloField());
        carregarQuartos();
        
        if (quartoPreSelecionado != null) {
            // Seleciona o quarto pré-selecionado no combobox
            for (Quarto q : cbQuartos.getItems()) {
                if (q.getCodQuarto().equals(quartoPreSelecionado.getCodQuarto())) {
                    cbQuartos.setValue(q);
                    break;
                }
            }
        }
        cbQuartos.setOnAction(e -> calcularValorTotal());
        secQuarto.getChildren().addAll(lblQuarto, cbQuartos);

        // ── Seção Datas ──
        HBox secDatas = new HBox(20);
        
        VBox boxCheckin = new VBox(10);
        Label lblCheckin = new Label("Check-in:");
        lblCheckin.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dpCheckin = new DatePicker(LocalDate.now()); // Hoje por padrão
        dpCheckin.setPrefWidth(250);
        dpCheckin.setStyle(estiloField());
        dpCheckin.setOnAction(e -> calcularValorTotal());
        boxCheckin.getChildren().addAll(lblCheckin, dpCheckin);
        
        VBox boxCheckout = new VBox(10);
        Label lblCheckout = new Label("Check-out Previsto:");
        lblCheckout.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dpCheckout = new DatePicker(LocalDate.now().plusDays(1)); // +1 dia por padrão
        dpCheckout.setPrefWidth(250);
        dpCheckout.setStyle(estiloField());
        dpCheckout.setOnAction(e -> calcularValorTotal());
        boxCheckout.getChildren().addAll(lblCheckout, dpCheckout);
        
        secDatas.getChildren().addAll(boxCheckin, boxCheckout);

        // ── Resumo de Valores ──
        HBox secValor = new HBox();
        secValor.setAlignment(Pos.CENTER_RIGHT);
        secValor.setPadding(new Insets(15, 0, 10, 0));
        
        Label lblTitValor = new Label("Valor Total Previsto: ");
        lblTitValor.setFont(Font.font("Arial", 14));
        
        lblValorTotal = new Label("R$ 0,00");
        lblValorTotal.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblValorTotal.setTextFill(Color.web("#27AE60"));
        
        secValor.getChildren().addAll(lblTitValor, lblValorTotal);
        
        // Label de erro
        lblErro = new Label();
        lblErro.setTextFill(Color.RED);
        lblErro.setFont(Font.font(12));
        
        // Botão Salvar
        Button btnSalvar = new Button("Confirmar Check-in");
        btnSalvar.setPrefWidth(Double.MAX_VALUE);
        btnSalvar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnSalvar.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSalvar.setOnAction(e -> confirmarCheckin());
        
        // Chama calcular para iniciar com o valor padrão de 1 dia
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
            lblNomeCliente.setText("Digite um CPF.");
            lblNomeCliente.setTextFill(Color.RED);
            return;
        }
        
        try {
            Conexao.conectar();
            DadosPesDao dao = new DadosPesDao(Conexao.conexao);
            DadosPessoais cliente = dao.buscarPorCpf(cpf);
            
            if (cliente != null) {
                lblNomeCliente.setText("✔ Cliente: " + cliente.getNomeCliente());
                lblNomeCliente.setTextFill(Color.web("#27AE60"));
            } else {
                lblNomeCliente.setText("✖ Cliente não encontrado. Faça o cadastro primeiro.");
                lblNomeCliente.setTextFill(Color.RED);
            }
        } catch (SQLException e) {
            lblNomeCliente.setText("Erro no BD: " + e.getMessage());
            lblNomeCliente.setTextFill(Color.RED);
        } finally {
            Conexao.desconectar();
        }
    }
    
    private void carregarQuartos() {
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            List<Quarto> disponiveis = dao.listarDisponiveis();
            cbQuartos.getItems().addAll(disponiveis);
        } catch (SQLException e) {
            lblErro.setText("Erro ao carregar quartos.");
        } finally {
            Conexao.desconectar();
        }
    }
    
    private void calcularValorTotal() {
        Quarto quarto = cbQuartos.getValue();
        LocalDate in = dpCheckin.getValue();
        LocalDate out = dpCheckout.getValue();
        
        if (quarto != null && in != null && out != null) {
            long dias = ChronoUnit.DAYS.between(in, out);
            if (dias <= 0) dias = 1; // Mínimo 1 diária
            
            double total = dias * quarto.getPrecoBase();
            lblValorTotal.setText(String.format("R$ %,.2f", total));
        } else {
            lblValorTotal.setText("R$ 0,00");
        }
    }
    
    private void confirmarCheckin() {
        lblErro.setText("");
        
        String cpf = tfCpf.getText().trim();
        Quarto quarto = cbQuartos.getValue();
        LocalDate in = dpCheckin.getValue();
        LocalDate out = dpCheckout.getValue();
        
        // Validações
        if (cpf.isEmpty() || !lblNomeCliente.getText().startsWith("✔")) {
            lblErro.setText("Selecione um cliente válido.");
            return;
        }
        if (quarto == null) {
            lblErro.setText("Selecione um quarto.");
            return;
        }
        if (in == null || out == null || !out.isAfter(in)) {
            lblErro.setText("Data de check-out deve ser maior que check-in.");
            return;
        }
        
        // Criar reserva
        Reserva reserva = new Reserva(cpf, quarto.getCodQuarto(), in, out);
        ReservaController ctrl = new ReservaController(reserva);
        
        if (ctrl.salvarReserva()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Check-in realizado com sucesso! Quarto marcado como Ocupado.");
            alert.showAndWait();
            
            // Navega para dashboard
            Principal.navegarPara("dashboard");
        } else {
            lblErro.setText("Falha ao registrar check-in.");
        }
    }

    private String estiloField() {
        return "-fx-background-color: #F8F9FA; -fx-border-color: #DDE3EC; -fx-border-radius: 4; -fx-padding: 8;";
    }
}
