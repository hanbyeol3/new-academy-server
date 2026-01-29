package com.academy.api.sms.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.sms.domain.MessageLog;
import com.academy.api.sms.dto.RequestMessageLogCreate;
import com.academy.api.sms.dto.ResponseMessageLog;
import com.academy.api.sms.dto.ResponseMessageLogListItem;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 메시지 로그 Mapper.
 * 
 * Entity와 DTO 간의 변환을 담당합니다.
 * 
 * 주요 기능:
 * - Request DTO → Entity 변환
 * - Entity → Response DTO 변환  
 * - Page 객체 → ResponseList 변환
 * - 목록용 DTO와 상세용 DTO 분리
 */
@Component
public class MessageLogMapper {

    /**
     * RequestMessageLogCreate → MessageLog 변환.
     * 
     * @param request 생성 요청 DTO
     * @return MessageLog 엔티티
     */
    public MessageLog toEntity(RequestMessageLogCreate request) {
        return MessageLog.builder()
                .channel(request.getChannel())
                .sendType(request.getSendType())
                .toPhone(request.getToPhone())
                .toName(request.getToName())
                .toType(request.getToType())
                .fromPhone(request.getFromPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .templateCode(request.getTemplateCode())
                .status(request.getStatus())
                .provider(request.getProvider())
                .providerMessageId(request.getProviderMessageId())
                .cost(request.getCost())
                .characterCount(request.getCharacterCount())
                .byteCount(request.getByteCount())
                .errorCode(request.getErrorCode())
                .errorMessage(request.getErrorMessage())
                .purposeCode(request.getPurposeCode())
                .refType(request.getRefType())
                .refId(request.getRefId())
                .batchId(request.getBatchId())
                .batchSeq(request.getBatchSeq())
                .scheduledAt(request.getScheduledAt())
                .requestJson(request.getRequestJson())
                .responseJson(request.getResponseJson())
                .sentAt(request.getSentAt())
                .createdBy(request.getCreatedBy())
                .createdBySystem(request.getCreatedBySystem())
                .build();
    }

    /**
     * MessageLog → ResponseMessageLog 변환.
     * 
     * @param entity MessageLog 엔티티
     * @return ResponseMessageLog DTO
     */
    public ResponseMessageLog toResponse(MessageLog entity) {
        return ResponseMessageLog.from(entity);
    }

    /**
     * MessageLog → ResponseMessageLog 변환 (생성자 이름 포함).
     * 
     * @param entity MessageLog 엔티티
     * @param createdByName 생성자 이름
     * @return ResponseMessageLog DTO
     */
    public ResponseMessageLog toResponseWithCreatorName(MessageLog entity, String createdByName) {
        return ResponseMessageLog.fromWithCreatorName(entity, createdByName);
    }

    /**
     * MessageLog → ResponseMessageLogListItem 변환.
     * 
     * @param entity MessageLog 엔티티
     * @return ResponseMessageLogListItem DTO
     */
    public ResponseMessageLogListItem toListItem(MessageLog entity) {
        return ResponseMessageLogListItem.from(entity);
    }

    /**
     * MessageLog → ResponseMessageLogListItem 변환 (생성자 이름 포함).
     * 
     * @param entity MessageLog 엔티티
     * @param createdByName 생성자 이름
     * @return ResponseMessageLogListItem DTO
     */
    public ResponseMessageLogListItem toListItemWithCreatorName(MessageLog entity, String createdByName) {
        return ResponseMessageLogListItem.fromWithCreatorName(entity, createdByName);
    }

    /**
     * MessageLog List → ResponseMessageLogListItem List 변환.
     * 
     * @param entities MessageLog 엔티티 목록
     * @return ResponseMessageLogListItem DTO 목록
     */
    public List<ResponseMessageLogListItem> toListItems(List<MessageLog> entities) {
        return ResponseMessageLogListItem.fromList(entities);
    }

    /**
     * Page<MessageLog> → ResponseList<ResponseMessageLogListItem> 변환.
     * 
     * @param page MessageLog 페이지 객체
     * @return ResponseList DTO
     */
    public ResponseList<ResponseMessageLogListItem> toListItemResponseList(Page<MessageLog> page) {
        return ResponseList.from(page.map(ResponseMessageLogListItem::from));
    }

    /**
     * Page<MessageLog> → ResponseList<ResponseMessageLog> 변환 (상세 정보용).
     * 
     * @param page MessageLog 페이지 객체
     * @return ResponseList DTO
     */
    public ResponseList<ResponseMessageLog> toResponseList(Page<MessageLog> page) {
        return ResponseList.from(page.map(ResponseMessageLog::from));
    }

    /**
     * SOLAPI 발송을 위한 메시지 로그 생성 도우미.
     * 
     * @param channel 발송 채널
     * @param toPhone 수신번호
     * @param toName 수신자명  
     * @param fromPhone 발신번호
     * @param message 메시지 내용
     * @param purposeCode 목적 코드
     * @param requestJson 요청 JSON
     * @return MessageLog 엔티티
     */
    public MessageLog createForSolapi(MessageLog.Channel channel, String toPhone, String toName, 
                                     String fromPhone, String message, String purposeCode, 
                                     String requestJson) {
        return MessageLog.builder()
                .channel(channel)
                .sendType(MessageLog.SendType.IMMEDIATE)
                .toPhone(toPhone)
                .toName(toName)
                .toType(MessageLog.TargetType.USER) // 기본값, 필요시 수정
                .fromPhone(fromPhone)
                .message(message)
                .status(MessageLog.Status.PENDING)
                .provider("SOLAPI")
                .purposeCode(purposeCode)
                .requestJson(requestJson)
                .createdBySystem(true)
                .build();
    }

    /**
     * SOLAPI 발송을 위한 LMS 메시지 로그 생성 도우미.
     * 
     * @param toPhone 수신번호
     * @param toName 수신자명
     * @param fromPhone 발신번호
     * @param subject 제목
     * @param message 메시지 내용
     * @param purposeCode 목적 코드
     * @param requestJson 요청 JSON
     * @return MessageLog 엔티티
     */
    public MessageLog createLmsForSolapi(String toPhone, String toName, String fromPhone, 
                                        String subject, String message, String purposeCode, 
                                        String requestJson) {
        return MessageLog.builder()
                .channel(MessageLog.Channel.LMS)
                .sendType(MessageLog.SendType.IMMEDIATE)
                .toPhone(toPhone)
                .toName(toName)
                .toType(MessageLog.TargetType.USER)
                .fromPhone(fromPhone)
                .subject(subject)
                .message(message)
                .status(MessageLog.Status.PENDING)
                .provider("SOLAPI")
                .purposeCode(purposeCode)
                .requestJson(requestJson)
                .createdBySystem(true)
                .build();
    }

    /**
     * 배치 발송용 메시지 로그 생성 도우미.
     * 
     * @param channel 발송 채널
     * @param toPhone 수신번호
     * @param toName 수신자명
     * @param message 메시지 내용
     * @param purposeCode 목적 코드
     * @param batchId 배치 ID
     * @param batchSeq 배치 순서
     * @return MessageLog 엔티티
     */
    public MessageLog createForBatch(MessageLog.Channel channel, String toPhone, String toName,
                                    String message, String purposeCode, String batchId, Integer batchSeq) {
        return MessageLog.builder()
                .channel(channel)
                .sendType(MessageLog.SendType.BATCH)
                .toPhone(toPhone)
                .toName(toName)
                .toType(MessageLog.TargetType.USER)
                .message(message)
                .status(MessageLog.Status.PENDING)
                .provider("SOLAPI")
                .purposeCode(purposeCode)
                .batchId(batchId)
                .batchSeq(batchSeq)
                .createdBySystem(true)
                .build();
    }
}