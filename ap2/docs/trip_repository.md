# TripRepository – Explicação Passo a Passo

Este documento explica, de forma bem detalhada e em linguagem simples, como funciona o `TripRepository` e por que ele foi escolhido em vez de ficar passando listas entre Activities com `Intent`.

> Ideia central: o `TripRepository` é **um lugar único** onde o app guarda tudo que é importante sobre a viagem enquanto o aplicativo está aberto.

---

## 1. O que é o TripRepository?

No código, o repositório está em `app/src/main/java/com/example/ap2/data/TripRepository.kt` e começa assim:

```kotlin
object TripRepository {
    private val participants = mutableListOf<Participant>()
    private val expenses = mutableListOf<Expense>()

    var tripName: String = "Minha viagem"
        private set

    var displayCurrency: Currency = Currency.BRL
        private set

    // ...funções...
}
```

Tradução para palavras simples:

- `object TripRepository` → é uma **classe única** (singleton). Existe **apenas uma** instância dela no app inteiro.
- `participants` → uma **lista mutável** (`MutableList`) de `Participant`. Guarda quem vai fazer a viagem.
- `expenses` → uma **lista mutável** de `Expense`. Guarda todas as despesas cadastradas.
- `tripName` → nome da viagem. Começa como "Minha viagem".
- `displayCurrency` → moeda usada para exibir valores na tela (por exemplo, BRL, USD, EUR). Isso é importante para o conversor de moedas.

Enquanto o app está aberto, **todas as telas** enxergam o mesmo `TripRepository`. É como um "banco de dados em memória" bem simples.

---

## 2. Como os dados são guardados

### 2.1 Participantes

Os participantes são representados por uma classe simples:

```kotlin
data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
```

No `TripRepository` temos:

```kotlin
private val participants = mutableListOf<Participant>()

fun getParticipants(): List<Participant> = participants.toList()

fun addParticipant(name: String): Participant {
    require(name.isNotBlank()) { "Nome não pode ser vazio" }
    val exists = participants.any { it.name.equals(name.trim(), ignoreCase = true) }
    if (exists) error("Participante já existe")
    val participant = Participant(name = name.trim())
    participants.add(participant)
    return participant
}

fun removeParticipant(id: String) {
    participants.removeAll { it.id == id }
    // também atualiza as despesas, removendo esse participante de divisões antigas
    val updatedExpenses = expenses.map { expense ->
        val filteredShared = expense.sharedParticipantIds.filter { it != id }
        val filteredCharges = expense.personalCharges.filter { it.participantId != id }
        expense.copy(
            sharedParticipantIds = filteredShared,
            personalCharges = filteredCharges
        )
    }
    expenses.clear()
    expenses.addAll(updatedExpenses)
}
```

Em português:

- `getParticipants()` → devolve uma cópia da lista de participantes para quem está lendo.
- `addParticipant(name)` →
  - não deixa adicionar nome vazio;
  - não deixa adicionar nome repetido (ignora maiúsculas/minúsculas);
  - cria um `Participant` novo e coloca na lista.
- `removeParticipant(id)` →
  - tira o participante da lista;
  - também limpa esse participante de qualquer despesa antiga (para não ficar inconsistência).

### 2.2 Despesas

As despesas são representadas por:

```kotlin
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val payerId: String,
    val total: Money,
    val sharedParticipantIds: List<String>,
    val personalCharges: List<PersonalCharge>
)
```

No repositório:

```kotlin
private val expenses = mutableListOf<Expense>()

fun getExpenses(): List<Expense> = expenses.toList()

fun getExpense(id: String): Expense? = expenses.find { it.id == id }

fun addExpense(...)
fun updateExpense(...)
fun removeExpense(expenseId: String)
```

Ele usa uma função interna `buildExpense` para criar ou atualizar uma despesa, garantindo que a estrutura fique sempre organizada:

```kotlin
private fun buildExpense(
    id: String,
    title: String,
    payerId: String,
    amount: Money,
    sharedParticipantIds: List<String>,
    personalCharges: List<PersonalCharge>
): Expense {
    val cleanedShared = sharedParticipantIds.distinct()
    val validPersonalCharges = personalCharges
        .filter { charge -> participants.any { it.id == charge.participantId } }
    return Expense(
        id = id,
        title = title.trim().ifBlank { "Despesa sem nome" },
        payerId = payerId,
        total = amount,
        sharedParticipantIds = cleanedShared,
        personalCharges = validPersonalCharges
    )
}
```

Resumindo:

