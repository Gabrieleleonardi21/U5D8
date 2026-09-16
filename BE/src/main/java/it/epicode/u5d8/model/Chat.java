package it.epicode.u5d8.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/** Una conversazione con l'agente. La cancellazione e' logica (isDeleted), i messaggi restano a DB. */
@Entity
@Table(name = "chat")
@Getter
@Setter
public class Chat {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	@Column(nullable = false, unique = true, length = 100)
	private String nome;

	// Somma dei token (input + output) consumati da tutte le chiamate LLM di questa chat.
	// Nessun setter: si aggiorna solo con l'UPDATE atomico di ChatRepository.aggiungiToken
	@Column(nullable = false)
	@Setter(AccessLevel.NONE)
	private long tokens;

	@Column(name = "created_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant createdAt;

	@Column(name = "last_message_sent", nullable = false)
	private Instant lastMessageSent;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	protected Chat() {
	}

	public Chat(String nome) {
		this.nome = nome;
	}

	// Alla creazione l'ultimo messaggio coincide con la creazione stessa
	@PrePersist
	void primaDiSalvare() {
		this.createdAt = Instant.now();
		this.lastMessageSent = this.createdAt;
	}
}
