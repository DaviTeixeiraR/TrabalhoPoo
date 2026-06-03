package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Quarto;
import model.Reserva;

/**
 * Classe principal da aplicação JavaFX.
 * Gerencia a navegação entre telas via BorderPane com sidebar lateral.
 * Todas as telas chamam Principal.navegarPara() para trocar o conteúdo central.
 */
public class Principal extends Application {

    // ── Estado global da aplicação ────────────────────────────
    private static BorderPane rootPane;
    private static Stage      appStage;
    private static Button     btnAtivo = null;

    // ─────────────────────────────────────────────────────────
    // CICLO DE VIDA JAVAFX
    // ─────────────────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        appStage = stage;
        stage.setTitle("🏨 Hotel Manager");
        mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ─────────────────────────────────────────────────────────
    // NAVEGAÇÃO PRINCIPAL
    // ─────────────────────────────────────────────────────────

    /** Exibe a tela de login (janela pequena, sem barra lateral). */
    public static void mostrarLogin() {
        Tela_Login telaLogin = new Tela_Login();
        Scene scene = telaLogin.getScene();
        appStage.setScene(scene);
        appStage.setWidth(480);
        appStage.setHeight(430);
        appStage.setResizable(true);
        appStage.centerOnScreen();
        appStage.show();
    }

    /** Exibe o sistema principal com sidebar + área de conteúdo. */
    public static void mostrarSistema() {
        rootPane = new BorderPane();
        rootPane.setLeft(criarSidebar());
        rootPane.setTop(criarTopBar());
        rootPane.setStyle("-fx-background-color: #F0F4F8;");

        // Tela inicial: dashboard
        navegarPara("dashboard");

        Scene scene = new Scene(rootPane, 1100, 700);
        appStage.setScene(scene);
        appStage.setWidth(1100);
        appStage.setHeight(700);
        appStage.setResizable(true);
        appStage.setMaximized(true); // Abre a janela em tela cheia / maximizada
        appStage.centerOnScreen();
    }

    // ─────────────────────────────────────────────────────────
    // MÉTODOS DE NAVEGAÇÃO (chamados pelas telas)
    // ─────────────────────────────────────────────────────────

    /** Navega para uma tela pelo nome. */
    public static void navegarPara(String tela) {
        if (rootPane == null) return;
        Node node;
        switch (tela) {
            case "dashboard": node = new TelaMenuPrincipal().getNode();   break;
            case "clientes":  node = new TelaDadosPessoais().getNode();   break;
            case "quartos":   node = new TelaQuartos().getNode();          break;
            case "selecao":   node = new TelaSelecaoQuartos().getNode();   break;
            case "checkin":   node = new TelaCheckin().getNode();           break;
            case "checkout":  node = new TelaCheckout().getNode();          break;
            case "relatorio": node = new TelaRelatorio().getNode();         break;
            default:          node = new TelaMenuPrincipal().getNode();
        }
        rootPane.setCenter(node);
    }

    /** Navega para a tela de pagamento passando a reserva selecionada. */
    public static void navegarParaPagamento(Reserva reserva) {
        if (rootPane != null)
            rootPane.setCenter(new TelaCheckoutPagamento(reserva).getNode());
    }

    /** Navega para check-in com um quarto já pré-selecionado. */
    public static void navegarParaCheckinComQuarto(Quarto quarto) {
        if (rootPane != null)
            rootPane.setCenter(new TelaCheckin(quarto).getNode());
    }

    public static Stage getStage() { return appStage; }

    // ─────────────────────────────────────────────────────────
    // CONSTRUÇÃO DA SIDEBAR
    // ─────────────────────────────────────────────────────────

    private static VBox criarSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setStyle("-fx-background-color: #1E2A4A;");

