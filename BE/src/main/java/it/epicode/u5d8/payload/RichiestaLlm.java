package it.epicode.u5d8.payload;

import java.util.List;

/** Corpo della POST /chat/completions: il modello e l'intero contesto della conversazione. */
public record RichiestaLlm(String model, List<MessaggioLlm> messages) {
}
