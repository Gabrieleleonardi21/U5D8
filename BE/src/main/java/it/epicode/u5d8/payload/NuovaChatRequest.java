package it.epicode.u5d8.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NuovaChatRequest(
		@NotBlank(message = "Il nome della chat e' obbligatorio")
		@Size(max = 100, message = "Il nome puo' avere al massimo 100 caratteri")
		String nome) {
}
