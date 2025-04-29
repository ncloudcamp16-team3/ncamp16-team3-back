package tf.tailfriend.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.schedule.entity.dto.EventDTO;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.ScheduleGetDTO;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.SchedulePostDTO;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.SchedulePutDTO;
import tf.tailfriend.schedule.service.EventService;
import tf.tailfriend.schedule.service.ScheduleService;

import java.util.List;

@RestController
@RequestMapping("/api/calendar/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<?> getAllEvent() {

        List<EventDTO> schedules = eventService.getAllEvent();

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

}
