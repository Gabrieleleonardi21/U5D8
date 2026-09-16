package it.epicode.u5d8.controller;

import java.util.UUID;

import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d8.payload.MessaggioResponse;
import it.epicode.u5d8.payload.NuovoMessaggioRequest;
import it.epicode.u5d8.service.AgenteService;
import it.epicode.u5d8.service.MessaggioService;
import jakarta.validation.Valid;

/** Messaggi: invio (con risposta dell'agente) e cronologia paginata. */
@RestController
@RequestMapping("/api")
public class MessaggioController {

	private final AgenteService agenteService;
	private final MessaggioService messaggioService;

	public MessaggioController(AgenteService agenteService, MessaggioService messaggioService) {
		this.agenteService = agenteService;
		this.messaggioService = messaggioService;
	}

	/** Risponde con il messaggio dell'agente (anche quello di errore, con isErrorMessage = true). */
	@PostMapping("/messaggi")
	@ResponseStatus(HttpStatus.CREATED)
	public MessaggioResponse nuovo(@RequestBody @Valid NuovoMessaggioRequest body) {
		return agenteService.rispondi(body.chatId(), body.testo().trim());
	}

	// PagedModel: JSON stabile { content: [...], page: { size, number, totalElements, totalPages } }
	@GetMapping("/chat/{chatId}/messaggi")
	public PagedModel<MessaggioResponse> cronologia(@PathVariable UUID chatId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
		return new PagedModel<>(messaggioService.cronologia(chatId, page, size));
	}
}
