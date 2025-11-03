# Relatório do Projeto - Quiz Super Herói

---

## O que o App Faz

Um quiz de personalidade que descobre qual super-herói você seria. O usuário responde 6 perguntas e recebe um resultado baseado nas suas escolhas.

---

## Classes Principais

### MainActivity
**O que faz:** Tela inicial do app.
- Pede o nome do usuário
- Tem um checkbox para aceitar termos
- Valida se o nome foi digitado e se aceitou os termos
- Quando o usuário clica em "Iniciar Quiz", abre a QuizActivity

### QuizActivity
**O que faz:** Mostra as perguntas do quiz.
- Exibe 6 perguntas, uma de cada vez
- Cada pergunta tem opções de múltipla escolha (RadioButtons)
- Conta quantos pontos cada herói recebe baseado nas respostas
- Mostra uma barra de progresso para o usuário saber em qual pergunta está
- Quando termina, calcula qual herói teve mais pontos
- Se houver empate, abre a TiebreakerActivity
- Se houver um vencedor, abre a ResultActivity

**Como funciona a pontuação:**
- Cada opção de resposta dá 1 ponto para um herói específico
- Por exemplo: na pergunta "Qual é o seu maior medo?", se escolher "Perder o controle", dá 1 ponto para o Homem de Ferro
- No final, soma todos os pontos e vê qual herói ganhou

### TiebreakerActivity
**O que faz:** Tela de desempate quando dois ou mais heróis empatam.
- Mostra cards com foto, nome e descrição de cada herói empatado
- Permite o usuário escolher qual herói prefere
- Quando escolhe, abre a ResultActivity com o herói selecionado

### ResultActivity
**O que faz:** Mostra o resultado final.
- Exibe a foto, nome e descrição do herói que você é
- Tem 3 botões:
  - **Compartilhar:** abre WhatsApp, Instagram, etc para compartilhar o resultado
  - **Ver no GitHub:** abre o navegador com o link do projeto
  - **Reiniciar Quiz:** volta para o início

---

## Perguntas e Heróis

O quiz tem 6 perguntas. As 2 primeiras têm 4 opções, as outras 4 têm 7 opções.

**Heróis disponíveis:**
1. Homem de Ferro
2. Hulk
3. Capitão América
4. Doutor Estranho
5. Thor
6. Viúva Negra
7. Homem-Aranha

Cada opção de cada pergunta dá ponto para um herói diferente. Por exemplo:
- Pergunta 1, Opção 1 → Homem de Ferro
- Pergunta 1, Opção 2 → Hulk
- E assim por diante...

---

## Como Rodar o Projeto

1. Abra no Android Studio
2. Clique em "Run" (ou Shift+F10)
3. Escolha um emulador ou dispositivo conectado
4. O app vai instalar e abrir

---

## O que Foi Usado

- **Linguagem:** Kotlin
- **Views:** EditText, Button, RadioButton, CheckBox, ImageView, ProgressBar
- **Navegação:** Intents (para trocar de tela)
- **Intents Implícitas:** Para compartilhar e abrir links
- **Collections:** List e MutableMap para armazenar perguntas e pontuações
- **Log:** Para debugar e ver o que está acontecendo

---

Projeto criado para a disciplina de Desenvolvimento Mobile.
