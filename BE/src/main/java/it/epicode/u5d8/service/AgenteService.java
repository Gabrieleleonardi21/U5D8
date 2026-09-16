package it.epicode.u5d8.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.epicode.u5d8.config.IstruzioniAgente;
import it.epicode.u5d8.model.Mittente;
import it.epicode.u5d8.payload.MessaggioLlm;
import it.epicode.u5d8.payload.MessaggioResponse;
import it.epicode.u5d8.payload.RispostaLlm;

/**
 * Orchestra il flusso "l'utente scrive, l'agente risponde".
 * VOLUTAMENTE senza @Transactional: ogni passo sul DB e' una transazione breve di un altro bean
 * (cosi' passa dal proxy di Spring), mentre la chiamata all'LLM resta fuori da qualsiasi transazione.
 */
@Service
public class AgenteService {

	static final String TESTO_ERRORE = "Servizio attualmente non disponibile";

	private final MessaggioService messaggioService;
	private final ClientLlm clientLlm;
	private final IstruzioniAgente istruzioni;
	private final int maxMessaggiContesto;

	public AgenteService(MessaggioService messaggioService, ClientLlm clientLlm, IstruzioniAgente istruzioni,
			@Value("${app.llm.max-messaggi-contesto}") int maxMessaggiContesto) {
		this.messaggioService = messaggioService;
		this.clientLlm = clientLlm;
		this.istruzioni = istruzioni;
		this.maxMessaggiContesto = maxMessaggiContesto;
	}

	/**
	 * 1) salva il messaggio dell'utente (gia' committato prima di contattare l'LLM: resta anche se l'LLM fallisce)
	 * 2) costruisce il contesto 3) chiama l'LLM con retry 4) salva la risposta (o il messaggio di errore).
	 */
	public MessaggioResponse rispondi(UUID chatId, String testo) {
		messaggioService.salva(chatId, Mittente.USER, testo, false);
		Optional<RispostaLlm> risposta = clientLlm.completa(costruisciContesto(chatId));
		if (risposta.isEmpty()) {
			// Nessun errore HTTP verso il client: la risposta "di servizio" e' un messaggio dell'agente marcato come errore
			return messaggioService.salva(chatId, Mittente.AGENT, TESTO_ERRORE, true);
		}
		long token = risposta.get().tokenInput() + risposta.get().tokenOutput();
		return messaggioService.salvaRispostaAgente(chatId, risposta.get().testo(), token);
	}

	// Istruzioni di sistema + ultimi N messaggi validi in ordine cronologico (compreso quello appena salvato)
	private List<MessaggioLlm> costruisciContesto(UUID chatId) {
		List<MessaggioLlm> messaggi = new ArrayList<>();
		messaggi.add(new MessaggioLlm("system", istruzioni.testo()));
		for (MessaggioResponse m : messaggioService.ultimiPerContesto(chatId, maxMessaggiContesto)) {
			messaggi.add(new MessaggioLlm(ruolo(m.mittente()), m.testo()));
		}
		return messaggi;
	}

	private String ruolo(Mittente mittente) {
		if (mittente == Mittente.USER) {
			return "user";
		}
		return "assistant";
	}
}
