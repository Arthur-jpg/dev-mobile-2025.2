# Super Heróis

---

## 1. Visão Geral do Aplicativo

O aplicativo é um quiz de personalidade que, a partir das respostas do usuário, determina qual super-herói mais combina com ele. O fluxo principal consiste em três telas (Activities):

- `MainActivity` — Tela inicial: coleta o nome do usuário, checkbox de aceitação de termos e botão para iniciar o quiz.
- `QuizActivity` — Tela de perguntas: exibe perguntas de múltipla escolha (RadioGroup), controla o progresso (ProgressBar) e computa pontuação por herói.
- `ResultActivity` — Tela de resultado: mostra o herói vencedor (nome, imagem, descrição) e oferece opções de compartilhar o resultado e abrir o repositório no GitHub (Intents implícitas).

Recursos visuais e UI seguem Material Design (Material Components): `MaterialButton`, `MaterialCardView`, `TextInputLayout`, `MaterialRadioButton` etc. As imagens dos heróis são `VectorDrawable` simples em `res/drawable/`.

---

## 2. Estrutura de Arquivos Relevantes

Principais arquivos criados/alterados (caminhos relativos):

- `app/src/main/java/com/example/superheroi/MainActivity.kt`
- `app/src/main/java/com/example/superheroi/QuizActivity.kt`
- `app/src/main/java/com/example/superheroi/ResultActivity.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/activity_quiz.xml`
- `app/src/main/res/layout/activity_result.xml`
- `app/src/main/res/values/strings.xml` (muitas strings adicionadas)
- `app/src/main/res/values/colors.xml` (paleta vibrante)
- `app/src/main/res/drawable/hero_*.xml` (vetores para cada herói)
- `app/src/main/AndroidManifest.xml`
- `RELATORIO_PROFESSOR.md` (este relatório)

---

## 3. Abordagem de Programação — Orientação a Objetos

O projeto usa Kotlin, idiomático e orientado a objetos. Principais pontos OOP aplicados:

1. Data classes
   - `QuizActivity` define uma `data class Question(val text: Int, val options: List<Int>, val scores: List<String>)`.
   - Essa `data class` encapsula os dados necessários para cada pergunta (referências a recursos de string para enunciado e opções e um `List` que mapeia cada opção para o `heroId`).
   - Vantagem: imutabilidade parcial (os campos são val) e fácil leitura/escrita.

2. Modelagem simples de herói
   - Em `ResultActivity`, existe a `data class Hero(val id: String, val nameRes: Int, val descRes: Int, val imageRes: Int)` para manter as propriedades do herói.
   - Os `Hero` são armazenados em um `Map<String, Hero>` indexado pelo `id` (ex.: "ironman", "hulk" etc.).

3. Encapsulamento e responsabilidades
   - Cada Activity tem responsabilidade única: UI e interação do usuário (View + Controller leve). Lógica do quiz (perguntas, scores) está concentrada em `QuizActivity`.
   - Recursos (strings, cores, drawables) ficam em `res/` para separação de apresentação e lógica.

4. Coleções e Mutabilidade
   - `heroScores` é um `mutableMapOf(...)` que mantém as pontuações cumulativas por `heroId`.
   - `questions` é um `List<Question>` imutável após construção.

---

## 4. Implementação das Activities e Ciclo de Vida

### 4.1 MainActivity

- Layout (`activity_main.xml`) contém:
  - `TextView` (título), `ImageView` (logo), `TextInputEditText` (nome), `CheckBox` (termos) e `MaterialButton` (iniciar quiz).
- Comportamento:
  - Ao clicar em "Começar Quiz" o app valida se o nome foi preenchido e se o checkbox foi marcado.
  - Em caso positivo, cria um `Intent` explícito para `QuizActivity`, passando o `USER_NAME` via `intent.putExtra("USER_NAME", name)`.
- Observações:
  - `MainActivity` é `exported="true"` no manifest e é o `LAUNCHER`.

### 4.2 QuizActivity

Responsabilidade: apresenta as perguntas, recebe as respostas, acumula pontuação e navega para `ResultActivity`.

Pontos-chave da implementação:

