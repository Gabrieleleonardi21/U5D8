/**
 * Wrapper unico su fetch: aggiunge Content-Type JSON e traduce gli errori del backend
 * (ErrorPayload) in un throw con il messaggio in italiano, cosi' i componenti fanno solo try/catch.
 */
export async function api(percorso, opzioni = {}) {
  const headers = { 'Content-Type': 'application/json', ...opzioni.headers }
  let risposta
  try {
    risposta = await fetch(percorso, { ...opzioni, headers })
  } catch {
    // fetch fallisce solo per problemi di rete: il backend e' spento o irraggiungibile
    throw new Error('Impossibile contattare il server: controlla che il backend sia avviato e riprova')
  }
  if (!risposta.ok) {
    throw new Error(await leggiMessaggioErrore(risposta))
  }
  // 204 (PATCH elimina) non ha corpo
  if (risposta.status === 204) {
    return null
  }
  return risposta.json()
}

// Scorciatoie per non ripetere method + JSON.stringify in ogni componente
export function post(percorso, body) {
  return api(percorso, { method: 'POST', body: JSON.stringify(body) })
}

export function patch(percorso) {
  return api(percorso, { method: 'PATCH' })
}

async function leggiMessaggioErrore(risposta) {
  try {
    const corpo = await risposta.json()
    if (corpo.messaggio) return corpo.messaggio
  } catch {
    /* corpo non JSON */
  }
  return `Errore ${risposta.status}`
}
