package com.academy.api.explanation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 엑셀 파일 export 결과를 담는 DTO.
 * 
 * ResponseEntity로 반환하기 위해 파일명과 바이트 배열을 포함합니다.
 */
@Getter
@AllArgsConstructor
public class ExcelExportResult {
    
    /**
     * 엑셀 파일명
     */
    private String fileName;
    
    /**
     * 엑셀 파일 바이트 배열
     */
    private byte[] bytes;
}