- `addExpense(...)` → cria uma nova `Expense` com um `id` único e coloca na lista.
- `updateExpense(...)` → procura a despesa pelo `id` e substitui pelo novo conteúdo.
- `removeExpense(expenseId)` → tira a despesa da lista.

### 2.3 Nome da viagem e moeda de exibição

```kotlin
var tripName: String = "Minha viagem"
    private set

var displayCurrency: Currency = Currency.BRL
    private set

fun setTripName(name: String) {
    tripName = name.ifBlank { "Minha viagem" }
}

fun updateDisplayCurrency(currency: Currency) {
    displayCurrency = currency
}
```

- `tripName` é usado nas toolbars (por exemplo, título da tela de despesas e de fechamento).
- `displayCurrency` diz em qual moeda os totais devem ser exibidos (o cálculo interno é sempre em BRL, mas o valor final é convertido para essa moeda usando o `CurrencyConverter`).

---

## 3. Como o TripRepository participa de cada tela

Aqui vamos conectar o repositório com a experiência do usuário, tela por tela.

### 3.1 Tela inicial (`MainActivity`)

Quando o usuário digita o nome da viagem e toca em "Começar":

```kotlin
val tripName = tripNameInput.text?.toString().orEmpty()
TripRepository.setTripName(tripName)
startActivity(Intent(this, ParticipantsActivity::class.java))
```

- A tela lê o texto do `EditText`;
- chama `TripRepository.setTripName(...)` para gravar esse nome em memória;
- abre a tela de participantes sem precisar passar o nome na `Intent`, porque a próxima tela vai ler direto do repositório.

### 3.2 Participantes (`ParticipantsActivity`)

- **Para mostrar a lista:**

  ```kotlin
  private fun refreshList() {
      val participants = TripRepository.getParticipants()
      adapter.submitList(participants)
      // ...atualiza texto "vazio" e botão...
  }
  ```

  A Activity pede a lista do repositório e entrega para o adapter da RecyclerView.

- **Para adicionar um participante:**

  ```kotlin
  runCatching { TripRepository.addParticipant(name) }
      .onSuccess {
          participantInput.text?.clear()
          refreshList()
      }
  ```

  Ela chama `addParticipant` e depois recarrega a lista.

- **Para remover um participante:**

  Quem dispara a remoção é o adapter de participantes, mas ele apenas chama a função do repositório:

  ```kotlin
  private val adapter = ParticipantAdapter { participant ->
      TripRepository.removeParticipant(participant.id)
      refreshList()
  }
  ```

  Assim, a lógica de “como remover” fica no `TripRepository`, e a tela só decide *quando* remover.

### 3.3 Despesas (lista + overview)

Na `ExpensesActivity` e na `OverviewActivity`, o repositório é usado para **ler** dados:

- `TripRepository.tripName` → título da toolbar.
- `TripRepository.getExpenses()` → lista de despesas que o `ExpensesListFragment` mostra.
- `TripRepository.displayCurrency` → moeda atual para exibir os valores.

Na `OverviewActivity`, por exemplo, quando o usuário troca a moeda no spinner:

```kotlin
currencySpinner.onItemSelected<Currency> { currency ->
    TripRepository.updateDisplayCurrency(currency)
    updateSummary()
}
```

E o resumo usa os dados do repositório:

```kotlin
val participants = TripRepository.getParticipants()
val expenses = TripRepository.getExpenses()
val currency = TripRepository.displayCurrency

val totalSpent = expenses.sumOf { expense ->
    val base = CurrencyConverter.convert(
        expense.total.amount,
        expense.total.currency,
        Currency.BRL
    )
    base
}
val formattedTotal = Money(totalSpent, Currency.BRL).convertTo(currency).format()
```

### 3.4 Cadastro/edição de despesas (`AddExpenseActivity`)

Esta tela tanto lê quanto escreve no repositório:

- **Para saber quem são os participantes:**

  ```kotlin
  participants = TripRepository.getParticipants()
  ```

  Ela precisa disso para preencher os chips e os spinners.

- **Para carregar uma despesa existente (modo edição):**

  ```kotlin
  editingExpenseId = intent.getStringExtra(EXTRA_EXPENSE_ID)
  val expenseToEdit = editingExpenseId?.let { TripRepository.getExpense(it) }
  ```

- **Para salvar uma nova despesa:**

  ```kotlin
  TripRepository.addExpense(
      title = title,
      payerId = payer.id,
      amount = money,
      sharedParticipantIds = selectedParticipants,
      personalCharges = personalChargesCopy
  )
  ```

