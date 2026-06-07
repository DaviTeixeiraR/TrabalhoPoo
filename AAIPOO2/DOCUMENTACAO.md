# Sistema de Gestão Hoteleira — Hotel Manager

**Disciplina:** Programação Orientada a Objetos  
**Instituição:** FUMEC — Ciência da Computação  
**Professora:** Amanda Danielle Lima de Oliveira  
**Entrega:** 09/06/2026  

**Integrantes — Turma DA | Grupo 07:**
- Davi Teixeira Rabelo
- Luciano Pao Wen
- Luis Felipe dos Santos Alvarenga

**Banco de dados:** `DA123_AAI_G07`

---

## 1. Descrição do Sistema e Necessidade Identificada

O **Hotel Manager** é um sistema de gestão hoteleira desenvolvido para automatizar e organizar as operações diárias de um hotel, substituindo controles manuais por uma solução digital completa.

A necessidade identificada é a de hotéis de pequeno e médio porte que ainda gerenciam hóspedes, quartos e pagamentos por planilhas ou papel, o que gera erros como overbooking (dois clientes reservando o mesmo quarto no mesmo período), perda de histórico financeiro e dificuldade em visualizar a ocupação real do hotel.

O sistema resolve isso por meio de:

- Cadastro de clientes e quartos com validação de dados
- Controle de reservas com verificação inteligente de conflito de datas
- Fluxo completo de check-in, checkout e pagamento
- Dashboard com visão em tempo real da ocupação
- Relatório filtrado de todas as hospedagens

---

## 2. Regras de Negócio

### 2.1 Clientes
- CPF é a chave primária e deve ter exatamente 11 dígitos numéricos.
- E-mail e telefone são únicos por cliente (constraint `UNIQUE` no banco).
- Cliente deve ter nome completo (nome e sobrenome) e ao menos 18 anos.

### 2.2 Quartos
- Cada quarto pertence a um **Tipo de Quarto** (Simples, Duplo, Suíte, etc.) que define preço base e capacidade.
- O quarto possui um `id_status` para estado físico, mas a **disponibilidade real** é sempre calculada com base nas reservas ativas no banco — não no campo estático de status.
- Um quarto não pode ser excluído se possuir qualquer histórico de reserva (para preservar a integridade financeira).

### 2.3 Reservas
- Não é permitido reservar um quarto para um período que já possua outra reserva **ativa (sem pagamento)** sobreposta. A sobreposição é detectada pela lógica: `checkin_novo < checkout_existente AND checkout_novo > checkin_existente`.
- Reservas **já pagas** são tratadas como histórico e não bloqueiam novos períodos — permitindo, por exemplo, reutilizar um quarto que já foi ocupado e liberado.
- O mesmo cliente não pode ter duas reservas ativas idênticas (mesmo quarto, mesmo período), prevenindo duplicatas acidentais.
- A data de check-in não pode ser no passado.
- A data de checkout deve ser estritamente posterior ao check-in.

### 2.4 Checkout e Pagamento
- O checkout só é possível para reservas ativas (sem pagamento registrado).
- O pagamento quita a reserva e libera fisicamente o quarto (`id_status = 1`).
- Após o pagamento, o sistema verifica se o quarto possui **próxima reserva futura**: se sim, mantém o indicador visual como "Reservado"; caso contrário, marca como "Disponível".

### 2.5 Disponibilidade e Status Visual
O status exibido ao usuário é sempre derivado das reservas ativas, seguindo três estados:

| Estado | Condição | Cor |
|---|---|---|
| **Disponível** | Nenhuma reserva ativa presente ou futura | Verde |
| **Reservado: DD/MM a DD/MM** | Reserva futura, quarto físicamente livre hoje | Laranja |
| **Ocupado até DD/MM** | Hóspede presente (hoje está dentro do período) | Vermelho |

---

## 3. Decisões de Modelagem — Conceitos de POO

### 3.1 Pacotes e Arquitetura

O projeto adota o padrão **MVC (Model-View-Controller)** dividido em quatro pacotes principais:

