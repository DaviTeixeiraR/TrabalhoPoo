package controler;

import dao.QuartoDao;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import model.Conexao;
import model.Quarto;

public class QuartoController {

    private Quarto quarto;

    public QuartoController(Quarto quarto) {
        this.quarto = quarto;
    }

    @FXML
    public void salvarQuarto() {
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            dao.inserir(quarto);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Quarto salvo com sucesso!");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conexao.desconectar();
        }
    }
}