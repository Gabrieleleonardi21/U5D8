# U5D8 — Clone basic di ChatGPT

Chat multiple con un agente LLM (OpenRouter). Backend Spring Boot 4 + PostgreSQL, frontend React + Vite.

## Struttura

- `BE/` — API REST (Java 25, Spring Boot 4.1, JPA, RestClient verso OpenRouter)
- `FE/` — interfaccia (React 19, Vite; in dev inoltra `/api` al backend sulla porta 3001)

## Come funziona

- Le istruzioni dell'agente (carattere e regole) stanno in `BE/src/main/resources/agente/istruzioni.txt`
  e vengono caricate una sola volta all'avvio: non sono scritte nel codice.
- Il servizio LLM non ricorda le conversazioni: a ogni richiesta il backend invia le istruzioni +
  gli ultimi N messaggi della chat (`app.llm.max-messaggi-contesto`, default 20), esclusi quelli di errore.
- Flusso invio messaggio: salva il messaggio utente → chiama l'LLM (max 3 tentativi) → salva la risposta
  e somma i token consumati (input + output) alla chat. Se tutti i tentativi falliscono viene salvata la
  risposta "Servizio attualmente non disponibile" con `isErrorMessage = true`.
- La chiamata HTTP all'LLM resta fuori dalle transazioni DB: il messaggio utente e' gia' salvato prima di
  contattare il servizio e nessuna connessione al database resta bloccata durante l'attesa.
- Cancellazione logica delle chat (`isDeleted`), cronologia paginata (50 per pagina, dalla piu' recente).

## Setup

1. Database: `createdb -U postgres u5d8`
2. Segreti (variabili d'ambiente oppure `BE/env.properties`, non versionato):
   - `DB_PASSWORD` (e `DB_USERNAME`, default `postgres`)
   - `OPENROUTER_API_KEY` — chiave OpenRouter (`sk-or-v1-...`); i modelli free usati, in ordine di fallback, sono in `app.llm.modelli`
3. Backend: `cd BE && mvn spring-boot:run` (porta 3001)
4. Frontend: `cd FE && npm install && npm run dev` → http://localhost:5173

## Endpoint

| Metodo | Percorso | Descrizione |
|---|---|---|
| POST | `/api/chat` | Crea una chat `{ "nome": "..." }` (409 se il nome esiste) |
| GET | `/api/chat` | Chat non cancellate, ordinate per ultimo messaggio |
| PATCH | `/api/chat/{id}/elimina` | Cancellazione logica |
| POST | `/api/messaggi` | `{ "chatId", "testo" }` → risposta dell'agente |
| GET | `/api/chat/{chatId}/messaggi?page=0&size=50` | Cronologia (createdAt DESC) |

Gli errori hanno sempre la forma `{ "status", "messaggio", "timestamp" }`.
