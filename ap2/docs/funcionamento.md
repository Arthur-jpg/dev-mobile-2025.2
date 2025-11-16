# SplitEasy – Funcionamento Detalhado

Este documento explica, passo a passo, como o aplicativo foi construído, quais dados são coletados em cada tela e como eles viajam pelo app. A ideia é permitir que alguém que esteja começando em Kotlin/Android consiga entender o fluxo completo.

## 1. Estrutura Geral

- **Linguagem:** Kotlin.
- **Arquitetura simplificada:** Activities + Fragment + um repositório em memória (`TripRepository`), com persistência em `SharedPreferences`.
- **Principais componentes:**
  - `MainActivity` (Onboarding)
  - `ParticipantsActivity`
  - `ExpensesActivity` + `ExpensesListFragment`
  - `AddExpenseActivity`
  - `SettlementActivity`
  - `TripRepository` (armazenamento central)
  - `ExpenseCalculator` (regra de negócio)

## 2. Repositório e Persistência

### `TripRepository`
- Implementado como `object` (singleton) para ser acessível de qualquer lugar.
- Guarda 4 informações centrais:
  1. `tripName`: nome da viagem.
  2. `displayCurrency`: moeda para exibir totais.
  3. Lista de `Participant`.
  4. Lista de `Expense`.
- Persistência:
  - Na inicialização do app (`SplitEasyApp`), chamamos `TripRepository.initialize(context)`.
  - O repositório salva todo o estado em `SharedPreferences` como um JSON único.
  - Cada alteração (adicionar participante, despesa etc.) chama `persistState()` para gravar o JSON.
  - Ao abrir o app novamente, `loadState()` reconstrói a memória a partir desse JSON.

## 3. Fluxo de Telas e Dados

### 3.1 `MainActivity`
- Mostra o gradiente de boas-vindas e pede o nome da viagem.
- Ao tocar em “Começar”:
  1. O texto digitado é enviado para `TripRepository.setTripName(tripName)`.
  2. Abrimos a `ParticipantsActivity` com `Intent(this, ParticipantsActivity::class.java)`.

### 3.2 `ParticipantsActivity`
- Reaproveita o `TripRepository` para ler e gravar participantes.
- Elementos principais:
  - RecyclerView com `ParticipantAdapter` (mostra lista).
  - TextInputLayout para digitar novos nomes.
  - Botão “Adicionar”: chama `TripRepository.addParticipant(name)` (com validações de nome vazio e duplicado).
  - Cada item da lista tem botão de remover → `TripRepository.removeParticipant(id)`.
  - Botão “Ir para as despesas” só habilita quando existem pelo menos 2 participantes.
- Ao clicar em “Ir para as despesas” abrimos `ExpensesActivity`.

### 3.3 `ExpensesActivity`
- Funciona como hub das despesas e contém:
  - Toolbar com nome da viagem.
  - Card com informações de moeda e botão “Ver fechamento” (abre `SettlementActivity`).
  - FragmentContainerView que recebe o `ExpensesListFragment` (injeção feita no `onCreate`).
  - FAB “Adicionar despesa” que abre `AddExpenseActivity`.
- A partir dessa tela o usuário pode acessar também a **Visão geral** (novo botão “Ver visão geral”), que reaproveita o mesmo fragmento para mostrar as despesas em outro contexto.
- Também expõe um menu com Intents implícitas:
  - `ACTION_VIEW` para abrir site do Ibmec.
  - `ACTION_SENDTO` com `mailto` para simular envio de e-mail.

### 3.4 `OverviewActivity`
- Tela adicional que reutiliza o `ExpensesListFragment` para oferecer uma visão consolidada antes de ir para o fechamento.
- Mostra um cartão com o total gasto na moeda selecionada e a quantidade de participantes cadastrados, além do fragmento com a lista reutilizada.
- Pode ser acessada a partir do botão “Ver visão geral” na `ExpensesActivity`.

### 3.5 `ExpensesListFragment`
- Responsável por exibir a lista atualizada de despesas.
- Ao criar:
  1. Instancia `ExpenseAdapter` com lambdas de ação (`onEdit`, `onDelete`).
  2. Configura RecyclerView + layout manager.
  3. Chama `refreshList()` sempre que volta para o fragment (no `onResume`).
- `refreshList()` lê `TripRepository.getExpenses()` e envia para o adapter.
- Ações do adapter:
  - **Editar:** abre `AddExpenseActivity` passando `EXTRA_EXPENSE_ID` via Intent.
  - **Excluir:** pede confirmação (`AlertDialog`) e chama `TripRepository.removeExpense(id)`.

