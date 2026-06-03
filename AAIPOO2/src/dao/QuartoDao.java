package dao;

import java.sql.*;
import java.util.*;
import model.Quarto;

/**
 * DAO para operações na tabela Quarto.
 * Inclui os métodos originais + listarDisponiveis, listarTodosComStatus
 * e atualizarStatus (necessários para Check-in / Checkout).
 */
public class QuartoDao {

    private Connection conn;

    public QuartoDao(Connection conn) {
        this.conn = conn;
    }

    // ── Inserir (original mantido) ────────────────────────────

    public void inserir(Quarto quarto) throws SQLException {
        // 1. Insere em Tipo_Quarto e recupera o id gerado
        String sqlTipo = "INSERT INTO Tipo_Quarto (nome_tipo, capacidade_pessoas, preco_base) "
                       + "VALUES (?, ?, ?)";
        PreparedStatement stmtTipo = conn.prepareStatement(sqlTipo, Statement.RETURN_GENERATED_KEYS);
        stmtTipo.setString(1, quarto.getNumeroQuarto());
        stmtTipo.setInt(2, quarto.getCapacidadePessoas());
        stmtTipo.setDouble(3, quarto.getPrecoBase());
        stmtTipo.executeUpdate();

        ResultSet rs = stmtTipo.getGeneratedKeys();
        int idTipoGerado = 0;
        if (rs.next()) idTipoGerado = rs.getInt(1);
        rs.close();
        stmtTipo.close();

        // 2. Insere em Quarto usando o id_tipo_quarto gerado
        String sqlQuarto = "INSERT INTO Quarto (cod_quarto, numero_quarto, id_tipo_quarto, id_status) "
                         + "VALUES (?, ?, ?, ?)";
        PreparedStatement stmtQuarto = conn.prepareStatement(sqlQuarto);
        stmtQuarto.setString(1, quarto.getCodQuarto());
        stmtQuarto.setString(2, quarto.getNumeroQuarto());
        stmtQuarto.setInt(3, idTipoGerado);
        stmtQuarto.setInt(4, quarto.getIdStatus());
        stmtQuarto.executeUpdate();
        stmtQuarto.close();
    }

    // ── Listar todos (original mantido) ──────────────────────

    public List<Quarto> listar() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, "
                   + "t.capacidade_pessoas, t.preco_base "
                   + "FROM Quarto q "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto = t.id_tipo_quarto";

        Statement stmt  = conn.createStatement();
        ResultSet rs    = stmt.executeQuery(sql);
        while (rs.next()) {
            Quarto q = new Quarto();
            q.setCodQuarto(rs.getString("cod_quarto"));
            q.setNumeroQuarto(rs.getString("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            lista.add(q);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Listar todos com status e tipo (para TelaSelecaoQuartos) ──

    public List<Quarto> listarTodosComStatus() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, q.id_tipo_quarto, "
                   + "t.capacidade_pessoas, t.preco_base, t.nome_tipo, "
                   + "s.descricao AS desc_status "
                   + "FROM Quarto q "
                   + "JOIN Tipo_Quarto t  ON q.id_tipo_quarto = t.id_tipo_quarto "
                   + "JOIN Status_Quarto s ON q.id_status     = s.id_status "
                   + "ORDER BY q.numero_quarto";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            Quarto q = new Quarto();
            q.setCodQuarto(rs.getString("cod_quarto"));
            q.setNumeroQuarto(rs.getString("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            q.setNomeTipo(rs.getString("nome_tipo"));
            q.setDescricaoStatus(rs.getString("desc_status"));
            lista.add(q);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Listar apenas quartos disponíveis (id_status = 1) ─────

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
            q.setCodQuarto(rs.getString("cod_quarto"));
            q.setNumeroQuarto(rs.getString("numero_quarto"));
            q.setIdStatus(rs.getInt("id_status"));
            q.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            q.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            q.setPrecoBase(rs.getDouble("preco_base"));
            q.setNomeTipo(rs.getString("nome_tipo"));
            q.setDescricaoStatus("Disponível");
            lista.add(q);
        }
        rs.close();
        stmt.close();
        return lista;
    }

    // ── Atualizar status do quarto ─────────────────────────────

    /**
     * Atualiza o status de um quarto.
     * id_status = 1 → Disponível
     * id_status = 2 → Ocupado
     * id_status = 3 → Manutenção
     */
    public void atualizarStatus(String codQuarto, int idStatus) throws SQLException {
        String sql = "UPDATE Quarto SET id_status = ? WHERE cod_quarto = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idStatus);
        stmt.setString(2, codQuarto);
        stmt.executeUpdate();
        stmt.close();
    }

    // ── Contar quartos por status (para Dashboard) ─────────────

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
}