import { useEffect, useState } from 'react'
import './App.css'
import ListaChat from './components/ListaChat.jsx'
import Conversazione from './components/Conversazione.jsx'
import { creaChat, elencoChat, eliminaChat } from './services/chat.js'

/** Pagina unica: a sinistra l'elenco delle chat, a destra la conversazione aperta. */
function App() {
  const [chat, setChat] = useState([])
  const [chatAperta, setChatAperta] = useState(null)
  const [errore, setErrore] = useState('')

  useEffect(() => {
    aggiornaLista()
  }, [])

  // Rilegge la lista dal server: ordine (lastMessageSent) e token li decide il backend
  async function aggiornaLista() {
    try {
      setChat(await elencoChat())
    } catch (e) {
      setErrore(e.message)
    }
  }

  // Ritorna true se la chat e' stata creata, cosi' la sidebar sa se svuotare il campo nome
  async function nuovaChat(nome) {
    setErrore('')
    try {
      const creata = await creaChat(nome)
      await aggiornaLista()
      setChatAperta(creata)
      return true
    } catch (e) {
      setErrore(e.message)
      return false
    }
  }

  async function elimina(c) {
    setErrore('')
    try {
      await eliminaChat(c.id)
      if (chatAperta && chatAperta.id === c.id) setChatAperta(null)
      await aggiornaLista()
    } catch (e) {
      setErrore(e.message)
    }
  }

  // key = id: cambiando chat il componente viene ricreato con stato pulito (messaggi, pagina, input)
  let colonnaDestra = (
    <div className="benvenuto">
      <h1>Ciao, sono Epi.</h1>
      <p>Dai un nome a una chat qui a sinistra e scrivimi: rispondo in italiano, in breve, e ricordo il filo della conversazione.</p>
    </div>
  )
  if (chatAperta) {
    colonnaDestra = <Conversazione key={chatAperta.id} chat={chatAperta} onRisposta={aggiornaLista} />
  }

  return (
    <div className="app">
      <ListaChat chat={chat} chatAperta={chatAperta} onNuova={nuovaChat} onApri={setChatAperta} onElimina={elimina} errore={errore} />
      <main className="colonna">{colonnaDestra}</main>
    </div>
  )
}

export default App
