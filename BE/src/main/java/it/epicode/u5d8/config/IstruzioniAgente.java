package it.epicode.u5d8.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Carattere e regole dell'agente, letti UNA volta all'avvio dal file agente/istruzioni.txt.
 * Il prompt vive nel file, mai nel codice Java. Se il file manca o e' vuoto l'applicazione non parte.
 */
@Component
public class IstruzioniAgente {

	private final String testo;

	public IstruzioniAgente(@Value("classpath:agente/istruzioni.txt") Resource risorsa) throws IOException {
		if (!risorsa.exists()) {
			throw new IllegalStateException("File delle istruzioni dell'agente mancante: agente/istruzioni.txt");
		}
		String contenuto = risorsa.getContentAsString(StandardCharsets.UTF_8).trim();
		if (contenuto.isBlank()) {
			throw new IllegalStateException("File delle istruzioni dell'agente vuoto: agente/istruzioni.txt");
		}
		this.testo = contenuto;
	}

	public String testo() {
		return testo;
	}
}