```
src/
├── model/       — Classes de domínio e infraestrutura
├── dao/         — Acesso ao banco de dados (JDBC)
├── controler/   — Regras de negócio e orquestração
├── view/        — Interface gráfica (JavaFX)
└── util/        — Utilitários de validação
```

### 3.2 Encapsulamento

Todos os modelos de domínio possuem atributos **privados** acessíveis apenas via métodos `get`/`set`, protegendo o estado interno dos objetos.

**Exemplo — `Quarto.java`:**
```java
private int    codQuarto;
private double precoBase;
private List<LocalDate[]> reservasAtivas = new ArrayList<>();

public int    getCodQuarto()      { return codQuarto; }
public void   setCodQuarto(int v) { this.codQuarto = v; }
```

A classe `Conexao` encapsula os detalhes de conexão JDBC (URL, driver, credenciais), expondo apenas `conectar()` e `desconectar()` ao restante da aplicação.

### 3.3 Herança

A classe `Principal` estende `javafx.application.Application`, herdando todo o ciclo de vida da aplicação JavaFX (`init()`, `start()`, `stop()`). Isso é herança direta de uma classe da biblioteca padrão Java para especializar o comportamento de inicialização do sistema.

```java
public class Principal extends Application {
    @Override
    public void start(Stage stage) { ... }
}
```

Os modelos de dados também exploram a hierarquia de tipos do Java — `Reserva` e `Quarto` sobrescrevem `toString()` da classe `Object`, demonstrando herança com sobrescrita de método.

### 3.4 Polimorfismo

#### Sobrescrita (Override)
Todos os modelos sobrescrevem `toString()` de `Object` para exibição contextualizada:

```java
// Reserva.java
@Override
public String toString() {
    return "#" + idReserva + " - " + nomeCliente + " | Quarto " + numeroQuarto;
}

// Quarto.java
@Override
public String toString() {
    return "Quarto " + numeroQuarto + " - " + nomeTipo
         + String.format(" | R$ %.2f/dia", precoBase);
}
```

O método `toString()` de `Quarto` é usado diretamente pelo `ComboBox<Quarto>` do JavaFX em `TelaCheckin`, sem nenhuma configuração extra — o polimorfismo funciona transparentemente.

O método `getStatusDisplay()` em `Quarto` também é um caso de polimorfismo em nível de regra de negócio: o mesmo método retorna textos diferentes conforme o estado das reservas em tempo de execução.

#### Sobrecarga (Overload)
Todos os modelos oferecem construtor vazio (para uso pelo DAO) e construtor completo (para criação na camada de view/controller):

```java
// Reserva.java
public Reserva() {}   // usado pelo DAO ao mapear ResultSet

public Reserva(String cpf, int codQuarto,
               LocalDate dataCheckin, LocalDate dataCheckout) { ... }
```

```java
// TelaCheckin.java — sobrecarga do construtor de tela
public TelaCheckin() {}
public TelaCheckin(Quarto quartoSelecionado) { ... }
```

### 3.5 Classe Utilitária (Validador)

A classe `Validador` concentra todas as regras de validação da aplicação em métodos estáticos que retornam `null` (válido) ou uma `String` com a mensagem de erro. Esse padrão evita repetição de lógica de validação entre as telas:

```java
public static String validarCpf(String cpf) { ... }
public static String validarNome(String nome) { ... }
public static String validarEmail(String email) { ... }
public static String validarCheckin(LocalDate checkin) { ... }
public static String validarCheckout(LocalDate checkin, LocalDate checkout) { ... }
```

### 3.6 Classe de Gerenciamento de Tema (ThemeManager)

`ThemeManager` é uma classe com estado global estático que controla o tema visual (claro/escuro) da aplicação. Todas as telas a consultam para obter as cores corretas, garantindo consistência visual sem acoplamento direto entre views.

```java
public class ThemeManager {
    private static boolean dark = false;
    public static void toggle() { dark = !dark; }
    public static String bg() { return dark ? "#1A1D27" : "#F0F4F8"; }
    // ...
}
```

---

## 4. Telas do Sistema

### Tela 1 — Login (`Tela_Login`)
Tela de autenticação que controla o acesso ao sistema. Ponto de entrada da aplicação.

