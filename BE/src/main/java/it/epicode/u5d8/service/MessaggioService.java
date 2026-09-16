package it.epicode.u5d8.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.epicode.u5d8.model.Chat;
import it.epicode.u5d8.model.Messaggio;
import it.epicode.u5d8.model.Mittente;
import it.epicode.u5d8.payload.MessaggioResponse;
import it.epicode.u5d8.repository.MessaggioRepository;

/** Persistenza dei messaggi. Ogni metodo e' una transazione breve: qui non si parla mai con l'LLM. */
@Service
public class MessaggioService {

	private final MessaggioRepository messaggioRepository;
	private final ChatService chatService;

	public MessaggioService(MessaggioRepository messaggioRepository, ChatService chatService) {
		this.messaggioRepository = messaggioRepository;
		this.chatService = chatService;
	}

	/** Salva un messaggio (utente o agente) e porta la chat in cima alla lista aggiornando lastMessageSent. */
	@Transactional
	public MessaggioResponse salva(UUID chatId, Mittente mittente, String testo, boolean isErrorMessage) {
		Chat chat = chatService.trovaAttiva(chatId);
		Messaggio salvato = messaggioRepository.save(new Messaggio(chat, mittente, testo, isErrorMessage));
		chat.setLastMessageSent(salvato.getCreatedAt());
		return MessaggioResponse.da(salvato);
	}

	/**
	 * Risposta dell'agente e conteggio token nella STESSA transazione: o si salvano entrambi o nessuno.
	 * L'UPDATE atomico dei token va eseguito PRIMA di caricare la Chat, cosi' l'entita' letta subito dopo
	 * e' gia' aggiornata e Hibernate non rischia di riscrivere un valore vecchio al commit.
	 */
	@Transactional
	public MessaggioResponse salvaRispostaAgente(UUID chatId, String testo, long token) {
		chatService.aggiungiToken(chatId, token);
		return salva(chatId, Mittente.AGENT, testo, false);
	}

	/** Pagina 0 = messaggi piu' recenti (createdAt DESC): il client la inverte per mostrarli in ordine. */
	@Transactional(readOnly = true)
	public Page<MessaggioResponse> cronologia(UUID chatId, int page, int size) {
		chatService.trovaAttiva(chatId);
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return messaggioRepository.findByChat_Id(chatId, pageable).map(MessaggioResponse::da);
	}

	/**
	 * Strategia di contesto per l'LLM: solo gli ultimi n messaggi validi (esclusi quelli di errore),
	 * restituiti in ordine cronologico come li vuole l'API.
	 */
	@Transactional(readOnly = true)
	public List<MessaggioResponse> ultimiPerContesto(UUID chatId, int n) {
		List<Messaggio> recenti = messaggioRepository
				.findByChat_IdAndIsErrorMessageFalseOrderByCreatedAtDesc(chatId, Limit.of(n));
		// Dal DB arrivano dal piu' recente: reversed() li rimette in ordine cronologico senza copiare la lista
		return recenti.reversed().stream().map(MessaggioResponse::da).toList();
	}
}
