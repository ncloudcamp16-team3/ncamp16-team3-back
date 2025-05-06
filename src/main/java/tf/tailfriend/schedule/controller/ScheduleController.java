package tf.tailfriend.schedule.controller;

import jakarta.validation.Valid;
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


    @PostMapping
    public ResponseEntity<?> addSchedule(@Valid @RequestBody SchedulePostDTO dto) {
        try {
            scheduleService.postSchedule(dto);
            return new ResponseEntity<>("일정이 성공적으로 등록되었습니다.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 클라이언트 잘못
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>("일정 등록에 실패했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    public ResponseEntity<?> modifySchedule(@Valid @RequestBody SchedulePutDTO dto) {
        try {
            scheduleService.putSchedule(dto);
            return new ResponseEntity<>("일정이 성공적으로 수정되었습니다.", HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 클라이언트 잘못
        } catch (Exception e) {
            System.out.println(e); //
            return new ResponseEntity<>("일정 수정에 실패했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeSchedule(@PathVariable Integer id) {
        try {
            scheduleService.deleteSchedule(id);
            return new ResponseEntity<>("일정이 성공적으로 삭제되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e); //
            return new ResponseEntity<>("일정 삭제에 실패했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
