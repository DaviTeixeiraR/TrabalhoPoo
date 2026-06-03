package model;

/**
 * Model que representa a tabela Forma_Pagamento do banco de dados.
 * O toString() é sobrescrito para exibição correta em ComboBox.
 */
public class FormaPagamento {

    private int    idFormaPagamento;
    private String descricao;

    // ── Construtores ──────────────────────────────────────────

    public FormaPagamento() {}

    public FormaPagamento(int idFormaPagamento, String descricao) {
        this.idFormaPagamento = idFormaPagamento;
        this.descricao        = descricao;
    }

    // ── Getters e Setters ─────────────────────────────────────

    public int getIdFormaPagamento()              { return idFormaPagamento; }
    public void setIdFormaPagamento(int id)       { this.idFormaPagamento = id; }

    public String getDescricao()                  { return descricao; }
    public void setDescricao(String d)            { this.descricao = d; }

    /** Exibido automaticamente no ComboBox. */
    @Override
    public String toString() {
        return descricao;
    }
}
