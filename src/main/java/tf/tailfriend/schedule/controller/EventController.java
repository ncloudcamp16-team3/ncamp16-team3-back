package tf.tailfriend.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.schedule.entity.dto.EventDTO;
import tf.tailfriend.schedule.service.EventService;

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
