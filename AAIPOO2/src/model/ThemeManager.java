package model;

/**
 * Gerenciador de tema da aplicação (Claro / Escuro).
 * Todas as telas consultam esta classe para obter as cores corretas.
 * Chamar toggle() alterna entre os dois modos.
 */
public class ThemeManager {

    private static boolean dark = false;

    public static boolean isDark() { return dark; }
    public static void toggle()    { dark = !dark; }

    // ── Fundo e superfície ────────────────────────────────────
    public static String bg()         { return dark ? "#1A1D27" : "#F0F4F8"; }
    public static String card()       { return dark ? "#252B3B" : "#FFFFFF"; }
    public static String cardShadow() { return dark ? "rgba(0,0,0,0.35)" : "rgba(0,0,0,0.08)"; }

    // ── Texto ─────────────────────────────────────────────────
    public static String titleColor()    { return dark ? "#E8EEF7" : "#1E2A4A"; }
    public static String subtitleColor() { return dark ? "#8898AA" : "#666666"; }
    public static String labelColor()    { return dark ? "#B0BDD0" : "#444444"; }
    public static String textColor()     { return dark ? "#CCD6E8" : "#2C3E50"; }
    public static String mutedText()     { return dark ? "#6B7C93" : "#7F8C8D"; }

    // ── Campos ────────────────────────────────────────────────
    public static String fieldBg()     { return dark ? "#1E2330" : "#F8F9FA"; }
    public static String fieldBorder() { return dark ? "#374060" : "#DDE3EC"; }

    // ── Sidebar ───────────────────────────────────────────────
    public static String sidebarBg()     { return dark ? "#0F1520" : "#1E2A4A"; }
    public static String sidebarHeader() { return dark ? "#080E18" : "#15203A"; }
    public static String sidebarSep()    { return dark ? "#1A2535" : "#2E3E60"; }
    public static String sidebarLabel()  { return dark ? "#3A5070" : "#4A6080"; }
    public static String sidebarItem()   { return dark ? "#6A80A0" : "#90A8C4"; }
    public static String sidebarHover()  { return dark ? "#1A2535" : "#263558"; }

    // ── Top bar ───────────────────────────────────────────────
    public static String topBarBg()     { return dark ? "#1E2330" : "#FFFFFF"; }
    public static String topBarBorder() { return dark ? "#374060" : "#DDE3EC"; }
    public static String topBarText()   { return dark ? "#E8EEF7" : "#1E2A4A"; }
    public static String topBarSub()    { return dark ? "#8898AA" : "#444444"; }

    // ── Caixa de resumo (pagamento) ───────────────────────────
    public static String resumoBg()     { return dark ? "#1A1E2E" : "#F8F9FA"; }
    public static String resumoBorder() { return dark ? "#374060" : "#EAECEE"; }
    public static String resumoTitle()  { return dark ? "#CCD6E8" : "#34495E"; }

    // ── Caixa de dica/info ────────────────────────────────────
    public static String infoBg()     { return dark ? "#2A2415" : "#FEF9E7"; }
    public static String infoBorder() { return dark ? "#5A4A20" : "#F4D03F"; }
    public static String infoText()   { return dark ? "#C9A84C" : "#7D6608"; }

    // ── Cards de stat (dashboard) ─────────────────────────────
    // Em dark mode usa a cor do card padrão em vez do pastel claro
    public static String statCardBg(String lightBg) {
        return dark ? card() : lightBg;
    }

    // ── Tabela ────────────────────────────────────────────────
    public static String tableStyle() {
        String base = "-fx-font-family: Arial; -fx-font-size: 13px;";
        if (!dark) return base;
        return base
            + "-fx-background-color: " + card() + ";"
            + "-fx-control-inner-background: " + card() + ";"
            + "-fx-control-inner-background-alt: " + fieldBg() + ";"
            + "-fx-table-header-border-color: " + fieldBorder() + ";"
            + "-fx-selection-bar: #2A3F6B;"
            + "-fx-selection-bar-non-focused: " + fieldBorder() + ";"
            + "-fx-base: " + card() + ";";
    }

