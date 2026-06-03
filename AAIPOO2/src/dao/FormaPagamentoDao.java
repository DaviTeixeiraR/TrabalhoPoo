package dao;

import java.sql.*;
import java.util.*;
import model.FormaPagamento;

/**
 * DAO para operações na tabela Forma_Pagamento.
 * Fornece a lista de formas de pagamento disponíveis para o ComboBox.
 * Se a tabela estiver vazia, retorna opções padrão.
 */
public class FormaPagamentoDao {

    private Connection conn;

    public FormaPagamentoDao(Connection conn) {
        this.conn = conn;
    }

    // ── Listar todas as formas de pagamento ────────────────────

    public List<FormaPagamento> listar() throws SQLException {
        List<FormaPagamento> lista = new ArrayList<>();
        String sql = "SELECT id_forma_pagamento, descricao FROM Forma_Pagamento ORDER BY id_forma_pagamento";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            lista.add(new FormaPagamento(
                rs.getInt("id_forma_pagamento"),
                rs.getString("descricao")
            ));
        }
        rs.close();
        stmt.close();

        // Fallback: se tabela vazia, retorna opções padrão
        if (lista.isEmpty()) {
            lista.add(new FormaPagamento(1, "Dinheiro"));
            lista.add(new FormaPagamento(2, "Cartão de Crédito"));
            lista.add(new FormaPagamento(3, "Cartão de Débito"));
            lista.add(new FormaPagamento(4, "PIX"));
        }
        return lista;
    }
}
