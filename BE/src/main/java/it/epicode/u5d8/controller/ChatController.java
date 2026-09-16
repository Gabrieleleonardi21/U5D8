package it.epicode.u5d8.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d8.payload.ChatResponse;
import it.epicode.u5d8.payload.NuovaChatRequest;
import it.epicode.u5d8.service.ChatService;
import jakarta.validation.Valid;

/** Gestione delle conversazioni: creazione, elenco e cancellazione logica. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ChatResponse nuova(@RequestBody @Valid NuovaChatRequest body) {
		return chatService.crea(body.nome());
	}

	@GetMapping
	public List<ChatResponse> attive() {
		return chatService.attive();
	}

	@PatchMapping("/{id}/elimina")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void elimina(@PathVariable UUID id) {
		chatService.elimina(id);
	}
}