### Tela 2 — Dashboard (`TelaMenuPrincipal`) — Menu Principal
Painel inicial com visão geral em tempo real:
- **Disponíveis** — quartos sem nenhuma reserva ativa
- **Com Reserva Futura** — quartos livres hoje mas com reservas agendadas
- **Ocupados** — quartos com hóspede presente
- **Reservas Ativas** — total de reservas sem pagamento
- **Entradas Hoje** — reservas registradas no dia
- **Receita Total** — soma de todos os pagamentos realizados
- Botões de ação rápida para as principais funcionalidades

### Tela 3 — Clientes (`TelaDadosPessoais`) — Cadastro 1
CRUD completo de clientes. Campos: CPF, nome completo, telefone, e-mail, data de nascimento. Validação em tempo real de todos os campos. Permite inserir, editar e excluir clientes.

### Tela 4 — Quartos (`TelaQuartos`) — Cadastro 2
CRUD completo de quartos. Layout dividido: tabela à esquerda com listagem e status atual de cada quarto; formulário à direita para adição/edição. A coluna de status exibe o período de reserva real ("Disponível", "Reservado: DD/MM a DD/MM", "Ocupado até DD/MM") com tooltip listando todas as reservas ativas ao passar o mouse.

### Tela 5 — Quartos e Status (`TelaSelecaoQuartos`) — Junção / Visão Operacional
Visão em grade de todos os quartos do hotel com cards coloridos. Cada card exibe número, tipo, capacidade, preço e o status dinâmico derivado das reservas. Quartos com múltiplas reservas exibem um botão **"📅 Ver reservas (N)"** que abre um popup listando todos os períodos, destacando qual está em curso. Clicar em um quarto disponível navega diretamente para o check-in.

### Tela 6 — Check-in (`TelaCheckin`)
Formulário de nova reserva com três etapas:
1. Busca de cliente por CPF
2. Seleção de quarto — a lista se atualiza dinamicamente conforme as datas selecionadas, exibindo apenas quartos disponíveis para o período informado
3. Definição de datas de check-in e checkout com cálculo automático do valor total

Valida conflito de período e reserva duplicada antes de salvar.

### Tela 7 — Checkout (`TelaCheckout`)
Lista todas as reservas ativas (sem pagamento) para o recepcionista selecionar a que será encerrada. Exibe quarto, cliente, período, diárias e valor.

### Tela 8 — Pagamento (`TelaCheckoutPagamento`)
Exibe resumo completo da hospedagem (cliente, quarto, período, diárias, total) e permite selecionar a forma de pagamento (Dinheiro, Cartão de Crédito, Cartão de Débito, PIX). Confirmar o pagamento registra a quitação e libera o quarto.

### Tela 9 — Relatório (`TelaRelatorio`)
Tabela com todas as reservas do sistema (ativas e pagas). Oferece **três filtros combinados**:
1. **Status de Pagamento** — Todos / Pago / Pendente
2. **Período de Check-in** — De (data) / Até (data)
3. **CPF do Cliente** — busca por texto

Rodapé com sumário dinâmico: total de reservas filtradas, total de diárias e receita do período filtrado.

---

## 5. Banco de Dados

### 5.1 Tecnologia
- **SGBD:** Microsoft SQL Server (SQL Server Express)
- **Conexão:** JDBC via `DriverManager`, autenticação integrada do Windows

### 5.2 Estrutura das Tabelas

