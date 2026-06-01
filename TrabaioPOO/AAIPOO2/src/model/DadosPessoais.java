package model;

import java.time.LocalDate;

public class DadosPessoais {
    private String cpf;
    private String nome;
    private String telefone;
    private String email;
    private LocalDate dtNascimento;

    // Construtor vazio (necessário para o DAO)
    public  DadosPessoais() {}

    // Construtor completo (usado no momento do cadastro)
    public DadosPessoais(String cpf, String nome, String telefone, String email, LocalDate dtNascimento) {
        this.cpf = cpf;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.dtNascimento = dtNascimento;
    }

    // --- GETTERS E SETTERS ---

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNomeCliente() {
        return nome;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nome = nomeCliente;
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
        return dtNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dtNascimento = dataNascimento;
    }
}