- Views:
  - `TextView` para enunciado; `RadioGroup` com até 7 `MaterialRadioButton`; `ProgressBar` horizontal; `MaterialButton` para avançar/ver resultado.
- Modelo de perguntas:
  - `questions: List<Question>` contém 6 perguntas. As duas primeiras têm 4 opções; as demais têm 7.
  - Cada `Question` contém `scores: List<String>` com os `heroId`s, na mesma ordem das opções.
- Lógica de apresentação:
  - `loadQuestion()` carrega o enunciado e, dinamicamente, exibe/oculta os botões de opção conforme `question.options.size`. Isso garante que perguntas com 4 opções não mostrem botões 5-7.
  - `allRadioButtons` é uma lista que facilita iterar e atualizar as opções.
- Registro da resposta:
  - `handleNextButton()` verifica a opção selecionada (via `answersRadioGroup.checkedRadioButtonId`) e usa um `when` para obter o índice selecionado.
  - Usa-se esse índice para buscar o `heroId` correspondente em `questions[currentQuestionIndex].scores[selectedIndex]` e incrementar `heroScores[heroId]`.
- Progressão:
  - Se houver próxima pergunta, incrementa `currentQuestionIndex` e chama `loadQuestion()`; caso contrário, chama `showResult()`.

Gerenciamento de estado (importante para o professor):

- Problema: mudanças de configuração (ex.: rotação) recriam a Activity. Sem salvamento, o progresso seria perdido.
- Solução implementada:
  - `onSaveInstanceState(outState)` salva:
    - `CURRENT_INDEX` — índice da pergunta atual (Int)
    - `HERO_SCORES` — `IntArray` com as pontuações na ordem definida por `HERO_IDS` (lista estável de IDs)
    - `CHECKED_ID` — id do RadioButton selecionado (Int)
  - Em `onCreate(savedInstanceState)` o código restaura, se presente:
    - `currentQuestionIndex` e `heroScores` usando `HERO_IDS` para mapear o array para o `mutableMapOf`.
    - Após `loadQuestion()`, a seleção do RadioButton (se havia) é reinserida em `answersRadioGroup.check(checkedId)` via `post {}` para garantir que as views já estejam montadas.
  - Resultado: rotação ou recriação não perde o progresso do quiz.

### 4.3 ResultActivity

Responsabilidade: receber o `HERO_ID` via Intent, localizar o `Hero` correspondente e exibir nome, descrição e drawable.

- Botões/Intents:
  - `Compartilhar Resultado` -> `Intent(Intent.ACTION_SEND)` com `type = "text/plain"` e `Intent.createChooser(...)`. Texto usa `R.string.share_text` com `String.format` (Kotlin `getString(R.string.share_text, heroName)`).
  - `Ver Projeto no GitHub` -> `Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Arthur-jpg"))` (Intent implícita que abre navegador).
  - `Fazer Novamente` -> volta para `MainActivity` limpando stack (`FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK`).
- Observações:
  - `Hero` é buscado do `Map` por `heroId`. Se `heroId` não existir, usa-se `ironman` como fallback.

---

## 5. Como é feita a escolha do super-herói (algoritmo de decisão)

A escolha do herói é baseada em um sistema de pontuação simples por opção. Cada opção de cada pergunta está associada a exatamente um `heroId`. Quando o usuário escolhe uma opção, o herói associado recebe +1 ponto. Ao final do quiz (após todas as perguntas), o herói com maior pontuação vence.

Detalhes:
- `heroScores` é um `MutableMap<String, Int>` com todos os IDs dos heróis inicializados em 0.
- Para cada resposta:
  1. Descobre-se o índice da opção selecionada (0..N-1).
  2. Usa-se `questions[currentQuestionIndex].scores[selectedIndex]` para obter o `heroId` da opção.
  3. Incrementa-se `heroScores[heroId]`.
- Ao final: `heroScores.maxByOrNull { it.value }?.key` retorna o `heroId` vencedor. Em caso de empate, `maxByOrNull` retorna o primeiro maior (por ordem de chave interna do Map), portanto a ordem de inicialização dos heróis pode influenciar o desempate — nota abaixo.

