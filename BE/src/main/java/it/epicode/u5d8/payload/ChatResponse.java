package it.epicode.u5d8.payload;

import java.time.Instant;
import java.util.UUID;

import it.epicode.u5d8.model.Chat;

public record ChatResponse(UUID id, String nome, long tokens, Instant createdAt, Instant lastMessageSent) {

	public static ChatResponse da(Chat c) {
		return new ChatResponse(c.getId(), c.getNome(), c.getTokens(), c.getCreatedAt(), c.getLastMessageSent());
	}
}
