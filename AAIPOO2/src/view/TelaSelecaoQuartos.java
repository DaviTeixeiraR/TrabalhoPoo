package view;

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
import model.Quarto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.ThemeManager;

import java.util.List;

/**
 * Tela de seleção visual de quartos.
 * Exibe todos os quartos em cards coloridos (verde = disponível, vermelho = ocupado).
 */
public class TelaSelecaoQuartos {

    public Node getNode() {
        ScrollPane scroll = new ScrollPane(criarConteudo());
        scroll.setFitToWidth(true);
        scroll.setStyle(ThemeManager.scrollStyle());
        return scroll;
    }

    private VBox criarConteudo() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(35, 40, 35, 40));
        root.setStyle("-fx-background-color: " + ThemeManager.bg() + ";");

        // ── Cabeçalho ──
        VBox header = new VBox(4);
        Label lblTitulo = new Label("🛏  Quartos e Status");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web(ThemeManager.titleColor()));

        Label lblSub = new Label("Visão geral dos quartos do hotel. Clique em um quarto disponível para check-in.");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web(ThemeManager.subtitleColor()));

        // Legenda de status
        HBox legenda = new HBox(15);
        legenda.setPadding(new Insets(10, 0, 0, 0));
        legenda.getChildren().addAll(
            criarItemLegenda("Disponível",         "#27AE60"),
            criarItemLegenda("Reservado (futuro)", "#E67E22"),
            criarItemLegenda("Ocupado (presente)", "#E74C3C")
        );
        header.getChildren().addAll(lblTitulo, lblSub, legenda);

        // ── Grid de Quartos ──
        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);

        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            List<Quarto> quartos = dao.listarTodosComStatus();

            if (quartos.isEmpty()) {
                Label empty = new Label("Nenhum quarto cadastrado no sistema.");
                empty.setFont(Font.font(14));
                empty.setTextFill(Color.web(ThemeManager.subtitleColor()));
                grid.getChildren().add(empty);
            } else {
                for (Quarto q : quartos) {
                    grid.getChildren().add(criarCardQuarto(q));
                }
            }
        } catch (Exception e) {
            Label erro = new Label("Erro ao carregar quartos: " + e.getMessage());
            erro.setTextFill(Color.web("#E74C3C"));
            grid.getChildren().add(erro);
        } finally {
            Conexao.desconectar();
        }

        root.getChildren().addAll(header, grid);
        return root;
    }

    private HBox criarItemLegenda(String texto, String cor) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);

        Label bolinha = new Label("●");
        bolinha.setTextFill(Color.web(cor));
        bolinha.setFont(Font.font(16));

        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web(ThemeManager.subtitleColor()));

        item.getChildren().addAll(bolinha, lbl);
        return item;
    }

    private VBox criarCardQuarto(Quarto q) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(240);

        // Status derivado das reservas ativas, não do campo id_status
        String statusDisplay = q.getStatusDisplay();
        String corBorda, corTexto, textoStatus;
        boolean clicavel = false;

        if (statusDisplay.equals("Disponível")) {
            corBorda    = "#27AE60";
            corTexto    = "#27AE60";
            textoStatus = "DISPONÍVEL";
            clicavel    = true;
        } else if (statusDisplay.startsWith("Ocupado")) {
            corBorda    = "#E74C3C";
            corTexto    = "#E74C3C";
            textoStatus = statusDisplay.toUpperCase();
        } else {
            // "Reservado: DD/MM a DD/MM"
            corBorda    = "#E67E22";
            corTexto    = "#E67E22";
            textoStatus = statusDisplay.toUpperCase();
        }

        card.setStyle(
            "-fx-background-color: " + ThemeManager.card() + ";"
          + "-fx-background-radius: 10;"
          + "-fx-border-color: " + corBorda + ";"
          + "-fx-border-width: 2;"
          + "-fx-border-radius: 8;"
          + "-fx-effect: dropshadow(gaussian, " + ThemeManager.cardShadow() + ", 5, 0, 0, 2);"
        );

        // Cabeçalho do card
        HBox headerCard = new HBox();
        headerCard.setAlignment(Pos.CENTER_LEFT);

        Label lblNum = new Label(String.valueOf(q.getNumeroQuarto()));
        lblNum.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblNum.setTextFill(Color.web(ThemeManager.titleColor()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = new Label(textoStatus);
        lblStatus.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        lblStatus.setTextFill(Color.WHITE);
        lblStatus.setPadding(new Insets(3, 7, 3, 7));
        lblStatus.setWrapText(true);
        lblStatus.setMaxWidth(130);
        lblStatus.setStyle("-fx-background-color: " + corBorda + "; -fx-background-radius: 10;");
        headerCard.getChildren().addAll(lblNum, spacer, lblStatus);

        Label lblTipo = new Label((q.getNomeTipo() != null ? q.getNomeTipo() : "Padrão") + " • " + q.getCapacidadePessoas() + " pessoas");
        lblTipo.setFont(Font.font("Arial", 12));
        lblTipo.setTextFill(Color.web(ThemeManager.subtitleColor()));

        Label lblPreco = new Label(String.format("R$ %.2f / dia", q.getPrecoBase()));
        lblPreco.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblPreco.setTextFill(Color.web("#2980B9"));

        card.getChildren().addAll(headerCard, lblTipo, lblPreco);

        // Botão de reservas ativas — aparece sempre que há pelo menos uma reserva
        List<LocalDate[]> reservas = q.getReservasAtivas();
        if (!reservas.isEmpty()) {
            Button btnReservas = new Button("📅 Ver reservas (" + reservas.size() + ")");
            btnReservas.setFont(Font.font("Arial", 11));
            btnReservas.setStyle(
                "-fx-background-color: transparent;"
              + "-fx-border-color: " + corBorda + ";"
              + "-fx-border-radius: 6; -fx-text-fill: " + corBorda + ";"
              + "-fx-cursor: hand; -fx-padding: 4 10;"
            );
            btnReservas.setOnAction(e -> abrirPopupReservas(q.getNumeroQuarto(), reservas));
            card.getChildren().add(btnReservas);
        }

        if (clicavel) {
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");
            card.setOnMouseEntered(e -> card.setOpacity(0.8));
            card.setOnMouseExited(e  -> card.setOpacity(1.0));
            card.setOnMouseClicked(e -> {
                // Evita que clique no botão de reservas também dispare o checkin
                if (e.getTarget() instanceof Button) return;
                Principal.navegarParaCheckinComQuarto(q);
            });
            Tooltip.install(card, new Tooltip("Clique no card para fazer Check-in"));
        }

        return card;
    }

    private void abrirPopupReservas(int numeroQuarto, List<LocalDate[]> reservas) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoje = LocalDate.now();

        StringBuilder sb = new StringBuilder();
        for (LocalDate[] r : reservas) {
            LocalDate checkin  = r[0];
            LocalDate checkout = r[1];
            String linha = "• " + checkin.format(fmt) + " a " + checkout.format(fmt);
            if (!hoje.isBefore(checkin) && !hoje.isAfter(checkout)) {
                linha += "  ← em curso";
            }
            sb.append(linha).append("\n");
        }

        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setTitle("Reservas do Quarto " + numeroQuarto);
        popup.setHeaderText("Quarto " + numeroQuarto + " — " + reservas.size()
            + (reservas.size() == 1 ? " reserva ativa" : " reservas ativas"));
        popup.setContentText(sb.toString().trim());
        popup.showAndWait();
    }
}
