package com.medtrack.repository;

import com.medtrack.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i FROM Interaction i WHERE i.medicationA.id = :medId OR i.medicationB.id = :medId")
    List<Interaction> findByMedicationId(@Param("medId") Long medId);
}