### 3.6 `AddExpenseActivity`
- Tela usada para criar ou editar despesas.
- Passo a passo:
  1. Recupera participantes do repositório (se não houver, fecha com Toast).
  2. Verifica se recebeu um `EXTRA_EXPENSE_ID` e, se sim, carrega a despesa existente pelo repositório.
  3. Configura spinners (moeda e pagador) usando `ArrayAdapter`.
  4. Monta chips com todos os participantes (`ChipGroup`) marcados por padrão.
  5. Configura RecyclerView de ajustes individuais (`PersonalChargeAdapter`).
  6. `addPersonalChargeButton`: abre `AlertDialog` com um layout customizado (`DialogPersonalChargeBinding`) para informar:
      - Participante
      - Valor
      - Observação (opcional)
      - Moeda (usa a mesma do formulário principal)
  7. `saveExpenseButton`:
      - Valida campos (valor > 0, pagador selecionado, pelo menos 1 participante se houver divisão).
      - Converte valores para BRL para conferir se ajustes individuais não excedem a despesa.
      - Se for nova despesa ⇒ `TripRepository.addExpense(...)`.
      - Se for edição ⇒ `TripRepository.updateExpense(...)`.
      - Mostra `Toast` e finaliza a Activity.

### 3.7 `SettlementActivity`
- Consulta participantes + despesas e usa `ExpenseCalculator.buildSummary(...)` para gerar:
  - `totalSpent`
  - `balances`: mapa com saldo de cada participante.
  - `transfers`: lista ordenada de quem deve pagar para quem.
- Também permite alterar a moeda de exibição:
  1. Spinner inicializado com todas as `Currency`.
  2. Ao selecionar uma, atualiza `TripRepository.displayCurrency`.
  3. Re-renderiza os valores convertendo de BRL para a moeda escolhida (exibição apenas).
- Botão “Compartilhar resumo” cria um texto com as transferências e envia via `Intent.ACTION_SEND`.

## 4. Lógica de Cálculo

### `ExpenseCalculator`
- Sempre trabalha no **BRL** como base.
- Para cada despesa:
  1. Converte o valor total para BRL.
  2. Soma ajustes individuais, também convertendo.
  3. Calcula o valor “compartilhado” (total – ajustes).
  4. Divide igualmente entre os participantes marcados para aquela despesa.
  5. Atualiza o saldo de quem pagou (+total) e de quem deve (-share ou -ajuste).
- Depois cria duas listas:
  - Devedores (saldo negativo).
  - Credores (saldo positivo).
- Gera transferências simulando quitações entre as duas listas.

## 5. Compartilhamento e Intents

- `ExpensesActivity`:
  - **Menu Abrir Site:** `Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ibmec.br"))`.
  - **Menu Enviar e-mail:** `Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))` com extras de destinatário e assunto.
- `SettlementActivity`:
  - **Compartilhar resumo:** `Intent(Intent.ACTION_SEND)` com `type = "text/plain"`.

## 6. Persistência e Estado

- `TripRepository.initialize(context)` é chamado apenas uma vez no `Application` (`SplitEasyApp`) para manter o estado em memória.
- Todos os métodos de escrita chamam `persistState()` que:
  - Constrói um `JSONObject` com todas as entidades.
  - Salva o JSON em `SharedPreferences`.
- Quando o app fecha, nada se perde.
- Se quiser começar uma viagem do zero, basta chamar `TripRepository.clearAll()` (hoje é feito apenas quando o usuário reinstala o app ou limpa dados).

## 7. Boas Práticas para Estudos

- Procure o arquivo relacionado a cada tela na pasta `app/src/main/java/com/example/ap2`.
- Observe como o View Binding (`ActivityXxxBinding`) evita `findViewById`.
- Veja como `RecyclerView` e os Adapters usam `DiffUtil` (no `ParticipantAdapter` e `ExpenseAdapter`).
- Para testar o fluxo, suba o app no emulador:
  1. Cadastre participantes.
  2. Adicione despesas variadas (com ajustes individuais).
  3. Edite/exclua para ver a atualização imediata.
  4. Verifique o fechamento e compartilhe o resumo.

---
Com este guia você deve conseguir entender como cada tela conversa com o repositório, como as Intents trocam dados entre Activities e como toda a lógica de divisão funciona por baixo dos panos. Bons estudos! ✈️
