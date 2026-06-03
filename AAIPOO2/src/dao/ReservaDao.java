package dao;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import model.Reserva;

/**
 * DAO para operações na tabela Reserva.
 * Inclui métodos para inserir, listar ativas (sem pagamento),
 * listar todas com detalhes (para Relatório) e buscar por ID.
 */
public class ReservaDao {

    private Connection conn;

    public ReservaDao(Connection conn) {
        this.conn = conn;
    }

    // ── Inserir nova reserva ───────────────────────────────────

    public void inserir(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO Reserva (cpf, cod_quarto, data_checkin, data_checkout, data_hora_reserva) "
                   + "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, reserva.getCpf());
        stmt.setString(2, reserva.getCodQuarto());
        stmt.setDate(3, Date.valueOf(reserva.getDataCheckin()));
        stmt.setDate(4, Date.valueOf(reserva.getDataCheckout()));
        stmt.setTimestamp(5, Timestamp.valueOf(reserva.getDataHoraReserva()));
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) reserva.setIdReserva(rs.getInt(1));
        rs.close();
        stmt.close();
    }

    // ── Listar reservas ativas (sem pagamento registrado) ──────

    public List<Reserva> listarAtivas() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.cpf, r.cod_quarto, "
                   + "r.data_checkin, r.data_checkout, r.data_hora_reserva, "
                   + "c.nome_cliente, q.numero_quarto, t.preco_base "
                   + "FROM Reserva r "
                   + "JOIN Cliente c      ON r.cpf       = c.cpf "
                   + "JOIN Quarto q       ON r.cod_quarto = q.cod_quarto "
                   + "JOIN Tipo_Quarto t  ON q.id_tipo_quarto = t.id_tipo_quarto "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE p.id_pagamento IS NULL "
                   + "ORDER BY r.data_checkin DESC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Reserva r = mapearReserva(rs);
            lista.add(r);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Listar todas com detalhes completos (para Relatório) ───

    public List<Reserva> listarTodasComDetalhes() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.cpf, r.cod_quarto, "
                   + "r.data_checkin, r.data_checkout, r.data_hora_reserva, "
                   + "c.nome_cliente, q.numero_quarto, t.preco_base, "
                   + "CASE WHEN p.id_pagamento IS NULL THEN 'Pendente' ELSE 'Pago' END AS status_pag "
                   + "FROM Reserva r "
                   + "JOIN Cliente c      ON r.cpf             = c.cpf "
                   + "JOIN Quarto q       ON r.cod_quarto       = q.cod_quarto "
                   + "JOIN Tipo_Quarto t  ON q.id_tipo_quarto   = t.id_tipo_quarto "
                   + "LEFT JOIN Pagamento p ON r.id_reserva    = p.id_reserva "
                   + "ORDER BY r.data_checkin DESC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Reserva r = mapearReserva(rs);
            try { r.setNomeStatus(rs.getString("status_pag")); } catch (SQLException ignored) {}
            lista.add(r);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Buscar por ID ──────────────────────────────────────────

    public Reserva buscarPorId(int idReserva) throws SQLException {
        String sql = "SELECT r.id_reserva, r.cpf, r.cod_quarto, "
                   + "r.data_checkin, r.data_checkout, r.data_hora_reserva, "
                   + "c.nome_cliente, q.numero_quarto, t.preco_base "
                   + "FROM Reserva r "
                   + "JOIN Cliente c     ON r.cpf             = c.cpf "
                   + "JOIN Quarto q      ON r.cod_quarto       = q.cod_quarto "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto   = t.id_tipo_quarto "
                   + "WHERE r.id_reserva = ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idReserva);
        ResultSet rs = stmt.executeQuery();
        Reserva reserva = null;
        if (rs.next()) reserva = mapearReserva(rs);
        rs.close();
        stmt.close();
        return reserva;
    }

    // ── Contar reservas ativas (para Dashboard) ────────────────

    public int contarAtivas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva r "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE p.id_pagamento IS NULL";
        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    // ── Contar reservas de hoje (para Dashboard) ───────────────

    public int contarHoje() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva WHERE CAST(data_hora_reserva AS DATE) = CAST(GETDATE() AS DATE)";
        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    // ── Método auxiliar de mapeamento ─────────────────────────

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setCpf(rs.getString("cpf"));
        r.setCodQuarto(rs.getString("cod_quarto"));

        Date checkin  = rs.getDate("data_checkin");
        Date checkout = rs.getDate("data_checkout");
        if (checkin  != null) r.setDataCheckin(checkin.toLocalDate());
        if (checkout != null) r.setDataCheckout(checkout.toLocalDate());

        Timestamp ts = rs.getTimestamp("data_hora_reserva");
        if (ts != null) r.setDataHoraReserva(ts.toLocalDateTime());

        try { r.setNomeCliente(rs.getString("nome_cliente")); } catch (SQLException ignored) {}
        try { r.setNumeroQuarto(rs.getString("numero_quarto")); } catch (SQLException ignored) {}
        try { r.setPrecoBase(rs.getDouble("preco_base")); } catch (SQLException ignored) {}
        return r;
    }
}
