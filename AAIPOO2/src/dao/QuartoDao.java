package dao;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import model.Quarto;

/**
 * DAO para operacoes na tabela Quarto.
 * cod_quarto e IDENTITY — nunca e fornecido na insercao.
 */
public class QuartoDao {

    private Connection conn;

    public QuartoDao(Connection conn) {
        this.conn = conn;
    }

    // ── Inserir ───────────────────────────────────────────────

    public void inserir(Quarto quarto) throws SQLException {
        String sql = "INSERT INTO Quarto (numero_quarto, id_tipo_quarto, id_status) "
                   + "VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, quarto.getNumeroQuarto());
        stmt.setInt(2, quarto.getIdTipoQuarto());
        stmt.setInt(3, quarto.getIdStatus());
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Listar todos ──────────────────────────────────────────

    public List<Quarto> listar() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, q.id_tipo_quarto, "
                   + "t.capacidade_pessoas, t.preco_base "
                   + "FROM Quarto q "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto = t.id_tipo_quarto";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Quarto q = new Quarto();
            q.setCodQuarto(rs.getInt("cod_quarto"));
            q.setNumeroQuarto(rs.getInt("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            lista.add(q);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Listar todos com status e tipo (TelaSelecaoQuartos) ───
    // Duas queries: a primeira traz os dados base dos quartos; a segunda traz
    // TODAS as reservas ativas (sem pagamento, checkout >= hoje) de cada quarto,
    // ordenadas por checkin. O status exibido é derivado dessas datas.

    public List<Quarto> listarTodosComStatus() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        Map<Integer, Quarto> porCod = new HashMap<>();

        String sqlQuartos = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, q.id_tipo_quarto, "
                          + "t.capacidade_pessoas, t.preco_base, t.nome_tipo, "
                          + "s.descricao AS desc_status "
                          + "FROM Quarto q "
                          + "JOIN Tipo_Quarto t   ON q.id_tipo_quarto = t.id_tipo_quarto "
                          + "JOIN Status_Quarto s ON q.id_status      = s.id_status "
                          + "ORDER BY q.numero_quarto";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sqlQuartos);
        while (rs.next()) {
            Quarto q = new Quarto();
            q.setCodQuarto(rs.getInt("cod_quarto"));
            q.setNumeroQuarto(rs.getInt("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            q.setNomeTipo(rs.getString("nome_tipo"));
            q.setDescricaoStatus(rs.getString("desc_status"));
            lista.add(q);
            porCod.put(q.getCodQuarto(), q);
        }
        rs.close();
        stmt.close();

        // Segunda query: todas as reservas ativas futuras/presentes, agrupadas por quarto
        String sqlReservas = "SELECT r.cod_quarto, r.data_checkin, r.data_checkout "
                           + "FROM Reserva r "
                           + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                           + "WHERE p.id_pagamento IS NULL "
                           + "  AND r.data_checkout >= CAST(GETDATE() AS DATE) "
                           + "ORDER BY r.cod_quarto, r.data_checkin ASC";

        Statement stmt2 = conn.createStatement();
        ResultSet rs2   = stmt2.executeQuery(sqlReservas);
        while (rs2.next()) {
            Quarto q = porCod.get(rs2.getInt("cod_quarto"));
            if (q != null) {
                q.adicionarReservaAtiva(
                    rs2.getDate("data_checkin").toLocalDate(),
                    rs2.getDate("data_checkout").toLocalDate()
                );
            }
        }
        rs2.close();
        stmt2.close();

        return lista;
    }

    // ── Listar quartos disponiveis (id_status = 1) ────────────

    public List<Quarto> listarDisponiveis() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, q.id_tipo_quarto, "
                   + "t.capacidade_pessoas, t.preco_base, t.nome_tipo "
                   + "FROM Quarto q "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto = t.id_tipo_quarto "
                   + "WHERE q.id_status = 1 "
                   + "ORDER BY q.numero_quarto";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Quarto q = new Quarto();
            q.setCodQuarto(rs.getInt("cod_quarto"));
            q.setNumeroQuarto(rs.getInt("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            q.setNomeTipo(rs.getString("nome_tipo"));
            q.setDescricaoStatus("Disponivel");
            lista.add(q);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Listar quartos sem conflito de reserva para o período ─
    // Ignora o campo id_status e verifica diretamente a tabela Reserva.
    // Dois períodos se sobrepõem quando: checkin1 < checkout2 AND checkout1 > checkin2

    public List<Quarto> listarDisponiveisParaPeriodo(LocalDate checkin, LocalDate checkout) throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, q.id_tipo_quarto, "
                   + "t.capacidade_pessoas, t.preco_base, t.nome_tipo "
                   + "FROM Quarto q "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto = t.id_tipo_quarto "
                   + "WHERE NOT EXISTS ( "
                   + "  SELECT 1 FROM Reserva r "
                   + "  LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "  WHERE r.cod_quarto = q.cod_quarto "
                   + "    AND p.id_pagamento IS NULL "
                   + "    AND r.data_checkin < ? "
                   + "    AND r.data_checkout > ? "
                   + ") "
                   + "ORDER BY q.numero_quarto";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDate(1, Date.valueOf(checkout));
        stmt.setDate(2, Date.valueOf(checkin));
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Quarto q = new Quarto();
            q.setCodQuarto(rs.getInt("cod_quarto"));
            q.setNumeroQuarto(rs.getInt("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            q.setNomeTipo(rs.getString("nome_tipo"));
            q.setDescricaoStatus("Disponivel");
            lista.add(q);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Atualizar status ──────────────────────────────────────

    public void atualizarStatus(int codQuarto, int idStatus) throws SQLException {
        String sql = "UPDATE Quarto SET id_status = ? WHERE cod_quarto = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idStatus);
        stmt.setInt(2, codQuarto);
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Atualizar dados do quarto ─────────────────────────────

    public void atualizar(Quarto quarto) throws SQLException {
        String sql = "UPDATE Quarto SET numero_quarto = ?, id_tipo_quarto = ? WHERE cod_quarto = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, quarto.getNumeroQuarto());
        stmt.setInt(2, quarto.getIdTipoQuarto());
        stmt.setInt(3, quarto.getCodQuarto());
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Deletar quarto ────────────────────────────────────────

    /**
     * Exclui o quarto fisicamente, mas SOMENTE se não houver nenhuma reserva
     * (ativa ou histórica) vinculada a ele.
     * Dessa forma a receita e o histórico financeiro nunca são afetados.
     * Lança {@link IllegalStateException}("HAS_HISTORY") se existir qualquer reserva.
     */
    public void deletar(int codQuarto) throws SQLException {
        // Verifica se há qualquer reserva (ativa OU já finalizada/paga)
        String sqlCheck = "SELECT COUNT(*) FROM Reserva WHERE cod_quarto = ?";
        PreparedStatement checkStmt = conn.prepareStatement(sqlCheck);
        checkStmt.setInt(1, codQuarto);
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        int totalReservas = rs.getInt(1);
        rs.close();
        checkStmt.close();

        if (totalReservas > 0) {
            // Impossível excluir sem schema change — bloqueia para preservar receita
            throw new IllegalStateException("HAS_HISTORY");
        }

        // Sem histórico → exclusão física segura (sem impacto na receita)
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Quarto WHERE cod_quarto = ?");
        stmt.setInt(1, codQuarto);
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Contar por status (Dashboard) ─────────────────────────
    // Mantido para compatibilidade com código legado que ainda o usa.

    public int contarPorStatus(int idStatus) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Quarto WHERE id_status = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idStatus);
        ResultSet rs = stmt.executeQuery();
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    // ── Contagens baseadas em reservas (Dashboard — lógica correta) ──

    /** Quartos sem nenhuma reserva ativa presente ou futura. */
    public int contarDisponiveis() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Quarto q "
                   + "WHERE NOT EXISTS ( "
                   + "  SELECT 1 FROM Reserva r "
                   + "  LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "  WHERE r.cod_quarto = q.cod_quarto "
                   + "    AND p.id_pagamento IS NULL "
                   + "    AND r.data_checkout >= CAST(GETDATE() AS DATE) "
                   + ")";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); stmt.close();
        return count;
    }

    /** Quartos com hóspede presente hoje (checkin <= hoje <= checkout, sem pagamento). */
    public int contarOcupados() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT q.cod_quarto) FROM Quarto q "
                   + "JOIN Reserva r ON r.cod_quarto = q.cod_quarto "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE p.id_pagamento IS NULL "
                   + "  AND r.data_checkin  <= CAST(GETDATE() AS DATE) "
                   + "  AND r.data_checkout >= CAST(GETDATE() AS DATE)";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); stmt.close();
        return count;
    }

    /** Quartos com ao menos uma reserva futura ativa (checkin > hoje), independente de estar ocupado agora. */
    public int contarReservados() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT r.cod_quarto) FROM Reserva r "
                   + "LEFT JOIN Pagamento p ON r.id_reserva = p.id_reserva "
                   + "WHERE p.id_pagamento IS NULL "
                   + "  AND r.data_checkin > CAST(GETDATE() AS DATE)";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); stmt.close();
        return count;
    }
}
