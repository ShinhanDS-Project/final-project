package com.merge.final_project.org.illegalfoundation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IllegalFoundationRepository extends JpaRepository<IllegalFoundation, Long> {
    Optional<IllegalFoundation> findByNameAndRepresentative(String name, String representative);
    List<IllegalFoundation> findByNameContaining(String name);
}
