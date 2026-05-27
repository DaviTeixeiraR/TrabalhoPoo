package model;

public class Quarto {
	private String codQuarto;
	private String numeroQuarto;
	private int capacidadePessoas;
	private double precoBase;
	private int idStatus;

	public Quarto() {
	}

	public Quarto(String codQuarto, String numeroQuarto, int capacidadePessoas, double precoBase, int idStatus) {
		this.codQuarto = codQuarto;
		this.numeroQuarto = numeroQuarto;
		this.capacidadePessoas = capacidadePessoas;
		this.precoBase = precoBase;
		this.idStatus = idStatus;
	}

	public String getCodQuarto() {
		return codQuarto;
	}

	public void setCodQuarto(String codQuarto) {
		this.codQuarto = codQuarto;
	}

	public String getNumeroQuarto() {
		return numeroQuarto;
	}

	public void setNumeroQuarto(String numeroQuarto) {
		this.numeroQuarto = numeroQuarto;
	}

	public int getCapacidadePessoas() {
		return capacidadePessoas;
	}

	public void setCapacidadePessoas(int capacidadePessoas) {
		this.capacidadePessoas = capacidadePessoas;
	}

	public double getPrecoBase() {
		return precoBase;
	}

	public void setPrecoBase(double precoBase) {
		this.precoBase = precoBase;
	}

	public int getIdStatus() {
		return idStatus;
	}

	public void setIdStatus(int idStatus) {
		this.idStatus = idStatus;
	}
}