import { useState } from 'react'
import Avviso from './Avviso.jsx'

/** Sidebar: form per una nuova chat ed elenco delle chat esistenti (la piu' recente in cima). */
function ListaChat({ chat, chatAperta, onNuova, onApri, onElimina, errore }) {
  const [nome, setNome] = useState('')

  async function crea(e) {
    e.preventDefault()
    if (!nome.trim()) return
    // Svuoto il campo solo se la creazione va a buon fine (es. nome duplicato -> resta scritto)
    const creata = await onNuova(nome.trim())
    if (creata) setNome('')
  }

  function classeChat(id) {
    if (chatAperta && chatAperta.id === id) return 'contatto attivo'
    return 'contatto'
  }

  return (
    <aside className="sidebar">
      <header>
        <strong>Le mie chat</strong>
      </header>
      <form className="nuova-chat" onSubmit={crea}>
        <input value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Nome della nuova chat" maxLength={100} aria-label="Nome della nuova chat" />
        <button type="submit">Crea</button>
      </form>
      {chat.length === 0 && <p className="vuoto">Nessuna chat ancora.</p>}
      <ul>
        {chat.map((c) => (
          <li key={c.id}>
            <button type="button" className={classeChat(c.id)} onClick={() => onApri(c)}>
              <span>{c.nome}</span>
              <small>{c.tokens} token</small>
            </button>
            <button type="button" className="elimina" title="Elimina chat" aria-label={`Elimina la chat ${c.nome}`} onClick={() => onElimina(c)}>×</button>
          </li>
        ))}
      </ul>
      <Avviso testo={errore} />
    </aside>
  )
}

export default ListaChat
