package util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Classe utilitária com métodos estáticos de validação.
 * Retorna uma mensagem de erro (String) quando inválido, ou null quando válido.
 */
public class Validador {

    // ── CPF ───────────────────────────────────────────────────

    /**
     * Valida CPF verificando apenas a quantidade de dígitos (11).
     * Aceita CPF com ou sem formatação (pontos e traço são ignorados).
     */
    public static String validarCpf(String cpf) {
        if (cpf == null || cpf.isBlank())
            return "CPF é obrigatório.";

        String digitos = cpf.replaceAll("[^0-9]", "");

        if (digitos.length() != 11)
            return "CPF deve conter exatamente 11 dígitos numéricos.";

        return null; // válido
    }

    // ── Nome ──────────────────────────────────────────────────

    /**
     * Nome deve ter ao menos duas palavras (nome e sobrenome) e não pode conter números.
     */
    public static String validarNome(String nome) {
        if (nome == null || nome.isBlank())
            return "Nome é obrigatório.";

        if (nome.trim().length() < 4)
            return "Nome muito curto.";

        if (nome.matches(".*[0-9].*"))
            return "Nome não pode conter números.";

        String[] partes = nome.trim().split("\\s+");
        if (partes.length < 2)
            return "Informe o nome completo (nome e sobrenome).";

        for (String parte : partes)
            if (parte.length() < 2)
                return "Cada parte do nome deve ter ao menos 2 caracteres.";

        return null;
    }

    // ── E-mail ────────────────────────────────────────────────

    public static String validarEmail(String email) {
        if (email == null || email.isBlank())
            return "E-mail é obrigatório.";

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            return "E-mail inválido. Use o formato: exemplo@dominio.com";

        return null;
    }

    // ── Telefone ──────────────────────────────────────────────

    /**
     * Aceita qualquer formatação; valida apenas a quantidade de dígitos (10 ou 11).
     */
    public static String validarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank())
            return "Telefone é obrigatório.";

        String digitos = telefone.replaceAll("[^0-9]", "");
        if (digitos.length() < 10 || digitos.length() > 11)
            return "Telefone deve ter 10 ou 11 dígitos. Ex: (11) 99999-9999";

        return null;
    }

    // ── Data de Nascimento ────────────────────────────────────

    /**
     * Verifica se a data de nascimento é válida: não pode ser futura,
     * não pode ser há mais de 120 anos e o cliente deve ter ao menos {@code idadeMinima} anos.
     */
    public static String validarNascimento(LocalDate nascimento, int idadeMinima) {
        if (nascimento == null)
            return "Data de nascimento é obrigatória.";

        LocalDate hoje = LocalDate.now();

        if (nascimento.isAfter(hoje))
            return "Data de nascimento não pode ser uma data futura.";

        if (nascimento.isBefore(hoje.minusYears(120)))
            return "Data de nascimento inválida.";

        int idade = Period.between(nascimento, hoje).getYears();
        if (idade < idadeMinima)
            return "O cliente deve ter pelo menos " + idadeMinima + " anos. "
                 + "(Idade informada: " + idade + " anos)";

        return null;
    }

    // ── Número do Quarto ──────────────────────────────────────

    /**
     * Número do quarto deve ser um inteiro positivo entre 1 e 9999.
     */
    public static String validarNumeroQuarto(String texto) {
        if (texto == null || texto.isBlank())
            return "Número do quarto é obrigatório.";

        int numero;
        try {
            numero = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return "Número do quarto deve ser um valor inteiro (ex: 101).";
        }

        if (numero < 1 || numero > 9999)
            return "Número do quarto deve estar entre 1 e 9999.";

        return null;
    }

    // ── Datas de Reserva ──────────────────────────────────────

    /**
     * A data de check-in não pode ser anterior a hoje.
     */
    public static String validarCheckin(LocalDate checkin) {
        if (checkin == null)
            return "Data de check-in é obrigatória.";

        if (checkin.isBefore(LocalDate.now()))
            return "A data de check-in não pode ser uma data passada.";

        return null;
    }

    /**
     * O check-out deve ser estritamente após o check-in.
     */
    public static String validarCheckout(LocalDate checkin, LocalDate checkout) {
        if (checkout == null)
            return "Data de check-out é obrigatória.";

        if (checkin != null && !checkout.isAfter(checkin))
            return "A data de check-out deve ser posterior à data de check-in.";

        return null;
    }
}
