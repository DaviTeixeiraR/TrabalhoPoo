package controler;

import dao.FormaPagamentoDao;
import dao.PagamentoDao;
import dao.QuartoDao;
import dao.ReservaDao;
import javafx.scene.control.Alert;
import model.Conexao;
import model.FormaPagamento;
import model.Pagamento;
import model.Reserva;

import java.util.ArrayList;
import java.util.List;

public class PagamentoController {

    private Pagamento pagamento;

    public PagamentoController(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public PagamentoController() {}

    // ── Registrar pagamento e liberar quarto ──────────────────

    public boolean registrarPagamento(int codQuarto) {
        try {
            Conexao.conectar();

            PagamentoDao pagDao = new PagamentoDao(Conexao.conexao);
            pagDao.inserir(pagamento);

            QuartoDao quartoDao = new QuartoDao(Conexao.conexao);
            quartoDao.atualizarStatus(codQuarto, 1);

            return true;
        } catch (Exception e) {
            mostrarErro("Erro ao registrar pagamento:\n" + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Listar formas de pagamento (ComboBox) ─────────────────

    public List<FormaPagamento> listarFormasPagamento() {
        try {
            Conexao.conectar();
            FormaPagamentoDao dao = new FormaPagamentoDao(Conexao.conexao);
            return dao.listar();
        } catch (Exception e) {
            List<FormaPagamento> fallback = new ArrayList<>();
            fallback.add(new FormaPagamento(1, "Dinheiro"));
            fallback.add(new FormaPagamento(2, "Cartao de Credito"));
            fallback.add(new FormaPagamento(3, "Cartao de Debito"));
            fallback.add(new FormaPagamento(4, "PIX"));
            return fallback;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Listar todos os pagamentos (Relatorio) ────────────────

    public List<Pagamento> listarTodos() {
        try {
            Conexao.conectar();
            PagamentoDao dao = new PagamentoDao(Conexao.conexao);
            return dao.listarTodos();
        } catch (Exception e) {
            mostrarErro("Erro ao listar pagamentos:\n" + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Calcular receita total (Dashboard) ────────────────────

    public double calcularReceitaTotal() {
        try {
            Conexao.conectar();
            PagamentoDao dao = new PagamentoDao(Conexao.conexao);
            return dao.calcularReceitaTotal();
        } catch (Exception e) {
            return 0;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Alerta de erro ────────────────────────────────────────

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
