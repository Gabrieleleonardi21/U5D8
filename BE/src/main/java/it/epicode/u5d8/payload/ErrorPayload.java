package it.epicode.u5d8.payload;

import java.time.Instant;

/** Corpo uniforme di tutte le risposte di errore del backend. */
public record ErrorPayload(int status, String messaggio, Instant timestamp) {

	public ErrorPayload(int status, String messaggio) {
		this(status, messaggio, Instant.now());
	}
}
