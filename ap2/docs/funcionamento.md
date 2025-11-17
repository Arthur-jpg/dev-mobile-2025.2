# SplitEasy – Funcionamento Detalhado

Este documento explica, passo a passo, como o aplicativo foi construído, quais dados são coletados em cada tela e como eles viajam pelo app. A ideia é permitir que alguém que esteja começando em Kotlin/Android consiga entender o fluxo completo.

## 1. Estrutura Geral

- **Linguagem:** Kotlin.
- **Arquitetura simplificada:** Activities + Fragment + um repositório em memória (`TripRepository`), sem persistência em disco.
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
  - O repositório mantém os dados apenas em memória enquanto o app está aberto.
  - Ao fechar o app, os dados são descartados (o que é suficiente para a atividade da disciplina).

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

- O `TripRepository` guarda os dados apenas em listas em memória (`participants` e `expenses`).
- Ao fechar o app ou matar o processo, os dados são perdidos e uma nova viagem começa do zero na próxima execução.
- Se quiser explicitamente começar outra viagem durante o uso, basta chamar `TripRepository.clearAll()` de algum ponto do código.

## 7. Boas Práticas para Estudos

- Procure o arquivo relacionado a cada tela na pasta `app/src/main/java/com/example/ap2`.
- Observe como cada Activity usa `setContentView` e `findViewById` para ligar o XML ao código.
- Veja como `RecyclerView` e os Adapters usam `DiffUtil` (no `ParticipantAdapter` e `ExpenseAdapter`) para atualizar listas de forma eficiente.
- Para testar o fluxo, suba o app no emulador:
  1. Cadastre participantes.
  2. Adicione despesas variadas (com ajustes individuais).
  3. Edite/exclua para ver a atualização imediata.
  4. Verifique o fechamento e compartilhe o resumo.

## 8. Modelos (data classes) e Conversão de Moeda

### 8.1 `Currency`, `Money` e `CurrencyConverter`

- **`Currency`** (`app/src/main/java/com/example/ap2/model/Money.kt`):
  - Enum que representa as moedas suportadas:
    - `BRL` (Real), `USD` (Dólar), `EUR` (Euro).
  - Cada valor tem um `displayName` (nome amigável) e um `symbol` (R$, US$, €).
  - É usado na interface (spinners) e nas conversões de valores.

- **`Money`**:
  - Classe que junta dois dados:
    - `amount: Double` → o valor numérico.
    - `currency: Currency` → em qual moeda esse valor está.
  - Métodos principais:
    - `format()` → devolve uma `String` pronta para mostrar na tela (inclui símbolo e número formatado).
    - `convertTo(target: Currency)` → converte o valor atual para outra moeda, usando o `CurrencyConverter`.

- **`CurrencyConverter`**:
  - Objeto responsável por converter de uma moeda para outra.
  - Tem um mapa com taxas de referência para transformar qualquer moeda em BRL.
  - A função `convert(amount, from, to)`:
    1. Converte o valor da moeda de origem para BRL.
    2. Converte de BRL para a moeda destino.
  - É usado em:
    - `AddExpenseActivity` para validar ajustes individuais.
    - `ExpenseCalculator` para normalizar valores.
    - `OverviewActivity` e `SettlementActivity` para exibir totais na moeda escolhida.

### 8.2 `Participant`, `PersonalCharge`, `Expense`, `Transfer`

- **`Participant`** (`app/src/main/java/com/example/ap2/model/TripModels.kt`):
  - Representa uma pessoa que participa da viagem.
  - Tem:
    - `id: String` → gerado automaticamente com `UUID`.
    - `name: String` → nome digitado pelo usuário.

- **`PersonalCharge`**:
  - Representa um ajuste individual de uma despesa (por exemplo, “só o João pagou o estacionamento”).
  - Campos:
    - `participantId` → id de quem vai pagar esse item.
    - `money: Money` → valor e moeda.
    - `note: String?` → descrição opcional.

- **`Expense`**:
  - Representa uma despesa da viagem.
  - Campos:
    - `title` → nome da despesa.
    - `payerId` → id de quem pagou.
    - `total: Money` → valor total da despesa.
    - `sharedParticipantIds: List<String>` → ids das pessoas que vão dividir o valor restante igualmente.
    - `personalCharges: List<PersonalCharge>` → lista de ajustes individuais.