        // ── Cabeçalho / Logo ──
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(28, 15, 22, 15));
        header.setStyle("-fx-background-color: #15203A;");

        Label lblIcon  = new Label("🏨");
        lblIcon.setFont(Font.font(38));

        Label lblNome  = new Label("HOTEL MANAGER");
        lblNome.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblNome.setTextFill(Color.WHITE);

        Label lblSub   = new Label("Sistema de Gestão");
        lblSub.setFont(Font.font("Arial", 10));
        lblSub.setTextFill(Color.web("#7A8FAA"));

        header.getChildren().addAll(lblIcon, lblNome, lblSub);

        // ── Separador ──
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2E3E60;");
        VBox.setMargin(sep, new Insets(0));

        // ── Itens de navegação ──
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 8, 8));

        nav.getChildren().addAll(
            secao("PRINCIPAL"),
            btnNav("🏠   Dashboard",       "dashboard"),
            secao("HOSPEDAGEM"),
            btnNav("🛏   Quartos / Status", "selecao"),
            btnNav("📋   Check-in",          "checkin"),
            btnNav("🚪   Checkout",           "checkout"),
            secao("CADASTRO"),
            btnNav("👤   Clientes",           "clientes"),
            btnNav("🔑   Cad. Quartos",       "quartos"),
            secao("ANÁLISE"),
            btnNav("📊   Relatórios",         "relatorio")
        );

        VBox.setVgrow(nav, Priority.ALWAYS);
        sidebar.getChildren().addAll(header, sep, nav);
        return sidebar;
    }

    private static Label secao(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        lbl.setTextFill(Color.web("#4A6080"));
        lbl.setPadding(new Insets(14, 5, 4, 10));
        return lbl;
    }

    private static Button btnNav(String texto, String tela) {
        Button btn = new Button(texto);
        btn.setPrefWidth(202);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle(estiloNavNormal());
        btn.setOnMouseEntered(e -> { if (btn != btnAtivo) btn.setStyle(estiloNavHover()); });
        btn.setOnMouseExited(e  -> { if (btn != btnAtivo) btn.setStyle(estiloNavNormal()); });
        btn.setOnAction(e -> {
            if (btnAtivo != null) btnAtivo.setStyle(estiloNavNormal());
            btn.setStyle(estiloNavAtivo());
            btnAtivo = btn;
            navegarPara(tela);
        });
        return btn;
    }

    private static String estiloNavNormal() {
        return "-fx-background-color: transparent; -fx-text-fill: #90A8C4;"
             + "-fx-padding: 10 15; -fx-background-radius: 8; -fx-cursor: hand;";
    }
    private static String estiloNavHover() {
        return "-fx-background-color: #263558; -fx-text-fill: white;"
             + "-fx-padding: 10 15; -fx-background-radius: 8; -fx-cursor: hand;";
    }
    private static String estiloNavAtivo() {
        return "-fx-background-color: #C9A84C; -fx-text-fill: white;"
             + "-fx-padding: 10 15; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    // ─────────────────────────────────────────────────────────
    // CONSTRUÇÃO DA BARRA SUPERIOR
    // ─────────────────────────────────────────────────────────

    private static HBox criarTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(13, 25, 13, 25));
        topBar.setStyle("-fx-background-color: white;"
                      + "-fx-border-color: #DDE3EC;"
                      + "-fx-border-width: 0 0 1 0;");

        Label lblTitulo = new Label("Sistema de Hotel");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(Color.web("#1E2A4A"));
        HBox.setHgrow(lblTitulo, Priority.ALWAYS);

        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER);

        Label lblAv = new Label("👤");
        lblAv.setFont(Font.font(18));

        Label lblUser = new Label("Administrador");
        lblUser.setFont(Font.font("Arial", 13));
        lblUser.setTextFill(Color.web("#444"));

        Button btnSair = new Button("  Sair  ");
        btnSair.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnSair.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;"
                       + "-fx-background-radius: 6; -fx-cursor: hand;");
        btnSair.setOnAction(e -> {
            btnAtivo = null;
            mostrarLogin();
        });

        userBox.getChildren().addAll(lblAv, lblUser, btnSair);
        topBar.getChildren().addAll(lblTitulo, userBox);
        return topBar;
    }
}
