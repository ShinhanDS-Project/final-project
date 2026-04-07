package com.merge.final_project.global;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByTargetNameAndTargetNo(String targetName, Long targetNo);

    List<Image> findByTargetNameAndPurposeAndTargetNoIn(String targetName, String purpose, List<Long> targetNos);
}
