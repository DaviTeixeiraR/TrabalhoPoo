package view;

import controler.ReservaController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Reserva;
import model.ThemeManager;

import java.util.List;

/**
 * Tela de Checkout.
 * Lista as reservas ativas (sem pagamento) para que o recepcionista
 * selecione e vá para o pagamento.
 */
public class TelaCheckout {

    private ListView<Reserva> listaReservas;

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
        card.setMaxWidth(700);
        card.setStyle(ThemeManager.cardStyle());

        // ── Cabeçalho ──
        Label lblTitulo = new Label("🚪  Realizar Checkout");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Label lblSub = new Label("Selecione uma reserva ativa para prosseguir com o pagamento e liberar o quarto.");
        lblSub.setFont(Font.font("Arial", 13));
        lblSub.setTextFill(Color.web(ThemeManager.subtitleColor()));
        lblSub.setWrapText(true);

        Separator sep = new Separator();

        // ── Lista de Reservas Ativas ──
        listaReservas = new ListView<>();
        listaReservas.setPrefHeight(300);
        listaReservas.setStyle(ThemeManager.listStyle());
        carregarReservas();

        listaReservas.setCellFactory(param -> new ListCell<Reserva>() {
            @Override
            protected void updateItem(Reserva r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: " + ThemeManager.fieldBg() + ";");
                } else {
                    HBox box = new HBox(15);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setPadding(new Insets(6));

                    Label icon = new Label("🔑");
                    icon.setFont(Font.font(18));

                    VBox texts = new VBox(3);
                    Label lbl1 = new Label("Quarto " + r.getNumeroQuarto() + " | Cliente: " + r.getNomeCliente());
                    lbl1.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    lbl1.setTextFill(Color.web(ThemeManager.textColor()));

                    Label lbl2 = new Label(r.getDataCheckin() + " até " + r.getDataCheckout()
                        + " (" + r.getDiarias() + " diárias)");
                    lbl2.setFont(Font.font("Arial", 12));
                    lbl2.setTextFill(Color.web(ThemeManager.mutedText()));

                    texts.getChildren().addAll(lbl1, lbl2);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label lblValor = new Label(String.format("R$ %,.2f", r.getValorTotal()));
                    lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                    lblValor.setTextFill(Color.web("#27AE60"));

                    box.getChildren().addAll(icon, texts, spacer, lblValor);
                    setGraphic(box);
                    setStyle("-fx-background-color: " + ThemeManager.fieldBg() + ";");
                }
            }
        });

        // ── Ações ──
        HBox boxAcoes = new HBox(15);
        boxAcoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnAtualizar = new Button("↻ Atualizar");
        btnAtualizar.setStyle(
            "-fx-background-color: transparent;"
          + "-fx-border-color: " + ThemeManager.fieldBorder() + ";"
          + "-fx-border-radius: 4; -fx-cursor: hand;"
          + "-fx-text-fill: " + ThemeManager.labelColor() + ";"
        );
        btnAtualizar.setOnAction(e -> carregarReservas());

        Button btnAvancar = new Button("Ir para Pagamento ➜");
        btnAvancar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnAvancar.setStyle(
            "-fx-background-color: #2980B9; -fx-text-fill: white;"
          + "-fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"
        );
        btnAvancar.setOnMouseEntered(e -> btnAvancar.setOpacity(0.85));
        btnAvancar.setOnMouseExited(e  -> btnAvancar.setOpacity(1.0));
        btnAvancar.setOnAction(e -> prosseguirCheckout());

        boxAcoes.getChildren().addAll(btnAtualizar, btnAvancar);

        card.getChildren().addAll(lblTitulo, lblSub, sep, listaReservas, boxAcoes);
        root.getChildren().add(card);
        return root;
    }

    private void carregarReservas() {
        ReservaController ctrl = new ReservaController();
        List<Reserva> ativas = ctrl.listarReservasAtivas();
        listaReservas.getItems().clear();
        listaReservas.getItems().addAll(ativas);
    }

    private void prosseguirCheckout() {
        Reserva selecionada = listaReservas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            Principal.navegarParaPagamento(selecionada);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Selecione uma reserva na lista para prosseguir.");
            alert.show();
        }
    }
}
