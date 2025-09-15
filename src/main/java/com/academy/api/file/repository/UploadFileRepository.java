package com.academy.api.file.repository;

import com.academy.api.file.domain.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 업로드 파일 저장소.
 */
@Repository
public interface UploadFileRepository extends JpaRepository<UploadFile, String> {

    /**
     * 그룹키로 파일 목록 조회 (삭제되지 않은 파일만).
     * 
     * @param groupKey 파일 그룹키
     * @return 파일 목록
     */
    @Query("SELECT f FROM UploadFile f WHERE f.groupKey = :groupKey AND f.deleted = false ORDER BY f.regDate ASC")
    List<UploadFile> findByGroupKeyAndDeletedFalse(@Param("groupKey") String groupKey);

    /**
     * 파일 ID로 파일 조회 (삭제되지 않은 파일만).
     * 
     * @param id 파일 ID
     * @return 파일 정보
     */
    @Query("SELECT f FROM UploadFile f WHERE f.id = :id AND f.deleted = false")
    Optional<UploadFile> findByIdAndDeletedFalse(@Param("id") String id);

    /**
     * 그룹키와 파일명으로 파일 조회 (삭제되지 않은 파일만).
     * 
     * @param groupKey 파일 그룹키
     * @param fileName 파일명
     * @return 파일 정보
     */
    @Query("SELECT f FROM UploadFile f WHERE f.groupKey = :groupKey AND f.fileName = :fileName AND f.deleted = false")
    Optional<UploadFile> findByGroupKeyAndFileNameAndDeletedFalse(@Param("groupKey") String groupKey, 
                                                                 @Param("fileName") String fileName);

    /**
     * 그룹키로 파일 개수 조회 (삭제되지 않은 파일만).
     * 
     * @param groupKey 파일 그룹키
     * @return 파일 개수
     */
    @Query("SELECT COUNT(f) FROM UploadFile f WHERE f.groupKey = :groupKey AND f.deleted = false")
    long countByGroupKeyAndDeletedFalse(@Param("groupKey") String groupKey);

    /**
     * 그룹키로 파일들을 논리적 삭제 처리.
     * 
     * @param groupKey 파일 그룹키
     */
    @Query("UPDATE UploadFile f SET f.deleted = true WHERE f.groupKey = :groupKey")
    void deleteByGroupKey(@Param("groupKey") String groupKey);
}