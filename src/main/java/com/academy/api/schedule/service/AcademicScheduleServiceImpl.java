package com.academy.api.schedule.service;

import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.schedule.domain.AcademicSchedule;
import com.academy.api.schedule.dto.RequestAcademicScheduleCreate;
import com.academy.api.schedule.dto.RequestAcademicScheduleUpdate;
import com.academy.api.schedule.dto.ResponseAcademicScheduleListItem;
import com.academy.api.schedule.mapper.AcademicScheduleMapper;
import com.academy.api.schedule.repository.AcademicScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 학사일정 서비스 구현.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicScheduleServiceImpl implements AcademicScheduleService {

    private final AcademicScheduleRepository scheduleRepository;
    private final AcademicScheduleMapper scheduleMapper;

    @Override
    public List<ResponseAcademicScheduleListItem> getSchedulesByMonth(int year, int month) {
        log.info("월 단위 학사일정 조회 요청. year={}, month={}", year, month);
        
        List<AcademicSchedule> schedules = scheduleRepository.findByMonth(year, month, true);
        
        log.debug("학사일정 조회 결과. count={}", schedules.size());
        
        return scheduleMapper.toResponseList(schedules);
    }

    @Override
    @Transactional
    public ResponseData<ResponseAcademicScheduleListItem> createSchedule(RequestAcademicScheduleCreate request) {
        log.info("학사일정 등록 요청. title={}, startDate={}, endDate={}", 
                request.getTitle(), request.getStartDate(), request.getEndDate());
        
        AcademicSchedule schedule = scheduleMapper.toEntity(request);
        AcademicSchedule savedSchedule = scheduleRepository.save(schedule);
        
        log.info("학사일정 등록 완료. id={}, title={}", savedSchedule.getId(), savedSchedule.getTitle());
        
        ResponseAcademicScheduleListItem response = scheduleMapper.toResponse(savedSchedule);
        return ResponseData.ok("0000", "학사일정이 등록되었습니다.", response);
    }

    @Override
    @Transactional
    public ResponseData<ResponseAcademicScheduleListItem> updateSchedule(Long id, RequestAcademicScheduleUpdate request) {
        log.info("학사일정 수정 요청. id={}, title={}", id, request.getTitle());
        
        AcademicSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        
        schedule.update(
                request.getTitle(),
                null, // DTO에 description 필드가 없으므로 null
                request.getStartDate(),
                request.getEndDate(),
                null  // DTO에 updatedBy 필드가 없으므로 null
        );
        
        log.info("학사일정 수정 완료. id={}, title={}", id, schedule.getTitle());
        
        ResponseAcademicScheduleListItem response = scheduleMapper.toResponse(schedule);
        return ResponseData.ok("0000", "학사일정이 수정되었습니다.", response);
    }

    @Override
    @Transactional
    public Response deleteSchedule(Long id) {
        log.info("학사일정 삭제 요청. id={}", id);
        
        if (!scheduleRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        
        scheduleRepository.deleteById(id);
        
        log.info("학사일정 삭제 완료. id={}", id);
        
        return Response.ok("0000", "학사일정이 삭제되었습니다.");
    }
}