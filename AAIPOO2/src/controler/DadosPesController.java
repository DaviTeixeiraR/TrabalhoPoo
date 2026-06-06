package controler;

import dao.DadosPesDao;
import model.Conexao;
import model.DadosPessoais;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller para operações CRUD de clientes (DadosPessoais).
 * Cada método retorna boolean (true = sucesso) — a view é responsável pelo feedback visual.
 */
public class DadosPesController {

    private DadosPesDao       dao;
    private DadosPessoais     dadosPessoais;
    /** Motivo da última falha: "ACTIVE_RESERVATIONS" ou "DB_ERROR". Vazio = sem erro. */
    private String            ultimoErro = "";

    public DadosPesController() {}

    public DadosPesController(DadosPessoais dadosPessoais) {
        this.dadosPessoais = dadosPessoais;
    }

    public String getUltimoErro() { return ultimoErro; }

    // ── Inserir ───────────────────────────────────────────────

    public boolean inserir() {
        try {
            Conexao.conectar();
            dao = new DadosPesDao(Conexao.conexao);
            dao.inserir(dadosPessoais);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    /** Mantido por compatibilidade com código legado. */
    public void salvarDadosPessoais() {
        inserir();
    }

    // ── Atualizar ─────────────────────────────────────────────

    public boolean atualizar() {
        try {
            Conexao.conectar();
            dao = new DadosPesDao(Conexao.conexao);
            dao.atualizar(dadosPessoais);
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
     * Remove o cliente fisicamente, mas apenas se não houver histórico de reservas.
     * Retorna false se houver qualquer reserva (ativa ou paga) — consultar {@link #getUltimoErro()}.
     * ultimoErro = "HAS_HISTORY" → cliente tem histórico; "DB_ERROR" → falha de banco.
     */
    public boolean deletar(String cpf) {
        ultimoErro = "";
        try {
            Conexao.conectar();
            dao = new DadosPesDao(Conexao.conexao);
            dao.deletar(cpf);
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

    public List<DadosPessoais> listar() {
        try {
            Conexao.conectar();
            dao = new DadosPesDao(Conexao.conexao);
            return dao.listar();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            Conexao.desconectar();
        }
    }
}