```
Cliente
├── cpf            VARCHAR(11)   PK
├── nome_cliente   VARCHAR(100)  NOT NULL
├── telefone       VARCHAR(20)   UNIQUE
├── email          VARCHAR(100)  UNIQUE
└── data_nascimento DATE

Status_Quarto
├── id_status      INT           PK
└── descricao      VARCHAR(20)   NOT NULL

Tipo_Quarto
├── id_tipo_quarto INT IDENTITY  PK
├── nome_tipo      VARCHAR(50)   NOT NULL
├── capacidade_pessoas INT       CHECK (> 0)
└── preco_base     DECIMAL(10,2) CHECK (>= 0)

Quarto
├── cod_quarto     INT IDENTITY  PK
├── numero_quarto  INT           NOT NULL
├── id_tipo_quarto INT           FK → Tipo_Quarto
└── id_status      INT           FK → Status_Quarto

Forma_Pagamento
├── id_forma_pagamento INT IDENTITY PK
└── descricao          VARCHAR(50)  NOT NULL

Reserva
├── id_reserva         INT IDENTITY PK
├── cpf                VARCHAR(11)  FK → Cliente
├── cod_quarto         INT          FK → Quarto
├── data_checkin       DATE         NOT NULL
├── data_checkout      DATE         NOT NULL
└── data_hora_reserva  DATETIME     DEFAULT GETDATE()

Pagamento
├── id_pagamento        INT IDENTITY PK
├── id_reserva          INT          FK → Reserva
├── id_forma_pagamento  INT          FK → Forma_Pagamento
├── valor_pago          DECIMAL(10,2) CHECK (> 0)
└── data_pagamento      DATETIME     DEFAULT GETDATE()
```

### 5.3 Decisões de Modelagem do Banco

**Separação de Tipo e Instância de Quarto:** `Tipo_Quarto` armazena as categorias com preço e capacidade; `Quarto` representa cada unidade física. Isso evita redundância — mudar o preço de um tipo atualiza todos os quartos daquele tipo automaticamente.

**Status como tabela auxiliar:** `Status_Quarto` permite adicionar novos estados (ex: Manutenção) sem alterar o schema.

**Pagamento como entidade separada:** Em vez de um campo `pago BOOLEAN` na reserva, o pagamento é uma entidade própria. Isso permite registrar a forma de pagamento, o valor exato pago, a data e hora, e ainda serve como histórico auditável. Uma reserva sem registro em `Pagamento` é considerada ativa.

**Integridade referencial:** Todas as FKs são declaradas explicitamente, garantindo que não exista quarto sem tipo, reserva sem cliente, ou pagamento sem reserva.

**Constraints de negócio no banco:**
- `UQ_Cliente_Email` e `UQ_Cliente_Telefone` — unicidade garantida no nível do banco
- `CK_Capacidade_Minima` — capacidade do quarto sempre positiva
- `CK_Valor_Pago_Positivo` — valor do pagamento sempre maior que zero

### 5.4 Padrão de Acesso ao Banco (DAO)

Cada tabela possui uma classe DAO dedicada que concentra todas as queries SQL. Os controllers orquestram as chamadas aos DAOs e gerenciam a abertura/fechamento de conexão:

```java
// Padrão adotado em todos os controllers
try {
    Conexao.conectar();
    QuartoDao dao = new QuartoDao(Conexao.conexao);
    return dao.listarTodosComStatus();
} catch (Exception e) {
    // tratamento de erro
} finally {
    Conexao.desconectar();
}
```

---

## 6. Principais Funcionalidades

| Funcionalidade | Onde | Descrição |
|---|---|---|
| Cadastro de clientes | `TelaDadosPessoais` | CRUD completo com validação de CPF, e-mail, telefone e idade mínima |
| Cadastro de quartos | `TelaQuartos` | CRUD com proteção: quartos com histórico de reservas não podem ser excluídos |
| Visualização de ocupação | `TelaSelecaoQuartos` | Grade visual com status em tempo real derivado das reservas ativas |
| Check-in inteligente | `TelaCheckin` | Lista de quartos filtra automaticamente por período; bloqueia conflitos e duplicatas |
| Checkout e pagamento | `TelaCheckout` + `TelaCheckoutPagamento` | Fluxo em duas etapas: seleção da reserva e confirmação do pagamento com forma escolhida |
| Dashboard operacional | `TelaMenuPrincipal` | Contadores de disponíveis / reservados / ocupados calculados via reservas, não por campo estático |
| Relatório com filtros | `TelaRelatorio` | Três filtros combinados: status de pagamento, período e CPF; sumário dinâmico de diárias e receita |
| Tema claro/escuro | Toda a aplicação | Alternância em tempo real via `ThemeManager`, sem reinicialização |
| Popup de reservas | `TelaSelecaoQuartos` | Botão no card abre popup com todos os períodos ativos do quarto, destacando o em curso |
