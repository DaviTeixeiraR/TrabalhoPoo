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

/**
 * Controller responsável pelas operações de Pagamento e Checkout.
 * Ao confirmar pagamento, também libera o quarto (status = Disponível).
 */
public class PagamentoController {

    private Pagamento pagamento;

    public PagamentoController(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public PagamentoController() {}

    // ── Registrar pagamento e liberar quarto ──────────────────

    /**
     * Registra o pagamento no banco e atualiza o status do quarto para Disponível.
     * @param codQuarto  código do quarto a ser liberado
     * @return true se operação bem-sucedida
     */
    public boolean registrarPagamento(String codQuarto) {
        try {
            Conexao.conectar();

            // 1. Inserir pagamento
            PagamentoDao pagDao = new PagamentoDao(Conexao.conexao);
            pagDao.inserir(pagamento);

            // 2. Liberar quarto (id_status = 1 → Disponível)
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

    // ── Listar formas de pagamento (para ComboBox) ─────────────

    public List<FormaPagamento> listarFormasPagamento() {
        try {
            Conexao.conectar();
            FormaPagamentoDao dao = new FormaPagamentoDao(Conexao.conexao);
            return dao.listar();
        } catch (Exception e) {
            // Fallback com opções padrão
            List<FormaPagamento> fallback = new ArrayList<>();
            fallback.add(new FormaPagamento(1, "Dinheiro"));
            fallback.add(new FormaPagamento(2, "Cartão de Crédito"));
            fallback.add(new FormaPagamento(3, "Cartão de Débito"));
            fallback.add(new FormaPagamento(4, "PIX"));
            return fallback;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Listar todos os pagamentos (para Relatório) ────────────

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

    // ── Método auxiliar de alerta de erro ─────────────────────

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
