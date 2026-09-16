package it.epicode.u5d8.payload;

import java.time.Instant;
import java.util.UUID;

import it.epicode.u5d8.model.Messaggio;
import it.epicode.u5d8.model.Mittente;

public record MessaggioResponse(UUID id, UUID chatId, Mittente mittente, String testo, Instant createdAt,
		boolean isErrorMessage) {

	// Sulla relazione LAZY legge solo getId(): non scatena query aggiuntive
	public static MessaggioResponse da(Messaggio m) {
		return new MessaggioResponse(m.getId(), m.getChat().getId(), m.getMittente(), m.getTesto(),
				m.getCreatedAt(), m.isErrorMessage());
	}
}
