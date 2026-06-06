package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Model que representa a tabela Quarto do banco de dados.
 * cod_quarto e gerado automaticamente pelo banco (IDENTITY).
 * capacidadePessoas, precoBase e nomeTipo vem via JOIN com Tipo_Quarto.
 */
public class Quarto {

    private int    codQuarto;        // IDENTITY — gerado pelo banco, nao inserido
    private int    numeroQuarto;     // numero fisico do quarto
    private int    idTipoQuarto;
    private int    idStatus;

    // Campos extras para exibicao (JOIN nas consultas)
    private int       capacidadePessoas;
    private double    precoBase;
    private String    nomeTipo;
    private String    descricaoStatus;

    // Reservas ativas (sem pagamento, checkout >= hoje) ordenadas por checkin — preenchido em listarTodosComStatus
    private List<LocalDate[]> reservasAtivas = new ArrayList<>();

    // ── Construtores ──────────────────────────────────────────

    public Quarto() {}

    /** Construtor usado na insercao — cod_quarto e IDENTITY, nao precisa ser informado. */
    public Quarto(int numeroQuarto, int idTipoQuarto, int idStatus) {
        this.numeroQuarto  = numeroQuarto;
        this.idTipoQuarto  = idTipoQuarto;
        this.idStatus      = idStatus;
    }

    // ── Getters e Setters ─────────────────────────────────────

    public int    getCodQuarto()               { return codQuarto; }
    public void   setCodQuarto(int v)          { this.codQuarto = v; }

    public int    getNumeroQuarto()            { return numeroQuarto; }
    public void   setNumeroQuarto(int v)       { this.numeroQuarto = v; }

    public int    getIdTipoQuarto()            { return idTipoQuarto; }
    public void   setIdTipoQuarto(int v)       { this.idTipoQuarto = v; }

    public int    getIdStatus()                { return idStatus; }
    public void   setIdStatus(int v)           { this.idStatus = v; }

    public int    getCapacidadePessoas()       { return capacidadePessoas; }
    public void   setCapacidadePessoas(int v)  { this.capacidadePessoas = v; }

    public double getPrecoBase()               { return precoBase; }
    public void   setPrecoBase(double v)       { this.precoBase = v; }

    public String getNomeTipo()                { return nomeTipo; }
    public void   setNomeTipo(String v)        { this.nomeTipo = v; }

    public String getDescricaoStatus()         { return descricaoStatus; }
    public void   setDescricaoStatus(String v) { this.descricaoStatus = v; }

    public List<LocalDate[]> getReservasAtivas()                   { return reservasAtivas; }
    public void              setReservasAtivas(List<LocalDate[]> v) { this.reservasAtivas = v; }

    public void adicionarReservaAtiva(LocalDate checkin, LocalDate checkout) {
        reservasAtivas.add(new LocalDate[]{checkin, checkout});
    }

    /**
     * Status calculado a partir da primeira reserva ativa (a mais próxima),
     * sem depender do campo id_status.
     * - Sem reserva ativa      → "Disponível"
     * - Hóspede presente hoje  → "Ocupado até DD/MM"
     * - Reserva futura         → "Reservado: DD/MM a DD/MM"
     */
    public String getStatusDisplay() {
        if (reservasAtivas.isEmpty()) return "Disponível";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate checkin  = reservasAtivas.get(0)[0];
        LocalDate checkout = reservasAtivas.get(0)[1];
        LocalDate hoje = LocalDate.now();
        if (!hoje.isBefore(checkin) && !hoje.isAfter(checkout)) {
            return "Ocupado até " + checkout.format(fmt);
        }
        return "Reservado: " + checkin.format(fmt) + " a " + checkout.format(fmt);
    }

    /** Exibido automaticamente no ComboBox de selecao de quarto (TelaCheckin). */
    @Override
    public String toString() {
        return "Quarto " + numeroQuarto
             + (nomeTipo != null ? " - " + nomeTipo : "")
             + String.format(" | R$ %.2f/dia", precoBase);
    }
}
