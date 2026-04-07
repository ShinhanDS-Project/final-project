package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Key;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyRepository extends JpaRepository<Key, Long> {
}
