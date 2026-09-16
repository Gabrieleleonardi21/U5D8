package it.epicode.u5d8.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Solo i campi della risposta OpenRouter che usiamo: gli altri (id, model, reasoning...)
 * vengono ignorati, perche' Jackson 3 non fallisce sulle proprieta' sconosciute.
 */
public record RispostaOpenRouter(List<Scelta> choices, Utilizzo usage) {

	public record Scelta(MessaggioLlm message) {
	}

	public record Utilizzo(
			@JsonProperty("prompt_tokens") int promptTokens,
			@JsonProperty("completion_tokens") int completionTokens) {
	}
}
