package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexao {

    private static String Url =
        "jdbc:sqlserver://10.109.8.9:1433;databaseName=P00_gp01;" +
        "user=P00_gp01;password=;encrypt=false;trustServerCertificate=true;loginTimeout=30;";

    public static Connection conexao;

    public static void conectar() {
        try {
            conexao = DriverManager.getConnection(Url);
            JOptionPane.showMessageDialog(null, "Conexão realizada com sucesso!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro de conexão!\nERRO: " + ex.getMessage());
        }
    }

    public static void desconectar() {
        try {
            conexao.close();
            JOptionPane.showMessageDialog(null, "Conexão fechada com sucesso!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao fechar a conexão!\nERRO: " + ex.getMessage());
        }
    }
}