- **`Transfer`**:
  - Representa um “acerto” sugerido no fechamento.
  - Campos:
    - `from: Participant` → quem deve pagar.
    - `to: Participant` → quem deve receber.
    - `amount: Money` → quanto deve ser pago.

## 9. Repositório (`TripRepository`)

O `TripRepository` já é detalhado em `docs/trip_repository.md`, mas aqui vai um resumo rápido focado na relação com as telas:

- Guarda em memória:
  - `participants: MutableList<Participant>`
  - `expenses: MutableList<Expense>`
  - `tripName: String`
  - `displayCurrency: Currency`
- Fornece funções para:
  - **Participantes:** `getParticipants`, `addParticipant`, `removeParticipant`.
  - **Despesas:** `getExpenses`, `getExpense`, `addExpense`, `updateExpense`, `removeExpense`.
  - **Configurações:** `setTripName`, `updateDisplayCurrency`, `clearAll`.
- Todas as Activities/Fragment usam essas funções para:
  - Ler dados atuais (para preencher listas e textos).
  - Alterar dados quando o usuário interage (adiciona/remover/edita).

## 10. Adapters e Fragment

### 10.1 `ParticipantAdapter`

- Caminho: `app/src/main/java/com/example/ap2/ui/participants/ParticipantAdapter.kt`.
- Extende `ListAdapter<Participant, ParticipantViewHolder>` para:
  - Mostrar a lista de participantes na `ParticipantsActivity`.
  - Atualizar a lista com eficiência usando `DiffUtil`.
- Cada item de lista:
  - Mostra o nome (`participantName`).
  - Tem um botão de remover (`removeButton`) que chama a lambda `onRemove(participant)` recebida da Activity.

### 10.2 `ExpenseAdapter`

- Caminho: `app/src/main/java/com/example/ap2/ui/expenses/ExpenseAdapter.kt`.
- Responsável por exibir cada `Expense` no `ExpensesListFragment`.
- Para cada item:
  - Mostra:
    - título da despesa;
    - quem pagou;
    - valor original (`Money.format()`).
    - valor convertido para a moeda de exibição (se for diferente).
  - Mostra um resumo dos ajustes individuais (`PersonalCharge`).
  - Tem botões:
    - **Editar** → chama `onEdit(expense)` (abre `AddExpenseActivity` no modo edição).
    - **Excluir** → chama `onDelete(expense)` (o fragment exibe o diálogo e remove pelo repositório).

### 10.3 `PersonalChargeAdapter`

- Caminho: `app/src/main/java/com/example/ap2/ui/expenses/PersonalChargeAdapter.kt`.
- Mostra a lista de ajustes individuais dentro da `AddExpenseActivity`.
- Cada item mostra:
  - título (nome da pessoa + descrição).
  - valor (`Money.format()`).
  - botão de remover, que dispara `onRemove(charge)` para a Activity atualizar a lista local.

### 10.4 `TransferAdapter`

- Caminho: `app/src/main/java/com/example/ap2/ui/settlement/TransferAdapter.kt`.
- Mostra as transferências sugeridas na `SettlementActivity`.
- Para cada `Transfer`:
  - Exibe um texto “Fulano → Sicrano”.
  - Exibe o valor já convertido para a moeda de exibição.

### 10.5 `ExpensesListFragment`

- Caminho: `app/src/main/java/com/example/ap2/ui/expenses/ExpensesListFragment.kt`.
- É o **único Fragment** do app.
- Função principal:
  - Mostrar a lista de despesas (usando `ExpenseAdapter`).
  - Recarregar a lista sempre que a tela volta a ficar visível (`onResume`).
  - Intermediar ações de editar/excluir:
    - Editar → abre `AddExpenseActivity` com `EXTRA_EXPENSE_ID`.
    - Excluir → mostra um `AlertDialog`, remove a despesa do `TripRepository` e atualiza a lista.
- É reutilizado em:
  - `ExpensesActivity` (tela principal de despesas).
  - `OverviewActivity` (visão geral, com resumo + mesma lista de despesas).

## 11. Arquivos de Documentação

- `README.md`:
  - Visão geral do app.
  - Como rodar o projeto.
  - Prints das telas.
  - Como o app atende aos requisitos da AP2.