Desempate:
- Implementado: agora existe uma lógica interativa de desempate. Se, ao final do quiz, mais de um herói tiver a pontuação máxima, o aplicativo abre uma nova tela — `TiebreakerActivity` — que mostra os heróis empatados em forma de cards (imagem, nome, descrição) e permite que o próprio usuário escolha qual dos empatados prefere como resultado final.

- Alterações principais relacionadas ao desempate:
  - `QuizActivity.showResult()` detecta empates (todos os `heroId` com a pontuação máxima) e, se houver mais de um, cria um `Intent` para `TiebreakerActivity` passando um `ArrayList<String>` com a chave `TIED_HERO_IDS`.
  - `TiebreakerActivity` (`app/src/main/java/com/example/superheroi/TiebreakerActivity.kt`) recebe os IDs, monta dinamicamente cards com as informações dos heróis (usando `MaterialCardView`) e, ao clicar em "Escolher este herói", navega para `ResultActivity` com o `HERO_ID` selecionado.
  - Arquivos novos/alterados: `TiebreakerActivity.kt`, `activity_tiebreaker.xml`, atualização do `AndroidManifest.xml` (registro da nova activity) e adição de strings `tiebreaker_title`, `tiebreaker_subtitle` e `choose_hero` em `strings.xml`.

- Logs: o fluxo de desempate produz logs para facilitar debug (`Log.d`) em `QuizActivity` e `TiebreakerActivity` que mostram as pontuações finais, os IDs empatados e a escolha do usuário.

- Observações: anteriormente o empate era resolvido implicitamente (o primeiro com pontuação máxima vencia). A nova abordagem dá controle ao usuário, evitando decisões arbitrárias e melhorando a experiência.

---

## 6. Mapeamento completo das perguntas → opções → heróis

Abaixo está o mapeamento exato das perguntas e de qual heroId cada opção aponta (essa informação está implementada em `QuizActivity.questions`):

Observação: as strings estão em `res/values/strings.xml` (referências `R.string.xxx`). Aqui envio um resumo legível para o professor.

### Pergunta 1 (4 opções)
Enunciado: `question_1` — Ex.: "O que você prefere?"
- opção 1 (`q1_option1`) → `ironman`
- opção 2 (`q1_option2`) → `hulk`
- opção 3 (`q1_option3`) → `captain`
- opção 4 (`q1_option4`) → `strange`

### Pergunta 2 (4 opções)
Enunciado: `question_2` — Ex.: "Como você resolve problemas?"
- opção 1 (`q2_option1`) → `ironman`
- opção 2 (`q2_option2`) → `hulk`
- opção 3 (`q2_option3`) → `captain`
- opção 4 (`q2_option4`) → `strange`

### Pergunta 3 (7 opções)
Enunciado: `question_3` — Ex.: "Qual é seu maior valor?"
- `q3_option1` → `ironman`
- `q3_option2` → `hulk`
- `q3_option3` → `captain`
- `q3_option4` → `strange`
- `q3_option5` → `thor`
- `q3_option6` → `blackwidow`
- `q3_option7` → `spiderman`

### Pergunta 4 (7 opções)
Enunciado: `question_4` — Ex.: "Onde você se sente mais confortável?"
- `q4_option1` → `ironman`
- `q4_option2` → `hulk`
- `q4_option3` → `captain`
- `q4_option4` → `strange`
- `q4_option5` → `thor`
- `q4_option6` → `blackwidow`
- `q4_option7` → `spiderman`

### Pergunta 5 (7 opções)
Enunciado: `question_5` — Ex.: "Qual é sua maior habilidade?"
- `q5_option1` → `ironman`
- `q5_option2` → `hulk`
- `q5_option3` → `captain`
- `q5_option4` → `strange`
- `q5_option5` → `thor`
- `q5_option6` → `blackwidow`
- `q5_option7` → `spiderman`

### Pergunta 6 (7 opções)
Enunciado: `question_6` — Ex.: "Como você enfrenta o perigo?"
- `q6_option1` → `ironman`
- `q6_option2` → `hulk`
- `q6_option3` → `captain`
- `q6_option4` → `strange`
- `q6_option5` → `thor`
- `q6_option6` → `blackwidow`
- `q6_option7` → `spiderman`

