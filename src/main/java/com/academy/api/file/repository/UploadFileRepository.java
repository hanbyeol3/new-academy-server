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
public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

    /**
     * 파일 ID로 파일 조회 (String용 - 임시 호환).
     * 
     * @param id 파일 ID
     * @return 파일 정보
     */
    default Optional<UploadFile> findByIdAndDeletedFalse(String id) {
        try {
            Long longId = Long.parseLong(id);
            return findById(longId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 파일명으로 시작하는 파일 조회 (임시 파일 ID → 정식 파일 매핑용).
     * 
     * @param prefix 파일명 접두사 (예: tempFileId + ".")
     * @return 파일 정보
     */
    Optional<UploadFile> findByFileNameStartingWith(String prefix);
}