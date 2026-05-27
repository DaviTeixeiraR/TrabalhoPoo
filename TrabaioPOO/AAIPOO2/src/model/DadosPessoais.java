package model;

import java.time.LocalDate;

public class DadosPessoais {
    private String cpf;
    private String nomeCliente;
    private String telefone;
    private String email;
    private LocalDate dataNascimento;

    // Construtor vazio (necessário para o DAO)
    public  DadosPessoais() {}

    // Construtor completo (usado no momento do cadastro)
    public DadosPessoais(String cpf, String nomeCliente, String telefone, String email, LocalDate dataNascimento) {
        this.cpf = cpf;
        this.nomeCliente = nomeCliente;
        this.telefone = telefone;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }

    // --- GETTERS E SETTERS ---

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}