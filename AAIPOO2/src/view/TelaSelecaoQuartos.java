package view;

import dao.QuartoDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Conexao;
import model.Quarto;

import java.util.List;

/**
 * Tela de seleção visual de quartos.
 * Exibe todos os quartos do hotel em cards coloridos (verde = disponível, vermelho = ocupado).
 */
public class TelaSelecaoQuartos {

    public Node getNode() {
        ScrollPane scroll = new ScrollPane(criarConteudo());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F4F8; -fx-background: #F0F4F8;");
        return scroll;
    }

    private VBox criarConteudo() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(35, 40, 35, 40));
        root.setStyle("-fx-background-color: #F0F4F8;");

        // ── Cabeçalho ──
        VBox header = new VBox(4);
        Label lblTitulo = new Label("🛏  Quartos e Status");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));
        
        Label lblSub = new Label("Visão geral dos quartos do hotel. Clique em um quarto disponível para check-in.");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web("#666"));
        
        // Legenda de status
        HBox legenda = new HBox(15);
        legenda.setPadding(new Insets(10, 0, 0, 0));
        legenda.getChildren().addAll(
            criarItemLegenda("Disponível", "#27AE60"),
            criarItemLegenda("Ocupado", "#E74C3C"),
            criarItemLegenda("Manutenção", "#F39C12")
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
                grid.getChildren().add(empty);
            } else {
                for (Quarto q : quartos) {
                    grid.getChildren().add(criarCardQuarto(q));
                }
            }
        } catch (Exception e) {
            Label erro = new Label("Erro ao carregar quartos: " + e.getMessage());
            erro.setTextFill(Color.RED);
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
        lbl.setTextFill(Color.web("#555"));
        
        item.getChildren().addAll(bolinha, lbl);
        return item;
    }

    private VBox criarCardQuarto(Quarto q) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(220);
        
        // Define a cor baseada no status
        // 1 = Disponível, 2 = Ocupado, 3 = Manutenção
        String corFundo, corBorda, corTexto, textoStatus;
        boolean clicavel = false;
        
        if (q.getIdStatus() == 1) {
            corFundo = "#EAFAF1";
            corBorda = "#27AE60";
            corTexto = "#27AE60";
            textoStatus = "DISPONÍVEL";
            clicavel = true;
        } else if (q.getIdStatus() == 2) {
            corFundo = "#FDEDEC";
            corBorda = "#E74C3C";
            corTexto = "#E74C3C";
            textoStatus = "OCUPADO";
        } else {
            corFundo = "#FEF9E7";
            corBorda = "#F39C12";
            corTexto = "#F39C12";
            textoStatus = "MANUTENÇÃO";
        }
        
        card.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 10;"
          + "-fx-border-color: " + corBorda + ";"
          + "-fx-border-width: 2;"
          + "-fx-border-radius: 8;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );
        
        // Cabeçalho do card
        HBox headerCard = new HBox();
        headerCard.setAlignment(Pos.CENTER_LEFT);
        
        Label lblNum = new Label(q.getNumeroQuarto());
        lblNum.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblNum.setTextFill(Color.web("#1E2A4A"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lblStatus = new Label(textoStatus);
        lblStatus.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        lblStatus.setTextFill(Color.WHITE);
        lblStatus.setPadding(new Insets(3, 8, 3, 8));
        lblStatus.setStyle("-fx-background-color: " + corBorda + "; -fx-background-radius: 10;");
        
        headerCard.getChildren().addAll(lblNum, spacer, lblStatus);
        
        // Informações
        Label lblTipo = new Label((q.getNomeTipo() != null ? q.getNomeTipo() : "Padrão") + " • " + q.getCapacidadePessoas() + " pessoas");
        lblTipo.setFont(Font.font("Arial", 12));
        lblTipo.setTextFill(Color.web("#666"));
        
        Label lblPreco = new Label(String.format("R$ %.2f / dia", q.getPrecoBase()));
        lblPreco.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblPreco.setTextFill(Color.web("#2980B9"));
        
        card.getChildren().addAll(headerCard, lblTipo, lblPreco);
        
        // Ação de clique para quartos disponíveis
        if (clicavel) {
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");
            card.setOnMouseEntered(e -> card.setOpacity(0.8));
            card.setOnMouseExited(e -> card.setOpacity(1.0));
            card.setOnMouseClicked(e -> {
                Principal.navegarParaCheckinComQuarto(q);
            });
            
            // Dica de hover
            Tooltip t = new Tooltip("Clique para fazer Check-in");
            Tooltip.install(card, t);
        }
        
        return card;
    }
}
