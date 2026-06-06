package controler;

import dao.QuartoDao;
import dao.ReservaDao;
import javafx.scene.control.Alert;
import model.Conexao;
import model.Reserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsável pelas operações de Reserva.
 * Segue o mesmo padrão dos controllers já existentes no projeto.
 */
public class ReservaController {

    private Reserva reserva;

    public ReservaController(Reserva reserva) {
        this.reserva = reserva;
    }

    public ReservaController() {}

    // ── Salvar nova reserva e marcar quarto como ocupado ──────

    public boolean salvarReserva() {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            dao.inserir(reserva);

            // Atualiza status do quarto para Ocupado (id_status = 2)
            QuartoDao quartoDao = new QuartoDao(Conexao.conexao);
            quartoDao.atualizarStatus(reserva.getCodQuarto(), 2);

            return true;
        } catch (Exception e) {
            mostrarErro("Erro ao salvar reserva:\n" + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Listar reservas ativas (sem pagamento) ─────────────────

    public List<Reserva> listarReservasAtivas() {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            return dao.listarAtivas();
        } catch (Exception e) {
            mostrarErro("Erro ao listar reservas:\n" + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Listar todas as reservas com detalhes (Relatório) ─────

    public List<Reserva> listarTodasReservas() {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            return dao.listarTodasComDetalhes();
        } catch (Exception e) {
            mostrarErro("Erro ao listar reservas:\n" + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Contar reservas ativas (Dashboard) ────────────────────

    public int contarAtivas() {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            return dao.contarAtivas();
        } catch (Exception e) {
            return 0;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Verificar conflito de período para um quarto ──────────

    public boolean existeConflito(int codQuarto, LocalDate checkin, LocalDate checkout) {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            return dao.existeConflito(codQuarto, checkin, checkout);
        } catch (Exception e) {
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Verificar reserva duplicada ───────────────────────────

    public boolean existeReservaDuplicada(String cpf, int codQuarto, LocalDate checkin, LocalDate checkout) {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            return dao.existeReservaDuplicada(cpf, codQuarto, checkin, checkout);
        } catch (Exception e) {
            return false;
        } finally {
            Conexao.desconectar();
        }
    }

    // ── Contar reservas de hoje (Dashboard) ───────────────────

    public int contarHoje() {
        try {
            Conexao.conectar();
            ReservaDao dao = new ReservaDao(Conexao.conexao);
            return dao.contarHoje();
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
