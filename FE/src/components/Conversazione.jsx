import { useEffect, useRef, useState } from 'react'
import Avviso from './Avviso.jsx'
import { inviaMessaggio, messaggiDi } from '../services/chat.js'

/**
 * Unisce pagine diverse della cronologia senza duplicati (Map per id) e nell'ordine deciso
 * dal server (createdAt). Serve perche' i messaggi nuovi spostano gli offset delle pagine successive.
 */
function unisci(esistenti, nuovi) {
  const perId = new Map(esistenti.map((m) => [m.id, m]))
  nuovi.forEach((m) => perId.set(m.id, m))
  return [...perId.values()].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
}

/**
 * La conversazione aperta: cronologia paginata, form di invio e stato di attesa.
 * Il messaggio dell'utente compare subito (bolla locale) e viene rimpiazzato dalla versione
 * del server alla prossima lettura; la risposta dell'agente arriva dalla POST stessa.
 */
function Conversazione({ chat, onRisposta }) {
  const [messaggi, setMessaggi] = useState([])
  const [pagina, setPagina] = useState(0)
  const [altrePagine, setAltrePagine] = useState(false)
  const [testo, setTesto] = useState('')
  const [inAttesa, setInAttesa] = useState(false)
  const [errore, setErrore] = useState('')
  const fondo = useRef(null)

  // All'apertura carico la pagina 0 (i piu' recenti). Il genitore monta il componente con key = chat.id:
  // al cambio chat React lo ricrea da zero, quindi non serve resettare lo stato a mano
  useEffect(() => {
    caricaPagina(chat.id, 0)
  }, [chat.id])

  // Ogni nuovo messaggio porta la lista in fondo
  useEffect(() => {
    fondo.current?.scrollIntoView()
  }, [messaggi])

  async function caricaPagina(chatId, numero) {
    try {
      const risultato = await messaggiDi(chatId, numero)
      setMessaggi((prev) => unisci(prev, risultato.content))
      setPagina(numero)
      setAltrePagine(numero + 1 < risultato.page.totalPages)
    } catch (e) {
      setErrore(e.message)
    }
  }

  async function invia(e) {
    e.preventDefault()
    const daInviare = testo.trim()
    if (!daInviare || inAttesa) return
    setErrore('')
    setTesto('')
    setInAttesa(true)
    // Bolla provvisoria: id locale finche' non rileggo la cronologia dal server
    const locale = { id: `locale-${Date.now()}`, mittente: 'USER', testo: daInviare, createdAt: new Date().toISOString(), isErrorMessage: false }
    setMessaggi((prev) => [...prev, locale])
    try {
      const risposta = await inviaMessaggio(chat.id, daInviare)
      setMessaggi((prev) => [...prev, risposta])
      // La lista chat cambia ordine e conteggio token: la ricarica il genitore
      onRisposta()
    } catch (err) {
      // Errore del backend (es. chat cancellata, validazione): tolgo la bolla e rimetto il testo nell'input
      setMessaggi((prev) => prev.filter((m) => m.id !== locale.id))
      setTesto(daInviare)
      setErrore(err.message)
    } finally {
      setInAttesa(false)
    }
  }

  function classeMessaggio(m) {
    if (m.isErrorMessage) return 'bolla errore'
    if (m.mittente === 'USER') return 'bolla utente'
    return 'bolla'
  }

  function ora(iso) {
    return new Date(iso).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
  }

  let bottonePrecedenti = null
  if (altrePagine) {
    bottonePrecedenti = (
      <li className="precedenti">
        <button type="button" className="secondario" onClick={() => caricaPagina(chat.id, pagina + 1)}>Carica messaggi precedenti</button>
      </li>
    )
  }

  let bollaAttesa = null
  if (inAttesa) {
    bollaAttesa = (
      <li className="bolla attesa" aria-live="polite">
        <span className="puntini" aria-hidden="true"><i /><i /><i /></span>
        <span className="visivamente-nascosto">Epi sta scrivendo</span>
      </li>
    )
  }

  return (
    <>
      <header className="intestazione">{chat.nome}</header>
      <ul className="messaggi">
        {bottonePrecedenti}
        {messaggi.map((m) => (
          <li key={m.id} className={classeMessaggio(m)}>
            <p>{m.testo}</p>
            <small>{ora(m.createdAt)}</small>
          </li>
        ))}
        {bollaAttesa}
        <li ref={fondo} />
      </ul>
      <Avviso testo={errore} />
      <form className="invio" onSubmit={invia}>
        <input value={testo} onChange={(e) => setTesto(e.target.value)} placeholder="Scrivi a Epi" aria-label="Messaggio per Epi" disabled={inAttesa} autoFocus />
        <button type="submit" disabled={inAttesa}>Invia</button>
      </form>
    </>
  )
}

export default Conversazione