- `docs/funcionamento.md` (este arquivo):
  - Explica o fluxo completo, tela por tela.
  - Relaciona componentes principais (Activities, Fragment, repositório, cálculo).

- `docs/trip_repository.md`:
  - Detalha o `TripRepository` com foco em:
    - como as listas são mantidas;
    - como cada função se relaciona com a interação do usuário;
    - por que ele é melhor do que passar listas por `Intent` neste projeto.

## 12. Perguntas e Respostas (para estudo e apresentação)

### 12.1 Sobre Intents

**P: O que é uma Intent explícita e onde usamos isso no app?**  
R: Intent explícita é quando dizemos exatamente **qual Activity** queremos abrir.  
Exemplos:
- `Intent(this, ParticipantsActivity::class.java)` na `MainActivity`.
- `Intent(this, ExpensesActivity::class.java)` na `ParticipantsActivity`.
- `Intent(this, AddExpenseActivity::class.java)` e `Intent(this, SettlementActivity::class.java)` na `ExpensesActivity`.
- `Intent(this, OverviewActivity::class.java)` no botão “Ver visão geral”.
\n
**P: E uma Intent implícita? Onde usamos?**  
R: É quando pedimos ao Android para achar um app capaz de fazer uma ação, sem dizer qual Activity específica.  
Exemplos:
- Abrir site do Ibmec:
  ```kotlin
  Intent(Intent.ACTION_VIEW, Uri.parse(\"https://www.ibmec.br\"))
  ```
- Enviar e-mail:
  ```kotlin
  Intent(Intent.ACTION_SENDTO, Uri.parse(\"mailto:\"))
  ```
- Compartilhar resumo do fechamento:
  ```kotlin
  Intent(Intent.ACTION_SEND).apply { type = \"text/plain\" ... }
  ```

### 12.2 Sobre o Fragment

**P: Qual fragmento o app usa e qual o papel dele?**  
R: O app usa apenas o `ExpensesListFragment`. Ele é responsável por uma parte da tela: **a lista de despesas**.  
Ele mostra os dados do `TripRepository`, permite editar/excluir despesas e é reutilizado em duas telas:
- `ExpensesActivity` (tela principal de despesas).
- `OverviewActivity` (visão geral).
\n
**P: Por que usar um Fragment em vez de colocar tudo na Activity?**  
R: Porque o fragmento representa uma **porção reutilizável** da interface + comportamento.  
Aqui, a lista de despesas aparece em mais de uma Activity; o fragmento permite escrever essa lógica uma vez só e encaixar em layouts diferentes.

### 12.3 Sobre o TripRepository

**P: Por que criamos o `TripRepository` em vez de passar dados pelas Intents?**  
R: Passar listas completas entre Activities pela Intent exigiria usar `Serializable` ou `Parcelable` e `startActivityForResult`, o que é mais avançado.  
Com o `TripRepository`:
- mantemos tudo em uma única classe com listas em memória;
- as telas só perguntam “quais são os participantes/despesas?” ou “adicione/remova tal item”;
- o código usa apenas conceitos já vistos em sala: classes, listas e funções.

**P: O que acontece com os dados se eu fechar o app?**  
R: Como não há persistência em disco, os dados são apagados quando o processo do app é encerrado. Ao abrir novamente, a viagem começa do zero.

### 12.4 Sobre o cálculo de divisão

**P: Em qual moeda o cálculo é feito internamente?**  
R: Sempre em BRL. Cada despesa e ajuste individual é convertido para BRL pelo `CurrencyConverter`.  
Somente na hora de exibir (no overview e no fechamento) é que o valor total é convertido da base BRL para a moeda escolhida (`displayCurrency`).

**P: O que são os “ajustes individuais” e como eles entram na conta?**  
R: São valores que não entram na divisão igualitária (por exemplo, uma compra que só uma pessoa usou).  
No cálculo:
- Primeiro somamos todos os ajustes individuais em BRL;
- Subtraímos isso do total da despesa para obter o “pool” que será dividido igualmente entre os participantes marcados;
- Cada participante que participa da divisão paga uma parte igual desse pool, além de qualquer ajuste individual que ele tenha.

---
Com este guia você deve conseguir entender como cada tela conversa com o repositório, como as Intents trocam dados entre Activities, como o Fragment é reutilizado e como toda a lógica de divisão funciona por baixo dos panos. Bons estudos! ✈️
