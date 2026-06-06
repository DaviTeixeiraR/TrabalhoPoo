package dao;

import java.sql.*;
import java.util.*;
import model.TipoQuarto;

public class TipoQuartoDao {

    private Connection conn;

    public TipoQuartoDao(Connection conn) {
        this.conn = conn;
    }

    public List<TipoQuarto> listar() throws SQLException {
        List<TipoQuarto> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_quarto, nome_tipo, capacidade_pessoas, preco_base "
                   + "FROM Tipo_Quarto ORDER BY nome_tipo";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            TipoQuarto t = new TipoQuarto();
            t.setIdTipoQuarto(rs.getInt("id_tipo_quarto"));
            t.setNomeTipo(rs.getString("nome_tipo"));
            t.setCapacidadePessoas(rs.getInt("capacidade_pessoas"));
            t.setPrecoBase(rs.getDouble("preco_base"));
            lista.add(t);
        }
        rs.close();
        stmt.close();
        return lista;
    }
}
