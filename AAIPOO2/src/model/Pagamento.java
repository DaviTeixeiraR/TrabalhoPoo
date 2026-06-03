package model;

import java.time.LocalDate;

/**
 * Model que representa a tabela Pagamento do banco de dados.
 * Campos extras (descricaoForma, nomeCliente) são usados para exibição.
 */
public class Pagamento {

    private int       idPagamento;
    private int       idReserva;
    private int       idFormaPagamento;
    private double    valorPago;
    private LocalDate dataPagamento;

    // Campos extras para exibição (JOIN nas consultas)
    private String descricaoForma;
    private String nomeCliente;
    private String numeroQuarto;

    // ── Construtores ──────────────────────────────────────────

    public Pagamento() {}

    public Pagamento(int idReserva, int idFormaPagamento,
                     double valorPago, LocalDate dataPagamento) {
        this.idReserva        = idReserva;
        this.idFormaPagamento = idFormaPagamento;
        this.valorPago        = valorPago;
        this.dataPagamento    = dataPagamento;
    }

    // ── Getters e Setters ─────────────────────────────────────

    public int getIdPagamento()                      { return idPagamento; }
    public void setIdPagamento(int id)               { this.idPagamento = id; }

    public int getIdReserva()                        { return idReserva; }
    public void setIdReserva(int id)                 { this.idReserva = id; }

    public int getIdFormaPagamento()                 { return idFormaPagamento; }
    public void setIdFormaPagamento(int id)          { this.idFormaPagamento = id; }

    public double getValorPago()                     { return valorPago; }
    public void setValorPago(double v)               { this.valorPago = v; }

    public LocalDate getDataPagamento()              { return dataPagamento; }
    public void setDataPagamento(LocalDate d)        { this.dataPagamento = d; }

    // Campos de exibição
    public String getDescricaoForma()                { return descricaoForma; }
    public void setDescricaoForma(String d)          { this.descricaoForma = d; }

    public String getNomeCliente()                   { return nomeCliente; }
    public void setNomeCliente(String n)             { this.nomeCliente = n; }

    public String getNumeroQuarto()                  { return numeroQuarto; }
    public void setNumeroQuarto(String n)            { this.numeroQuarto = n; }
}
