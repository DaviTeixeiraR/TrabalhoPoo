package dao;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import model.DadosPessoais;

public class DadosPesDao {

    private Connection conn;

    public DadosPesDao(Connection conn) {
        this.conn = conn;
    }

    public void inserir(DadosPessoais dadosPessoais) throws SQLException {
        String sql = "INSERT INTO Cliente (cpf, nome_cliente, telefone, email, data_nascimento) "
                   + "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, dadosPessoais.getCpf());
        stmt.setString(2, dadosPessoais.getNomeCliente());
        stmt.setString(3, dadosPessoais.getTelefone());
        stmt.setString(4, dadosPessoais.getEmail());
        stmt.setDate(5, Date.valueOf(dadosPessoais.getDataNascimento()));

        stmt.executeUpdate();
        stmt.close();
    }

    public List<DadosPessoais> listar() throws SQLException {
        List<DadosPessoais> lista = new ArrayList<>();
        String sql = "SELECT * FROM Cliente";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            DadosPessoais dadosPessoais = new DadosPessoais();
            dadosPessoais.setCpf(rs.getString("cpf"));
            dadosPessoais.setNomeCliente(rs.getString("nome_cliente"));
            dadosPessoais.setTelefone(rs.getString("telefone"));
            dadosPessoais.setEmail(rs.getString("email"));
            dadosPessoais.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());

            lista.add(dadosPessoais);
        }

        rs.close();
        stmt.close();
        return lista;
    }

    public DadosPessoais buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM Cliente WHERE cpf = ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cpf);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            DadosPessoais dadosPessoais = new DadosPessoais();
            dadosPessoais.setCpf(rs.getString("cpf"));
            dadosPessoais.setNomeCliente(rs.getString("nome_cliente"));
            dadosPessoais.setTelefone(rs.getString("telefone"));
            dadosPessoais.setEmail(rs.getString("email"));
            dadosPessoais.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());

            rs.close();
            stmt.close();
            return dadosPessoais;
        }

        rs.close();
        stmt.close();
        return null;
    }

    public void atualizar(DadosPessoais dadosPessoais) throws SQLException {
        String sql = "UPDATE Cliente SET nome_cliente = ?, telefone = ?, email = ?, data_nascimento = ? "
                   + "WHERE cpf = ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, dadosPessoais.getNomeCliente());
        stmt.setString(2, dadosPessoais.getTelefone());
        stmt.setString(3, dadosPessoais.getEmail());
        stmt.setDate(4, Date.valueOf(dadosPessoais.getDataNascimento()));
        stmt.setString(5, dadosPessoais.getCpf());

        stmt.executeUpdate();
        stmt.close();
    }

    public void deletar(String cpf) throws SQLException {
        String sql = "DELETE FROM Cliente WHERE cpf = ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cpf);

        stmt.executeUpdate();
        stmt.close();
    }
}