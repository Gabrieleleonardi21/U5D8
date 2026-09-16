package it.epicode.u5d8.payload;

/** Un messaggio nel formato OpenAI/OpenRouter: role = system | user | assistant. */
public record MessaggioLlm(String role, String content) {
}