    /**
     * Cor de texto para células de tabela, usada nos cell factories.
     * Em light mode retorna a mesma cor que textColor().
     * Necessário porque o Modena CSS sobrescreve text-fill nas TableCells
     * e somente um setCellFactory com setTextFill() garante a cor correta.
     */
    public static javafx.scene.paint.Color tableCellTextColor() {
        return javafx.scene.paint.Color.web(textColor());
    }

    // ── ListView ─────────────────────────────────────────────
    public static String listStyle() {
        String base = "-fx-font-family: Arial; -fx-font-size: 14px; -fx-background-radius: 6;";
        if (!dark) return base;
        return base
            + "-fx-control-inner-background: " + fieldBg() + ";"
            + "-fx-background-color: " + fieldBg() + ";";
    }

    // ── Helpers de estilo completo ────────────────────────────
    public static String scrollStyle() {
        return "-fx-background-color: " + bg() + "; -fx-background: " + bg() + ";";
    }

    public static String cardStyle() {
        return "-fx-background-color: " + card() + ";"
             + "-fx-background-radius: 12;"
             + "-fx-effect: dropshadow(gaussian, " + cardShadow() + ", 15, 0, 0, 4);";
    }

    public static String fieldStyle() {
        return "-fx-background-color: " + fieldBg() + ";"
             + "-fx-border-color: "      + fieldBorder() + ";"
             + "-fx-border-radius: 6;"
             + "-fx-background-radius: 6;"
             + "-fx-padding: 10 12;"
             + "-fx-font-size: 13px;"
             + "-fx-text-fill: "         + textColor()  + ";"   // texto digitado
             + "-fx-prompt-text-fill: "  + mutedText()  + ";";  // placeholder
    }

    // ── Helpers para ComboBox e DatePicker ───────────────────
    /**
     * Aplica o tema correto a um ComboBox.
     * O Modena CSS sobrescreve o text-fill das células internas (buttonCell e
     * células do dropdown) mesmo quando o container já tem -fx-text-fill.
     * Este método define explicitamente as células, assim como fazemos com TableCell.
     */
    public static <T> void applyComboStyle(javafx.scene.control.ComboBox<T> cb) {
        cb.setStyle(fieldStyle());
        // Célula que exibe o item selecionado (ComboBox fechado)
        cb.setButtonCell(new javafx.scene.control.ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setStyle("-fx-text-fill: " + textColor() + ";"
                       + "-fx-background-color: transparent;");
            }
        });
        // Células da lista suspensa (ComboBox aberto)
        cb.setCellFactory(lv -> new javafx.scene.control.ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setStyle("-fx-text-fill: " + textColor() + ";"
                       + "-fx-background-color: " + fieldBg() + ";");
            }
        });
    }

    /**
     * Aplica o tema correto a um DatePicker.
     * O editor interno (TextField) é um nó separado e precisa de estilo próprio
     * — só definir o estilo no DatePicker container não é suficiente.
     */
    public static void applyDatePickerStyle(javafx.scene.control.DatePicker dp) {
        dp.setStyle(fieldStyle());
        dp.getEditor().setStyle(fieldStyle());
    }

    // ── Botão de toggle ───────────────────────────────────────
    public static String toggleLabel() {
        return dark ? "☀  Modo Claro" : "🌙  Modo Escuro";
    }

    public static String toggleStyle() {
        if (dark) {
            return "-fx-background-color: #C9A84C; -fx-text-fill: #1A1D27;"
                 + "-fx-background-radius: 20; -fx-padding: 6 16;"
                 + "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;";
        } else {
            return "-fx-background-color: #252B3B; -fx-text-fill: #E8EEF7;"
                 + "-fx-background-radius: 20; -fx-padding: 6 16;"
                 + "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;";
        }
    }
}
