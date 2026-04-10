package com.merge.final_project.recipient.beneficiary.repository;

import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    boolean existsByEmail(String email);

    Optional<Beneficiary> findByEmail(String email);


    Optional<Beneficiary> findByEntryCode(String entryCode);

}
