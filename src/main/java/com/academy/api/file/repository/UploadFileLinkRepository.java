package com.academy.api.file.repository;

import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * 파일 연결 레포지토리.
 */
public interface UploadFileLinkRepository extends JpaRepository<UploadFileLink, Long> {

    /**
     * 특정 소유자의 파일 연결 목록 조회.
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 엔티티 ID
     * @return 파일 연결 목록
     */
    List<UploadFileLink> findByOwnerTableAndOwnerId(String ownerTable, Long ownerId);

    /**
     * 특정 소유자의 특정 역할 파일 연결 목록 조회.
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 엔티티 ID
     * @param role 파일 역할
     * @return 파일 연결 목록
     */
    List<UploadFileLink> findByOwnerTableAndOwnerIdAndRole(String ownerTable, Long ownerId, FileRole role);

    /**
     * 특정 소유자의 파일 연결 삭제 (특정 역할).
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 엔티티 ID
     * @param role 파일 역할
     */
    @Modifying
    @Query("DELETE FROM UploadFileLink l WHERE l.ownerTable = :ownerTable AND l.ownerId = :ownerId AND l.role = :role")
    void deleteByOwnerTableAndOwnerIdAndRole(@Param("ownerTable") String ownerTable, 
                                           @Param("ownerId") Long ownerId, 
                                           @Param("role") FileRole role);

    /**
     * 특정 소유자의 모든 파일 연결 삭제.
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 엔티티 ID
     */
    @Modifying
    @Query("DELETE FROM UploadFileLink l WHERE l.ownerTable = :ownerTable AND l.ownerId = :ownerId")
    void deleteByOwnerTableAndOwnerId(@Param("ownerTable") String ownerTable, 
                                    @Param("ownerId") Long ownerId);

    /**
     * 여러 소유자의 파일 개수 일괄 조회 (IN절 활용).
     * 공지사항 목록에서 첨부파일/본문이미지 개수를 효율적으로 조회하기 위해 사용.
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerIds 소유 엔티티 ID 목록
     * @return Map<소유자ID, Map<역할, 개수>>
     */
    @Query("""
        SELECT l.ownerId as ownerId, 
               l.role as role, 
               COUNT(l.id) as fileCount
        FROM UploadFileLink l 
        WHERE l.ownerTable = :ownerTable 
          AND l.ownerId IN :ownerIds
        GROUP BY l.ownerId, l.role
        """)
    List<Object[]> countFilesByOwnerIdsGroupByRole(@Param("ownerTable") String ownerTable, 
                                                  @Param("ownerIds") List<Long> ownerIds);

    /**
     * 공지사항 파일 상세 정보 조회 (JOIN 활용).
     * 
     * @param ownerTable 소유 테이블명 
     * @param ownerId 소유 엔티티 ID
     * @param role 파일 역할
     * @return 파일 정보 목록 (fileId, fileName, originalName, ext, size, url)
     */
    @Query("""
        SELECT l.fileId as fileId,
               f.fileName as fileName, 
               f.originalName as originalName,
               f.ext as ext, 
               f.size as size, 
               f.serverPath as url
        FROM UploadFileLink l
        LEFT JOIN UploadFile f ON f.id = l.fileId
        WHERE l.ownerTable = :ownerTable 
          AND l.ownerId = :ownerId 
          AND l.role = :role
          AND f.id IS NOT NULL
        ORDER BY l.createdAt
        """)
    List<Object[]> findFileInfosByOwnerAndRole(@Param("ownerTable") String ownerTable,
                                              @Param("ownerId") Long ownerId, 
                                              @Param("role") FileRole role);

    /**
     * 고아 파일 연결 조회 (파일이 존재하지 않는 연결).
     * 
     * @return 고아 파일 연결 목록
     */
    @Query("""
        SELECT l FROM UploadFileLink l
        LEFT JOIN UploadFile f ON f.id = l.fileId
        WHERE f.id IS NULL
        """)
    List<UploadFileLink> findOrphanLinks();

    /**
     * 특정 파일 ID 목록의 연결 삭제 (선택적 파일 삭제용).
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 엔티티 ID
     * @param role 파일 역할
     * @param fileIds 삭제할 파일 ID 목록
     */
    @Modifying
    @Query("DELETE FROM UploadFileLink l WHERE l.ownerTable = :ownerTable AND l.ownerId = :ownerId AND l.role = :role AND l.fileId IN :fileIds")
    void deleteByOwnerTableAndOwnerIdAndRoleAndFileIdIn(
        @Param("ownerTable") String ownerTable,
        @Param("ownerId") Long ownerId, 
        @Param("role") FileRole role,
        @Param("fileIds") List<Long> fileIds
    );
}