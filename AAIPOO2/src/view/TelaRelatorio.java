package view;

import controler.ReservaController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Relatório.
 * Exibe todas as reservas do hotel, incluindo as já pagas (histórico),
 * utilizando um TableView para melhor visualização dos dados.
 */
public class TelaRelatorio {

    private TableView<Reserva> tabela;

    public Node getNode() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: #F0F4F8;");

        // ── Cabeçalho ──
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titles = new VBox(4);
        Label lblTitulo = new Label("📊  Relatório de Reservas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));
        
        Label lblSub = new Label("Histórico completo de hospedagens e status de pagamento.");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web("#666"));
        titles.getChildren().addAll(lblTitulo, lblSub);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnAtualizar = new Button("↻ Atualizar Dados");
        btnAtualizar.setStyle("-fx-background-color: white; -fx-border-color: #BDC3C7; -fx-border-radius: 4; -fx-cursor: hand;");
        btnAtualizar.setOnAction(e -> carregarDados());
        
        header.getChildren().addAll(titles, spacer, btnAtualizar);

        // ── Tabela ──
        tabela = new TableView<>();
        tabela.setStyle("-fx-font-family: Arial; -fx-font-size: 13px;");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        
        configurarColunas();
        carregarDados();
        
        // Estilo de painel para a tabela
        VBox panel = new VBox(tabela);
        VBox.setVgrow(panel, Priority.ALWAYS);
        panel.setPadding(new Insets(10));
        panel.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 8;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );

        root.getChildren().addAll(header, panel);
        return root;
    }

    @SuppressWarnings("unchecked")
    private void configurarColunas() {
        // ID
        TableColumn<Reserva, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colId.setPrefWidth(50);

        // Cliente
        TableColumn<Reserva, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colCliente.setPrefWidth(200);

        // Quarto
        TableColumn<Reserva, String> colQuarto = new TableColumn<>("Quarto");
        colQuarto.setCellValueFactory(new PropertyValueFactory<>("numeroQuarto"));
        colQuarto.setPrefWidth(80);

        // Check-in
        TableColumn<Reserva, String> colCheckin = new TableColumn<>("Check-in");
        colCheckin.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataCheckin() != null) {
                return new SimpleStringProperty(cellData.getValue().getDataCheckin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("");
        });
        colCheckin.setPrefWidth(100);

        // Check-out
        TableColumn<Reserva, String> colCheckout = new TableColumn<>("Check-out");
        colCheckout.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataCheckout() != null) {
                return new SimpleStringProperty(cellData.getValue().getDataCheckout().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("");
        });
        colCheckout.setPrefWidth(100);

        // Diárias
        TableColumn<Reserva, Long> colDias = new TableColumn<>("Diárias");
        colDias.setCellValueFactory(new PropertyValueFactory<>("diarias"));
        colDias.setPrefWidth(70);

        // Valor
        TableColumn<Reserva, String> colValor = new TableColumn<>("Valor Total");
        colValor.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("R$ %,.2f", cellData.getValue().getValorTotal()))
        );
        colValor.setPrefWidth(120);
        
        // Status Pagamento
        TableColumn<Reserva, String> colStatus = new TableColumn<>("Status Pag.");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("nomeStatus"));
        colStatus.setPrefWidth(100);
        
        // Colorir a coluna de status
        colStatus.setCellFactory(column -> new TableCell<Reserva, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : getItem());
                if (!empty) {
                    if ("Pago".equals(item)) {
                        setTextFill(Color.web("#27AE60")); // Verde
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.web("#E74C3C")); // Vermelho
                        setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });

        tabela.getColumns().addAll(colId, colCliente, colQuarto, colCheckin, colCheckout, colDias, colValor, colStatus);
    }

    private void carregarDados() {
        ReservaController ctrl = new ReservaController();
        List<Reserva> reservas = ctrl.listarTodasReservas();
        ObservableList<Reserva> dados = FXCollections.observableArrayList(reservas);
        tabela.setItems(dados);
    }
}
