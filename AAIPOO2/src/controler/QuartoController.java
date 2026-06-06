package controler;

import dao.QuartoDao;
import model.Conexao;
import model.Quarto;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller para operações CRUD de quartos.
 * Cada método retorna boolean (true = sucesso) — a view é responsável pelo feedback visual.
 */
public class QuartoController {

    private Quarto quarto;
    /** Motivo da última falha: "ACTIVE_RESERVATIONS" ou "DB_ERROR". Vazio = sem erro. */
    private String ultimoErro = "";

    public QuartoController() {}

    public QuartoController(Quarto quarto) {
        this.quarto = quarto;
    }

    public String getUltimoErro() { return ultimoErro; }

    // ── Inserir ───────────────────────────────────────────────

    public boolean inserir() {
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            dao.inserir(quarto);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    /** Mantido por compatibilidade com código legado. */
    public void salvarQuarto() {
        inserir();
    }

    // ── Atualizar ─────────────────────────────────────────────

    public boolean atualizar() {
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            dao.atualizar(quarto);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Deletar ───────────────────────────────────────────────

    /**
     * Remove o quarto fisicamente, mas apenas se não houver histórico de reservas.
     * Retorna false se houver qualquer reserva (ativa ou paga) — consultar {@link #getUltimoErro()}.
     * ultimoErro = "HAS_HISTORY" → quarto tem histórico; "DB_ERROR" → falha de banco.
     */
    public boolean deletar(int codQuarto) {
        ultimoErro = "";
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            dao.deletar(codQuarto);
            return true;
        } catch (IllegalStateException e) {
            ultimoErro = e.getMessage(); // "ACTIVE_RESERVATIONS"
            return false;
        } catch (Exception e) {
            ultimoErro = "DB_ERROR";
            e.printStackTrace();
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Listar ────────────────────────────────────────────────

    /** Retorna todos os quartos com status e tipo (via JOIN). */
    public List<Quarto> listar() {
        try {
            Conexao.conectar();
            QuartoDao dao = new QuartoDao(Conexao.conexao);
            return dao.listarTodosComStatus();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            Conexao.desconectar();
        }
    }
}
