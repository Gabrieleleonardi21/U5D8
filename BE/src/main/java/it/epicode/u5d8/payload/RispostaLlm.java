package it.epicode.u5d8.payload;

/** Risultato "pulito" di una chiamata LLM andata a buon fine, indipendente dal formato del provider. */
public record RispostaLlm(String testo, int tokenInput, int tokenOutput) {
}
