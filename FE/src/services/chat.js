import { api, patch, post } from './api.js'

/** Tutte le chiamate al backend in un posto solo: i componenti non conoscono gli URL. */
export function elencoChat() {
  return api('/api/chat')
}

export function creaChat(nome) {
  return post('/api/chat', { nome })
}

export function eliminaChat(id) {
  return patch(`/api/chat/${id}/elimina`)
}

// Pagina 0 = messaggi piu' recenti (il backend ordina per createdAt DESC)
export function messaggiDi(chatId, page = 0, size = 50) {
  return api(`/api/chat/${chatId}/messaggi?page=${page}&size=${size}`)
}

export function inviaMessaggio(chatId, testo) {
  return post('/api/messaggi', { chatId, testo })
}
