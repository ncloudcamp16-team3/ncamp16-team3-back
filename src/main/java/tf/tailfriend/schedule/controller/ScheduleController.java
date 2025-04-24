package tf.tailfriend.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tf.tailfriend.schedule.entity.dto.ScheduleDTO.*;
import tf.tailfriend.schedule.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<?> getAllSchedules(Integer userId) {

        List<ScheduleGetDTO> schedules = scheduleService.getAllSchedules(userId);

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/date")
    public ResponseEntity<?> getOneSchedules(@RequestParam("selectDateTime") @DateTimeFormat(pattern = "yyyy-MM-dd") String selectDate,
                                             Integer userId) {

        LocalDate localDate = LocalDate.parse(selectDate);

        List<ScheduleGetDTO> schedules = scheduleService.getOneSchedules(localDate,userId);

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> addSchedule(@RequestBody SchedulePostDTO dto) {
        try {
            // 서비스 레이어에서 일정 추가 처리
            scheduleService.postSchedule(dto);

            // 일정 추가 성공 시
            return new ResponseEntity<>("일정이 성공적으로 등록되었습니다.", HttpStatus.CREATED);
        } catch (Exception e) {
            // 예외 처리: 서버 오류 등
            System.out.println(e); //
            return new ResponseEntity<>("일정 등록에 실패했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