Esses mapeamentos vêm diretamente da inicialização das `Question(...)` em `QuizActivity.kt` (campo `scores`), portanto, para alterar o comportamento do quiz (por exemplo, associar uma opção diferente a um herói) basta editar essa lista.

---

## 7. Recursos e Internacionalização

- Todas as strings do app são mantidas em `res/values/strings.xml`, facilitando tradução futura.
- Cores e temas (Material 3) em `res/values/colors.xml` e `res/values/themes.xml`.
- Drawables de heróis em `res/drawable/hero_*.xml` são vetores; se for necessário usar imagens reais, substitua por `PNG`/`WebP` mantendo o mesmo nome (ex.: `hero_spiderman.png`).

---

## 8. Testes manuais sugeridos (roteiro para o professor avaliar)

1. Fluxo básico
   - Abrir app, inserir nome, marcar checkbox, iniciar quiz.
   - Responder todas as perguntas e verificar resultado plausível.
2. Validações
   - Tentar iniciar sem nome → ver `Toast` com `name_required`.
   - Tentar iniciar sem checkbox → ver `Toast` com `terms_required`.
   - Em `QuizActivity`, tentar avançar sem responder → ver `Toast` com `please_answer`.
3. Estado ao girar dispositivo
   - Iniciar quiz e, em pergunta 3, girar a tela. Verificar se a pergunta atual e a opção selecionada permanecem.
4. Compartilhamento e GitHub
   - Na tela de resultado, clicar em "Compartilhar Resultado" e escolher um app (WhatsApp/Telegram/Email) — conferir texto.
   - Clicar em "Ver Projeto no GitHub" — conferir abertura do navegador no link correto.
5. Desempate (opcional)
   - Construir respostas para gerar empate (ex.: distribuir pontos) e observar qual herói é retornado (comportamento atual: primeiro maior nas estruturas internas).

---

## 9. Decisões de projeto e possibilidades de melhoria

Decisões tomadas:
- Escolha de usar `Activity` ao invés de `Fragment` por simplicidade pedagógica e cumprimento do requisito de 3 telas distintas.
- Uso de `data class` para `Question` e `Hero` para modelagem declarativa.
- Lógica centralizada em `QuizActivity` para manter tudo relacionado ao quiz em um só lugar.

Melhorias possíveis:
- Migrar para arquitetura MVVM com `ViewModel` para separar UI e lógica e permitir teste unitário do cálculo de pontuação.
- Persistência local (SQLite/Room) para salvar histórico de resultados.
- Implementar desempate mais justo (ex.: peso por importância da pergunta ou mostrar múltiplos heróis em empate).
- Suportar imagens reais e cache (Glide/Picasso) e incluir testes automatizados UI (Espresso).
- Internacionalização: adicionar `strings.xml (pt-BR)` e outros idiomas.

---

## 10. Notas de Build & Execução

- Abra o projeto no Android Studio.
- Sincronize o Gradle (provavelmente já configurado no repositório fornecido).
- Execute em um emulador ou dispositivo.

Se quiser, eu posso também rodar um `./gradlew assembleDebug` aqui e reportar o resultado (se me autorizar a executar comandos). Caso prefira testar localmente, os passos acima são suficientes.

---

## 11. Conclusão

O aplicativo entrega um quiz interativo, com UI baseada em Material Design, salvamento de estado em mudanças de configuração, e uso de Intents implícitas para ações de compartilhamento e abertura de URLs. A estrutura é simples, extensível e documentada — o que permite ao professor rodar e avaliar o projeto com facilidade. O mapeamento entre opções e heróis é declarado em `QuizActivity` e pode ser alterado facilmente para ajustar o comportamento do quiz.

Se desejar, posso:
- Adicionar testes unitários para a função de cálculo de pontuação;
- Implementar `ViewModel` e separar lógica da UI;
- Gerar um APK de release pronto para entrega.

---

Arquivo criado por solicitação do aluno para apresentação e avaliação.
