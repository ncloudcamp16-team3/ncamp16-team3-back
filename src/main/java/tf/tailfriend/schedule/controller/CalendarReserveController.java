package tf.tailfriend.schedule.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.schedule.entity.dto.CalendarReserveDTO;
import tf.tailfriend.schedule.service.CalendarReserveService;

import java.util.List;

@RestController
@RequestMapping("/api/calendar/reserve")
@RequiredArgsConstructor
public class CalendarReserveController {

    private final CalendarReserveService calendarReserveService;

    @GetMapping
    public ResponseEntity<?> getAllReserve(Integer userId) {

        List<CalendarReserveDTO> schedules = calendarReserveService.getAllReserve(userId);

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }


}
