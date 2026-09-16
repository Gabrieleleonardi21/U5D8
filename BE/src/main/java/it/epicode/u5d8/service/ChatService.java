package it.epicode.u5d8.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d8.model.Chat;
import it.epicode.u5d8.payload.ChatResponse;
import it.epicode.u5d8.repository.ChatRepository;

@Service
public class ChatService {

	private final ChatRepository chatRepository;

	public ChatService(ChatRepository chatRepository) {
		this.chatRepository = chatRepository;
	}

	@Transactional
	public ChatResponse crea(String nome) {
		String pulito = nome.trim();
		// Il vincolo UNIQUE vale anche per le chat cancellate logicamente: il nome non e' riutilizzabile
		if (chatRepository.existsByNome(pulito)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Esiste gia' una chat con questo nome");
		}
		return ChatResponse.da(chatRepository.save(new Chat(pulito)));
	}

	/** Solo le chat non cancellate, la piu' recente (ultimo messaggio) per prima. */
	@Transactional(readOnly = true)
	public List<ChatResponse> attive() {
		return chatRepository.findByIsDeletedFalseOrderByLastMessageSentDesc().stream()
				.map(ChatResponse::da)
				.toList();
	}

	/** Cancellazione logica: l'UPDATE lo fa Hibernate al commit (dirty checking). */
	@Transactional
	public void elimina(UUID id) {
		trovaAttiva(id).setDeleted(true);
	}

	/** Usato anche dagli altri service dentro le loro transazioni (REQUIRED si unisce a quella esistente). */
	@Transactional(readOnly = true)
	public Chat trovaAttiva(UUID id) {
		return chatRepository.findByIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat non trovata"));
	}

	/** Somma i token consumati da una chiamata LLM al totale della chat (UPDATE atomico). */
	@Transactional
	public void aggiungiToken(UUID chatId, long token) {
		chatRepository.aggiungiToken(chatId, token);
	}
}
