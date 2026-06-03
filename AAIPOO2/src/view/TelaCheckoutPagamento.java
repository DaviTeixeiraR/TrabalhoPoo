package view;

import controler.PagamentoController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.FormaPagamento;
import model.Pagamento;
import model.Reserva;

import java.time.LocalDate;
import java.util.List;

/**
 * Tela de Pagamento do Checkout.
 * Exibe resumo da reserva, seleção da forma de pagamento e efetiva a baixa.
 */
public class TelaCheckoutPagamento {

    private Reserva reserva;
    private ComboBox<FormaPagamento> cbFormas;
    private Label lblErro;

    public TelaCheckoutPagamento(Reserva reserva) {
        this.reserva = reserva;
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

        // Título e Botão Voltar
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button btnVoltar = new Button("⬅ Voltar");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand;");
        btnVoltar.setOnAction(e -> Principal.navegarPara("checkout"));
        
        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);
        
        Label lblTitulo = new Label("💳  Pagamento");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));
        
        header.getChildren().addAll(btnVoltar, spacerH, lblTitulo);
        
        Separator sep1 = new Separator();

        // ── Resumo da Reserva ──
        VBox resumo = new VBox(12);
        resumo.setPadding(new Insets(20));
        resumo.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #EAECEE; -fx-border-radius: 8;");
        
        Label lblResumoTit = new Label("Resumo da Hospedagem");
        lblResumoTit.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblResumoTit.setTextFill(Color.web("#34495E"));
        
        resumo.getChildren().addAll(
            lblResumoTit,
            linhaResumo("Cliente:", reserva.getNomeCliente()),
            linhaResumo("Quarto:", reserva.getNumeroQuarto() + " (" + String.format("R$ %.2f/dia", reserva.getPrecoBase()) + ")"),
            linhaResumo("Período:", reserva.getDataCheckin() + " a " + reserva.getDataCheckout()),
            linhaResumo("Diárias:", String.valueOf(reserva.getDiarias()))
        );

        // Valor a pagar
        HBox boxValor = new HBox();
        boxValor.setAlignment(Pos.CENTER);
        boxValor.setPadding(new Insets(10, 0, 10, 0));
        
        Label lblValor = new Label(String.format("TOTAL: R$ %,.2f", reserva.getValorTotal()));
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        lblValor.setTextFill(Color.web("#27AE60"));
        boxValor.getChildren().add(lblValor);

        Separator sep2 = new Separator();

        // ── Seleção de Pagamento ──
        VBox secPagamento = new VBox(10);
        Label lblForma = new Label("Forma de Pagamento:");
        lblForma.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        cbFormas = new ComboBox<>();
        cbFormas.setPrefWidth(Double.MAX_VALUE);
        cbFormas.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        
        // Carrega as formas do banco
        PagamentoController ctrlPag = new PagamentoController();
        List<FormaPagamento> formas = ctrlPag.listarFormasPagamento();
        cbFormas.getItems().addAll(formas);
        if (!formas.isEmpty()) cbFormas.getSelectionModel().selectFirst();
        
        secPagamento.getChildren().addAll(lblForma, cbFormas);
        
        // Label de erro
        lblErro = new Label();
        lblErro.setTextFill(Color.RED);
        lblErro.setFont(Font.font(13));

        // ── Ações ──
        Button btnConfirmar = new Button("Confirmar Pagamento e Liberar Quarto");
        btnConfirmar.setPrefWidth(Double.MAX_VALUE);
        btnConfirmar.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btnConfirmar.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;");
        btnConfirmar.setOnAction(e -> processarPagamento());

        card.getChildren().addAll(
            header, sep1, 
            resumo, boxValor, sep2, 
            secPagamento, lblErro, btnConfirmar
        );

        root.getChildren().add(card);
        return root;
    }
    
    private HBox linhaResumo(String label, String valor) {
        HBox linha = new HBox(10);
        Label l1 = new Label(label);
        l1.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l1.setTextFill(Color.web("#7F8C8D"));
        l1.setPrefWidth(80);
        
        Label l2 = new Label(valor);
        l2.setFont(Font.font("Arial", 14));
        l2.setTextFill(Color.web("#2C3E50"));
        
        linha.getChildren().addAll(l1, l2);
        return linha;
    }
    
    private void processarPagamento() {
        lblErro.setText("");
        FormaPagamento forma = cbFormas.getValue();
        
        if (forma == null) {
            lblErro.setText("Selecione a forma de pagamento.");
            return;
        }
        
        // Criação do model Pagamento
        Pagamento pagamento = new Pagamento(
            reserva.getIdReserva(), 
            forma.getIdFormaPagamento(), 
            reserva.getValorTotal(), 
            LocalDate.now()
        );
        
        PagamentoController ctrl = new PagamentoController(pagamento);
        
        // Tenta registrar (também libera o quarto internamente)
        if (ctrl.registrarPagamento(reserva.getCodQuarto())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Pagamento confirmado! Quarto " + reserva.getNumeroQuarto() + " foi liberado (Disponível).");
            alert.showAndWait();
            
            // Volta para a tela de checkout (a lista será atualizada) ou dashboard
            Principal.navegarPara("dashboard");
        } else {
            lblErro.setText("Falha ao registrar pagamento.");
        }
    }
}
