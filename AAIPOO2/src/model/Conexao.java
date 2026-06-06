package model;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Conexao {

    /**
     * URL lida de db.properties (arquivo local, fora do git).
     * Cada máquina tem seu próprio db.properties com a string de conexão correta.
     * Fallback: tenta localhost\SQLEXPRESS caso o arquivo não exista.
     */
    private static final String Url = carregarUrl();

    private static String carregarUrl() {
        try (InputStream in = Conexao.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String url = props.getProperty("db.url");
                if (url != null && !url.isBlank()) return url.trim();
            }
        } catch (IOException ignored) {}
        // Fallback caso db.properties não exista
        return "jdbc:sqlserver://localhost\\SQLEXPRESS;"
             + "databaseName=DA123_AAI_G07;"
             + "integratedSecurity=true;encrypt=false;"
             + "trustServerCertificate=true;loginTimeout=30;";
    }

    public static Connection conexao;

    public static void conectar() {
        try {
            conexao = DriverManager.getConnection(Url);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro de conexão!\nERRO: " + ex.getMessage());
        }
    }

    public static void desconectar() {
        try {
            conexao.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao fechar a conexão!\nERRO: " + ex.getMessage());
        }
    }
}
