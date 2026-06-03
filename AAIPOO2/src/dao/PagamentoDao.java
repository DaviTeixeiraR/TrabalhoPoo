package dao;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import model.Pagamento;

/**
 * DAO para operações na tabela Pagamento.
 */
public class PagamentoDao {

    private Connection conn;

    public PagamentoDao(Connection conn) {
        this.conn = conn;
    }

    // ── Inserir pagamento ──────────────────────────────────────

    public void inserir(Pagamento pagamento) throws SQLException {
        String sql = "INSERT INTO Pagamento (id_reserva, id_forma_pagamento, valor_pago, data_pagamento) "
                   + "VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, pagamento.getIdReserva());
        stmt.setInt(2, pagamento.getIdFormaPagamento());
        stmt.setDouble(3, pagamento.getValorPago());
        stmt.setDate(4, Date.valueOf(pagamento.getDataPagamento()));
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) pagamento.setIdPagamento(rs.getInt(1));
        rs.close();
        stmt.close();
    }

    // ── Listar todos os pagamentos com detalhes (para Relatório) ──

    public List<Pagamento> listarTodos() throws SQLException {
        List<Pagamento> lista = new ArrayList<>();
        String sql = "SELECT p.id_pagamento, p.id_reserva, p.id_forma_pagamento, "
                   + "p.valor_pago, p.data_pagamento, "
                   + "fp.descricao AS forma, c.nome_cliente, q.numero_quarto "
                   + "FROM Pagamento p "
                   + "JOIN Reserva r        ON p.id_reserva        = r.id_reserva "
                   + "JOIN Cliente c         ON r.cpf               = c.cpf "
                   + "JOIN Quarto q          ON r.cod_quarto         = q.cod_quarto "
                   + "LEFT JOIN Forma_Pagamento fp ON p.id_forma_pagamento = fp.id_forma_pagamento "
                   + "ORDER BY p.data_pagamento DESC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Pagamento p = new Pagamento();
            p.setIdPagamento(rs.getInt("id_pagamento"));
            p.setIdReserva(rs.getInt("id_reserva"));
            p.setIdFormaPagamento(rs.getInt("id_forma_pagamento"));
            p.setValorPago(rs.getDouble("valor_pago"));
            Date d = rs.getDate("data_pagamento");
            if (d != null) p.setDataPagamento(d.toLocalDate());
            p.setDescricaoForma(rs.getString("forma"));
            p.setNomeCliente(rs.getString("nome_cliente"));
            p.setNumeroQuarto(rs.getString("numero_quarto"));
            lista.add(p);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Calcular receita total (para Dashboard) ────────────────

    public double calcularReceitaTotal() throws SQLException {
        String sql = "SELECT ISNULL(SUM(valor_pago), 0) FROM Pagamento";
        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        double total = 0;
        if (rs.next()) total = rs.getDouble(1);
        rs.close();
        stmt.close();
        return total;
    }
}
