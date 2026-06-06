package model;

public class TipoQuarto {

    private int    idTipoQuarto;
    private String nomeTipo;
    private int    capacidadePessoas;
    private double precoBase;

    public TipoQuarto() {}

    public TipoQuarto(int idTipoQuarto, String nomeTipo, int capacidadePessoas, double precoBase) {
        this.idTipoQuarto      = idTipoQuarto;
        this.nomeTipo          = nomeTipo;
        this.capacidadePessoas = capacidadePessoas;
        this.precoBase         = precoBase;
    }

    public int    getIdTipoQuarto()           { return idTipoQuarto; }
    public void   setIdTipoQuarto(int v)      { this.idTipoQuarto = v; }

    public String getNomeTipo()               { return nomeTipo; }
    public void   setNomeTipo(String v)       { this.nomeTipo = v; }

    public int    getCapacidadePessoas()      { return capacidadePessoas; }
    public void   setCapacidadePessoas(int v) { this.capacidadePessoas = v; }

    public double getPrecoBase()              { return precoBase; }
    public void   setPrecoBase(double v)      { this.precoBase = v; }

    /** Exibido no ComboBox da tela de cadastro de quartos. */
    @Override
    public String toString() {
        return nomeTipo
             + " (" + capacidadePessoas + " pessoa" + (capacidadePessoas > 1 ? "s" : "") + ")"
             + String.format(" — R$ %.2f/dia", precoBase);
    }
}
