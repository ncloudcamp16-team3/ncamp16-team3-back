package tf.tailfriend.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.*;
import tf.tailfriend.schedule.service.ScheduleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<?> getAllSchedules(Integer id) {

        List<ScheduleGetDTO> schedules = scheduleService.getAllSchedules(id);

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/date")
    public ResponseEntity<?> getOneSchedules(@RequestParam("selectDateTime") @DateTimeFormat(pattern = "yyyy-MM-dd") String selectDate,
                                             @RequestParam("id") Integer id) {

        LocalDate localDate = LocalDate.parse(selectDate);

        List<ScheduleGetDTO> schedules = scheduleService.getOneSchedules(localDate,id);

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

}
