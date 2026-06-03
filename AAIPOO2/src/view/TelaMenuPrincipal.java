package view;

import controler.PagamentoController;
import controler.QuartoController;
import controler.ReservaController;
import dao.QuartoDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Conexao;

/**
 * Dashboard principal do sistema.
 * Exibe cards de resumo com dados do banco e botões de ação rápida.
 */
public class TelaMenuPrincipal {

    public Node getNode() {
        ScrollPane scroll = new ScrollPane(criarConteudo());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F4F8; -fx-background: #F0F4F8;");
        return scroll;
    }

    private VBox criarConteudo() {
        VBox root = new VBox(28);
        root.setPadding(new Insets(35, 40, 35, 40));
        root.setStyle("-fx-background-color: #F0F4F8;");

        // ── Cabeçalho ──
        VBox header = new VBox(4);
        Label lblTitulo = new Label("🏠  Dashboard");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));
        Label lblSub = new Label("Bem-vindo ao sistema de gestão do hotel");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web("#666"));
        header.getChildren().addAll(lblTitulo, lblSub);

        // ── Cards de estatísticas ──
        GridPane cards = new GridPane();
        cards.setHgap(20);
        cards.setVgap(20);

        // Carrega dados do banco
        int disponiveis   = carregarContagem(1);
        int ocupados      = carregarContagem(2);
        int reservasAtiv  = carregarReservasAtivas();
        int reservasHoje  = carregarReservasHoje();
        double receita    = carregarReceita();

        cards.add(criarCard("🟢", "Quartos Disponíveis", String.valueOf(disponiveis),  "#27AE60", "#EAFAF1"), 0, 0);
        cards.add(criarCard("🔴", "Quartos Ocupados",    String.valueOf(ocupados),     "#E74C3C", "#FDEDEC"), 1, 0);
        cards.add(criarCard("📋", "Reservas Ativas",      String.valueOf(reservasAtiv), "#2980B9", "#EBF5FB"), 2, 0);
        cards.add(criarCard("📅", "Entradas Hoje",        String.valueOf(reservasHoje), "#8E44AD", "#F5EEF8"), 3, 0);

        // Card de receita - largura dupla
        HBox cardReceita = criarCardReceita(receita);
        GridPane.setColumnSpan(cardReceita, 2);
        cards.add(cardReceita, 0, 1);

        // ── Seção de ações rápidas ──
        Label lblAcoes = new Label("Ações Rápidas");
        lblAcoes.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        lblAcoes.setTextFill(Color.web("#1E2A4A"));

        HBox acoes = new HBox(15);
        acoes.setAlignment(Pos.CENTER_LEFT);
        acoes.getChildren().addAll(
            btnAcao("📋  Novo Check-in",      "#27AE60", "checkin"),
            btnAcao("🚪  Realizar Checkout",   "#E74C3C", "checkout"),
            btnAcao("🛏  Ver Quartos",         "#2980B9", "selecao"),
            btnAcao("📊  Relatórios",          "#8E44AD", "relatorio")
        );

        // ── Dica de uso ──
        HBox dica = new HBox(10);
        dica.setAlignment(Pos.CENTER_LEFT);
        dica.setPadding(new Insets(15, 20, 15, 20));
        dica.setStyle("-fx-background-color: #FEF9E7; -fx-background-radius: 10;"
                    + "-fx-border-color: #F4D03F; -fx-border-radius: 10;");
        Label lblDica = new Label("💡  Fluxo: Cadastre o Cliente → Faça Check-in selecionando o quarto e datas → No Checkout selecione a reserva e confirme o pagamento.");
        lblDica.setFont(Font.font("Arial", 13));
        lblDica.setTextFill(Color.web("#7D6608"));
        lblDica.setWrapText(true);
        dica.getChildren().add(lblDica);

        root.getChildren().addAll(header, cards, lblAcoes, acoes, dica);
        return root;
    }

    // ── Card de estatística individual ────────────────────────

    private VBox criarCard(String icone, String titulo, String valor,
                           String corTexto, String corFundo) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(22, 25, 22, 25));
        card.setPrefWidth(210);
        card.setStyle(
            "-fx-background-color: " + corFundo + ";"
          + "-fx-background-radius: 14;"
          + "-fx-border-color: " + corTexto + "22;"
          + "-fx-border-radius: 14;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label lblIcone = new Label(icone);
        lblIcone.setFont(Font.font(28));

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        lblValor.setTextFill(Color.web(corTexto));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", 13));
        lblTitulo.setTextFill(Color.web("#555"));

        card.getChildren().addAll(lblIcone, lblValor, lblTitulo);
        return card;
    }

    // ── Card de receita total ──────────────────────────────────

    private HBox criarCardReceita(double receita) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(22, 30, 22, 30));
        card.setStyle(
            "-fx-background-color: linear-gradient(to right, #1E2A4A, #2E4070);"
          + "-fx-background-radius: 14;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3);"
        );

        Label lblIcone = new Label("💰");
        lblIcone.setFont(Font.font(36));

        VBox texts = new VBox(4);
        Label lblTitulo = new Label("Receita Total do Hotel");
        lblTitulo.setFont(Font.font("Arial", 14));
        lblTitulo.setTextFill(Color.web("#90A8C4"));

        Label lblValor = new Label(String.format("R$ %,.2f", receita));
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        lblValor.setTextFill(Color.web("#C9A84C"));

        texts.getChildren().addAll(lblTitulo, lblValor);
        card.getChildren().addAll(lblIcone, texts);
        return card;
    }

    // ── Botão de ação rápida ───────────────────────────────────

    private Button btnAcao(String texto, String cor, String tela) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(12, 22, 12, 22));
        btn.setStyle(
            "-fx-background-color: " + cor + ";"
          + "-fx-text-fill: white;"
          + "-fx-background-radius: 9;"
          + "-fx-cursor: hand;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        btn.setOnAction(e -> Principal.navegarPara(tela));
        return btn;
    }

    // ── Carrega dados do banco com fallback ───────────────────

    private int carregarContagem(int idStatus) {
        try {
            Conexao.conectar();
            dao.QuartoDao dao = new dao.QuartoDao(Conexao.conexao);
            return dao.contarPorStatus(idStatus);
        } catch (Exception e) { return 0; }
        finally { Conexao.desconectar(); }
    }

    private int carregarReservasAtivas() {
        try {
            ReservaController ctrl = new ReservaController();
            return ctrl.contarAtivas();
        } catch (Exception e) { return 0; }
    }

    private int carregarReservasHoje() {
        try {
            ReservaController ctrl = new ReservaController();
            return ctrl.contarHoje();
        } catch (Exception e) { return 0; }
    }

    private double carregarReceita() {
        try {
            PagamentoController ctrl = new PagamentoController();
            return ctrl.calcularReceitaTotal();
        } catch (Exception e) { return 0.0; }
    }
}
