package com.merge.final_project.global;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    List<Image> findByTargetNameAndTargetNo(String targetName, Long targetNo);

    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.targetName = :targetName AND i.targetNo = :targetNo")
    void deleteByTargetNameAndTargetNo(@Param("targetName") String targetName, @Param("targetNo") Long targetNo);

}
