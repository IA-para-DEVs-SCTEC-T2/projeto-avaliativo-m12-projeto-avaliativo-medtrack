package com.medtrack.repository;

import com.medtrack.model.UserMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMedicationRepository extends JpaRepository<UserMedication, Long> {

    List<UserMedication> findByUserId(UUID userId);
}
