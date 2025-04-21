package tf.tailfriend.reserve.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.ReserveInfoResponseDto;
import tf.tailfriend.reserve.dto.ReserveListRequestDto;
import tf.tailfriend.reserve.service.ReserveService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reserve")
public class ReserveController {

    private final ReserveService service;

    public ReserveController(ReserveService service) {
        this.service = service;
    }

    @RequestMapping("/get")
    public ListResponseDto<ReserveInfoResponseDto> getReserveList(
            @RequestParam("localDateTime") String localDateTime,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("page") int page,
            @RequestParam("size") int size) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime parsedLocalDateTime = LocalDateTime.parse(localDateTime, formatter);

        ReserveListRequestDto requestDto = ReserveListRequestDto.builder()
                .localDateTime(parsedLocalDateTime)
                .latitude(latitude)
                .longitude(longitude)
                .page(page)
                .size(size)
                .build();

        return service.getReserveList(requestDto);
    }

}