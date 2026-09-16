package it.epicode.u5d8.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.epicode.u5d8.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

	boolean existsByNome(String nome);

	Optional<Chat> findByIdAndIsDeletedFalse(UUID id);

	List<Chat> findByIsDeletedFalseOrderByLastMessageSentDesc();

	// Incremento fatto dal DB in una sola UPDATE: niente lettura-modifica-scrittura, nessun lost update
	@Modifying
	@Query("update Chat c set c.tokens = c.tokens + :n where c.id = :id")
	void aggiungiToken(@Param("id") UUID id, @Param("n") long n);
}
