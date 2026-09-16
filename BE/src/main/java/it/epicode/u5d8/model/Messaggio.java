package it.epicode.u5d8.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

/** Un messaggio della conversazione. Immutabile dopo il salvataggio: solo getter. */
@Entity
@Table(name = "messaggi")
@Getter
public class Messaggio {

	@Id
	@GeneratedValue
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Mittente mittente;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String testo;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chat_id")
	private Chat chat;

	// true solo per il messaggio "di servizio" salvato quando l'LLM non risponde: escluso dal contesto
	@Column(name = "is_error_message", nullable = false)
	private boolean isErrorMessage;

	protected Messaggio() {
	}

	public Messaggio(Chat chat, Mittente mittente, String testo, boolean isErrorMessage) {
		this.chat = chat;
		this.mittente = mittente;
		this.testo = testo;
		this.isErrorMessage = isErrorMessage;
	}

	// L'ordine dei messaggi lo decide il server: createdAt viene assegnato qui, mai dal client
	@PrePersist
	void primaDiSalvare() {
		this.createdAt = Instant.now();
	}
}
