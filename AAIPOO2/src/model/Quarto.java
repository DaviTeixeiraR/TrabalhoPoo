package model;

/**
 * Model que representa a tabela Quarto do banco de dados.
 * Os campos nomeTipo e descricaoStatus são preenchidos via JOIN nas consultas.
 */
public class Quarto {

    private String codQuarto;
    private String numeroQuarto;
    private int    capacidadePessoas;
    private double precoBase;
    private int    idStatus;
    private int    idTipoQuarto;

    // Campos extras para exibição (JOIN nas consultas)
    private String nomeTipo;
    private String descricaoStatus;

    // ── Construtores ──────────────────────────────────────────

    public Quarto() {}

    public Quarto(String codQuarto, String numeroQuarto,
                  int capacidadePessoas, double precoBase, int idStatus) {
        this.codQuarto        = codQuarto;
        this.numeroQuarto     = numeroQuarto;
        this.capacidadePessoas = capacidadePessoas;
        this.precoBase        = precoBase;
        this.idStatus         = idStatus;
    }

    // ── Getters e Setters ─────────────────────────────────────

    public String getCodQuarto()                    { return codQuarto; }
    public void setCodQuarto(String v)              { this.codQuarto = v; }

    public String getNumeroQuarto()                 { return numeroQuarto; }
    public void setNumeroQuarto(String v)           { this.numeroQuarto = v; }

    public int getCapacidadePessoas()               { return capacidadePessoas; }
    public void setCapacidadePessoas(int v)         { this.capacidadePessoas = v; }

    public double getPrecoBase()                    { return precoBase; }
    public void setPrecoBase(double v)              { this.precoBase = v; }

    public int getIdStatus()                        { return idStatus; }
    public void setIdStatus(int v)                  { this.idStatus = v; }

    public int getIdTipoQuarto()                    { return idTipoQuarto; }
    public void setIdTipoQuarto(int v)              { this.idTipoQuarto = v; }

    // Campos de exibição
    public String getNomeTipo()                     { return nomeTipo; }
    public void setNomeTipo(String v)               { this.nomeTipo = v; }

    public String getDescricaoStatus()              { return descricaoStatus; }
    public void setDescricaoStatus(String v)        { this.descricaoStatus = v; }

    /** Exibido automaticamente no ComboBox de seleção de quarto. */
    @Override
    public String toString() {
        return "Quarto " + numeroQuarto
             + (nomeTipo != null ? " - " + nomeTipo : "")
             + String.format(" | R$ %.2f/dia", precoBase);
    }
}