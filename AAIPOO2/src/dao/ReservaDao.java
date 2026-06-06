package dao;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import model.Reserva;

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
        stmt.setInt(2, reserva.getCodQuarto());
        stmt.setDate(3, Date.valueOf(reserva.getDataCheckin()));
        stmt.setDate(4, Date.valueOf(reserva.getDataCheckout()));
        stmt.setTimestamp(5, Timestamp.valueOf(reserva.getDataHoraReserva()));
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) reserva.setIdReserva(rs.getInt(1));
        rs.close();
        stmt.close();
    }

    // ── Listar reservas ativas (sem pagamento) ─────────────────

    public List<Reserva> listarAtivas() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.cpf, r.cod_quarto, "
                   + "r.data_checkin, r.data_checkout, r.data_hora_reserva, "
                   + "c.nome_cliente, q.numero_quarto, t.preco_base "
                   + "FROM Reserva r "
                   + "JOIN Cliente c      ON r.cpf        = c.cpf "
                   + "JOIN Quarto q       ON r.cod_quarto  = q.cod_quarto "
                   + "JOIN Tipo_Quarto t  ON q.id_tipo_quarto = t.id_tipo_quarto "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE p.id_pagamento IS NULL "
                   + "ORDER BY r.data_checkin DESC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) lista.add(mapearReserva(rs));
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Listar todas com detalhes (Relatorio) ─────────────────

    public List<Reserva> listarTodasComDetalhes() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.cpf, r.cod_quarto, "
                   + "r.data_checkin, r.data_checkout, r.data_hora_reserva, "
                   + "c.nome_cliente, q.numero_quarto, t.preco_base, "
                   + "CASE WHEN p.id_pagamento IS NULL THEN 'Pendente' ELSE 'Pago' END AS status_pag, "
                   + "fp.descricao AS forma_pagamento "
                   + "FROM Reserva r "
                   + "JOIN Cliente c           ON r.cpf               = c.cpf "
                   + "JOIN Quarto q            ON r.cod_quarto         = q.cod_quarto "
                   + "JOIN Tipo_Quarto t       ON q.id_tipo_quarto     = t.id_tipo_quarto "
                   + "LEFT JOIN Pagamento p    ON r.id_reserva         = p.id_reserva "
                   + "LEFT JOIN Forma_Pagamento fp ON p.id_forma_pagamento = fp.id_forma_pagamento "
                   + "ORDER BY r.data_checkin DESC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Reserva r = mapearReserva(rs);
            try { r.setNomeStatus(rs.getString("status_pag")); }       catch (SQLException ignored) {}
            try { r.setFormaPagamento(rs.getString("forma_pagamento")); } catch (SQLException ignored) {}
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
                   + "JOIN Cliente c     ON r.cpf            = c.cpf "
                   + "JOIN Quarto q      ON r.cod_quarto      = q.cod_quarto "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto  = t.id_tipo_quarto "
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

    // ── Contar ativas (Dashboard) ──────────────────────────────

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

    // ── Contar reservas de hoje (Dashboard) ───────────────────

    public int contarHoje() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva "
                   + "WHERE CAST(data_hora_reserva AS DATE) = CAST(GETDATE() AS DATE)";
        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    // ── Verificar conflito de datas para um quarto ────────────
    // Dois períodos se sobrepõem quando: checkin1 < checkout2 AND checkout1 > checkin2

    public boolean existeConflito(int codQuarto, LocalDate checkin, LocalDate checkout) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva r "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE r.cod_quarto = ? "
                   + "  AND p.id_pagamento IS NULL "
                   + "  AND r.data_checkin < ? "
                   + "  AND r.data_checkout > ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, codQuarto);
        stmt.setDate(2, Date.valueOf(checkout));
        stmt.setDate(3, Date.valueOf(checkin));
        ResultSet rs = stmt.executeQuery();
        boolean conflito = rs.next() && rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return conflito;
    }

    // ── Verificar reserva duplicada (mesmo cliente/quarto/datas) ─

    public boolean existeReservaDuplicada(String cpf, int codQuarto, LocalDate checkin, LocalDate checkout) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva r "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE r.cpf = ? AND r.cod_quarto = ? AND r.data_checkin = ? AND r.data_checkout = ? "
                   + "  AND p.id_pagamento IS NULL";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cpf);
        stmt.setInt(2, codQuarto);
        stmt.setDate(3, Date.valueOf(checkin));
        stmt.setDate(4, Date.valueOf(checkout));
        ResultSet rs = stmt.executeQuery();
        boolean duplicada = rs.next() && rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return duplicada;
    }

    // ── Mapeamento interno ────────────────────────────────────

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setCpf(rs.getString("cpf"));
        r.setCodQuarto(rs.getInt("cod_quarto"));

        Date checkin  = rs.getDate("data_checkin");
        Date checkout = rs.getDate("data_checkout");
        if (checkin  != null) r.setDataCheckin(checkin.toLocalDate());
        if (checkout != null) r.setDataCheckout(checkout.toLocalDate());

        Timestamp ts = rs.getTimestamp("data_hora_reserva");
        if (ts != null) r.setDataHoraReserva(ts.toLocalDateTime());

        try { r.setNomeCliente(rs.getString("nome_cliente")); }  catch (SQLException ignored) {}
        try { r.setNumeroQuarto(rs.getString("numero_quarto")); } catch (SQLException ignored) {}
        try { r.setPrecoBase(rs.getDouble("preco_base")); }       catch (SQLException ignored) {}
        return r;
    }
}
