package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Model que representa a tabela Reserva do banco de dados.
 * Campos extras (nomeCliente, numeroQuarto, precoBase) são usados
 * apenas para exibição em tela, sem persistência.
 */
public class Reserva {

    private int        idReserva;
    private String     cpf;
    private String     codQuarto;
    private LocalDate  dataCheckin;
    private LocalDate  dataCheckout;
    private LocalDateTime dataHoraReserva;

    // Campos extras para exibição (JOIN nas consultas)
    private String nomeCliente;
    private String numeroQuarto;
    private double precoBase;
    private String nomeStatus;

    // ── Construtores ──────────────────────────────────────────

    public Reserva() {}

    public Reserva(String cpf, String codQuarto,
                   LocalDate dataCheckin, LocalDate dataCheckout) {
        this.cpf             = cpf;
        this.codQuarto       = codQuarto;
        this.dataCheckin     = dataCheckin;
        this.dataCheckout    = dataCheckout;
        this.dataHoraReserva = LocalDateTime.now();
    }

    // ── Métodos utilitários ───────────────────────────────────

    /** Calcula o número de diárias entre check-in e check-out. */
    public long getDiarias() {
        if (dataCheckin != null && dataCheckout != null) {
            return ChronoUnit.DAYS.between(dataCheckin, dataCheckout);
        }
        return 0;
    }

    /** Calcula o valor total com base no preço base e número de diárias. */
    public double getValorTotal() {
        return precoBase * getDiarias();
    }

    // ── Getters e Setters ─────────────────────────────────────

    public int getIdReserva()                       { return idReserva; }
    public void setIdReserva(int idReserva)         { this.idReserva = idReserva; }

    public String getCpf()                          { return cpf; }
    public void setCpf(String cpf)                  { this.cpf = cpf; }

    public String getCodQuarto()                    { return codQuarto; }
    public void setCodQuarto(String codQuarto)      { this.codQuarto = codQuarto; }

    public LocalDate getDataCheckin()               { return dataCheckin; }
    public void setDataCheckin(LocalDate d)         { this.dataCheckin = d; }

    public LocalDate getDataCheckout()              { return dataCheckout; }
    public void setDataCheckout(LocalDate d)        { this.dataCheckout = d; }

    public LocalDateTime getDataHoraReserva()       { return dataHoraReserva; }
    public void setDataHoraReserva(LocalDateTime d) { this.dataHoraReserva = d; }

    // Campos de exibição
    public String getNomeCliente()                  { return nomeCliente; }
    public void setNomeCliente(String n)            { this.nomeCliente = n; }

    public String getNumeroQuarto()                 { return numeroQuarto; }
    public void setNumeroQuarto(String n)           { this.numeroQuarto = n; }

    public double getPrecoBase()                    { return precoBase; }
    public void setPrecoBase(double p)              { this.precoBase = p; }

    public String getNomeStatus()                   { return nomeStatus; }
    public void setNomeStatus(String s)             { this.nomeStatus = s; }

    @Override
    public String toString() {
        return "#" + idReserva + " - " + nomeCliente + " | Quarto " + numeroQuarto;
    }
}
