package dao;

import java.sql.*;
import java.util.*;
import model.Quarto;

public class QuartoDao {

    private Connection conn;

    public QuartoDao(Connection conn) {
        this.conn = conn;
    }

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
        if (rs.next()) {
            idTipoGerado = rs.getInt(1);
        }
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

    public List<Quarto> listar() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT q.cod_quarto, q.numero_quarto, q.id_status, "
                   + "t.capacidade_pessoas, t.preco_base "
                   + "FROM Quarto q "
                   + "JOIN Tipo_Quarto t ON q.id_tipo_quarto = t.id_tipo_quarto";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            Quarto quarto = new Quarto();
            quarto.setCodQuarto(rs.getString("cod_quarto"));
            quarto.setNumeroQuarto(rs.getString("numero_quarto"));
			quarto.setIdStatus(rs.getInt("id_status"));
			quarto.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
			quarto.setPrecoBase(rs.getDouble("preco_base"));
			lista.add(quarto);
		}

		rs.close();
		stmt.close();
		return lista;
	}
}