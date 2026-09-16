package it.epicode.u5d8.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import it.epicode.u5d8.model.Messaggio;

public interface MessaggioRepository extends JpaRepository<Messaggio, UUID> {

	// L'ordinamento (createdAt DESC) arriva dal Pageable costruito nel service
	Page<Messaggio> findByChat_Id(UUID chatId, Pageable pageable);

	// Gli ultimi N messaggi validi (esclusi quelli di errore), dal piu' recente: il service li inverte
	List<Messaggio> findByChat_IdAndIsErrorMessageFalseOrderByCreatedAtDesc(UUID chatId, Limit limit);
}