- **Para atualizar uma despesa existente:**

  ```kotlin
  TripRepository.updateExpense(
      id = editingExpenseId!!,
      title = title,
      payerId = payer.id,
      amount = money,
      sharedParticipantIds = selectedParticipants,
      personalCharges = personalChargesCopy
  )
  ```

### 3.5 Fechamento (`SettlementActivity`)

Esta tela não altera nada, só lê dados para montar o resultado final:

```kotlin
val participants = TripRepository.getParticipants()
val expenses = TripRepository.getExpenses()
val summary = ExpenseCalculator.buildSummary(participants, expenses)
val currency = TripRepository.displayCurrency
```

Com essas informações, ela calcula total por pessoa, saldos e transferências sugeridas. Se o usuário trocar a moeda no spinner, a Activity chama `TripRepository.updateDisplayCurrency(currency)` e recalcula os textos.

---

## 4. Por que usar o TripRepository em vez de passar listas por Intent

O professor comentou a ideia de passar dados via `Intent` e `Bundle`. Isso funciona bem para **valores simples** (por exemplo, um `String` ou um `Int`). Mas, para esse app, passar as listas assim deixaria o código mais complicado do que usar o repositório. Veja por quê.

### 4.1 O que seria necessário sem TripRepository

Sem um repositório central, teríamos que:

1. **Colocar as listas nas Activities**
   - Por exemplo, `ParticipantsActivity` teria uma lista interna de `participants`.
   - Ao abrir `ExpensesActivity`, precisaríamos enviar essa lista pela `Intent`.

2. **Empacotar as listas**
   - O sistema não sabe enviar `List<Participant>` puro.
   - Cada modelo (`Participant`, `Expense`, etc.) teria que virar `Serializable` ou `Parcelable`, o que é conteúdo novo e mais avançado.

3. **Devolver resultados**
   - Quando `AddExpenseActivity` criasse uma despesa nova, ela teria que devolver a lista atualizada para `ExpensesActivity` usando `startActivityForResult` ou `registerForActivityResult`.
   - Isso introduz mais conceitos que normalmente aparecem depois no curso.

4. **Manter tudo sincronizado**
   - `SettlementActivity` e `OverviewActivity` também precisam das mesmas listas.
   - Teríamos que repassar listas atualizadas entre várias telas, correndo o risco de ficar com versões diferentes dos dados.

### 4.2 O que ganhamos com o TripRepository

Usando o repositório, o cenário fica mais simples:

- As listas vivem em **um lugar só** (`TripRepository`).
- Qualquer Activity/Fragment que precisar de dados faz perguntas simples:
  - `TripRepository.getParticipants()`
  - `TripRepository.getExpenses()`
  - `TripRepository.tripName`
  - `TripRepository.displayCurrency`
- Quando alguma tela precisa mudar algo:
  - chama `addParticipant`, `removeParticipant`, `addExpense`, `updateExpense`, etc.
  - e depois atualiza sua própria UI.

Do ponto de vista pedagógico (alguém que está começando em Kotlin):

- O `TripRepository` é **“só uma classe com listas e funções”**, usando conceitos que vocês já viram: classes, objetos, listas, funções.
- Ele evita que você tenha que aprender `Parcelable`, `Serializable` e Activity result apenas para esse trabalho.
- Fica mais fácil explicar na apresentação: 

> “Eu centralizei os dados da viagem em um repositório em memória (`TripRepository`). Todas as telas pedem e atualizam os dados por ele. Isso deixa o código mais organizado do que ficar empacotando listas em Intents o tempo todo.”

---

## 5. Resumo em linguagem bem simples

- **O que é o TripRepository?**
  - Um “caderno de anotações” do app que vive na memória enquanto o app está aberto.

- **O que ele guarda?**
  - Nome da viagem.
  - Lista de pessoas que vão na viagem.
  - Lista de despesas da viagem.
  - Qual moeda deve aparecer nas telas.

- **Quem usa esse caderno?**
  - Todas as telas: cadastro de participantes, lista de despesas, formulário de novas despesas, visão geral da viagem e fechamento.

- **Por que não mandar tudo por Intent?**
  - Porque seria mais complicado empacotar e desempacotar listas em todas as telas.
  - Você teria que aprender assuntos novos (Parcelable, Serializable, resultados entre Activities).
  - Fica mais difícil garantir que todas as telas estejam vendo os mesmos dados.

Com o `TripRepository`, o código continua dentro do que vocês viram em aula (classes + listas + funções) e o projeto fica mais fácil de entender, manter e explicar para o professor.

