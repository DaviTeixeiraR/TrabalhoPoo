package controler;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Conexao;
import model.DadosPessoais;
import dao.DadosPesDao;

public class DadosPesController {
    private DadosPesDao dao;
	private DadosPessoais dadosPessoais;
	
	public DadosPesController(DadosPessoais dadosPessoais) {
		this.dadosPessoais = dadosPessoais;
	}
	
    @FXML
    public void salvarDadosPessoais() {
        try {
            Conexao.conectar(); // sua classe de conexão
            dao = new DadosPesDao(Conexao.conexao);
            dao.inserir(dadosPessoais);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Cadastro salvo com sucesso!");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	Conexao.desconectar();
        }
    }
}

