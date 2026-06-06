package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Model que representa a tabela Reserva do banco de dados.
 * Campos extras (nomeCliente, numeroQuarto, precoBase) sao usados
 * apenas para exibicao em tela, sem persistencia.
 */
public class Reserva {

    private int           idReserva;
    private String        cpf;
    private int           codQuarto;       // int — FK para Quarto.cod_quarto (IDENTITY)
    private LocalDate     dataCheckin;
    private LocalDate     dataCheckout;
    private LocalDateTime dataHoraReserva;

    // Campos extras para exibicao (JOIN nas consultas)
    private String nomeCliente;
    private String numeroQuarto;    // String para exibicao (lido via getString do JOIN)
    private double precoBase;
    private String nomeStatus;
    private String formaPagamento;  // descrição da Forma_Pagamento (null quando pendente)

    // ── Construtores ──────────────────────────────────────────

    public Reserva() {}

    public Reserva(String cpf, int codQuarto,
                   LocalDate dataCheckin, LocalDate dataCheckout) {
        this.cpf             = cpf;
        this.codQuarto       = codQuarto;
        this.dataCheckin     = dataCheckin;
        this.dataCheckout    = dataCheckout;
        this.dataHoraReserva = LocalDateTime.now();
    }

    // ── Metodos utilitarios ───────────────────────────────────

    public long getDiarias() {
        if (dataCheckin != null && dataCheckout != null)
            return ChronoUnit.DAYS.between(dataCheckin, dataCheckout);
        return 0;
    }

    public double getValorTotal() {
        return precoBase * getDiarias();
    }

    // ── Getters e Setters ─────────────────────────────────────

    public int    getIdReserva()                       { return idReserva; }
    public void   setIdReserva(int v)                  { this.idReserva = v; }

    public String getCpf()                             { return cpf; }
    public void   setCpf(String v)                     { this.cpf = v; }

    public int    getCodQuarto()                       { return codQuarto; }
    public void   setCodQuarto(int v)                  { this.codQuarto = v; }

    public LocalDate getDataCheckin()                  { return dataCheckin; }
    public void   setDataCheckin(LocalDate d)          { this.dataCheckin = d; }

    public LocalDate getDataCheckout()                 { return dataCheckout; }
    public void   setDataCheckout(LocalDate d)         { this.dataCheckout = d; }

    public LocalDateTime getDataHoraReserva()          { return dataHoraReserva; }
    public void   setDataHoraReserva(LocalDateTime d)  { this.dataHoraReserva = d; }

    public String getNomeCliente()                     { return nomeCliente; }
    public void   setNomeCliente(String v)             { this.nomeCliente = v; }

    public String getNumeroQuarto()                    { return numeroQuarto; }
    public void   setNumeroQuarto(String v)            { this.numeroQuarto = v; }

    public double getPrecoBase()                       { return precoBase; }
    public void   setPrecoBase(double v)               { this.precoBase = v; }

    public String getNomeStatus()                      { return nomeStatus; }
    public void   setNomeStatus(String v)              { this.nomeStatus = v; }

    public String getFormaPagamento()                  { return formaPagamento; }
    public void   setFormaPagamento(String v)          { this.formaPagamento = v; }

    @Override
    public String toString() {
        return "#" + idReserva + " - " + nomeCliente + " | Quarto " + numeroQuarto;
    }
}
