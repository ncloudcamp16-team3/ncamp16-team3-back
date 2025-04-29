package tf.tailfriend.reserve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.reserve.dto.DateTimeRange;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.PaymentInfoResponseDto;
import tf.tailfriend.reserve.dto.PaymentListRequestDto;
import tf.tailfriend.reserve.service.PaymentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @RequestMapping("/get")
    public ResponseEntity<ListResponseDto<PaymentInfoResponseDto>> getPayments(
            @RequestParam("id") int userId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime parsedStartDate = null;
        LocalDateTime parsedEndDate = null;

        try {
            if (startDate != null && !startDate.isEmpty()) {
                parsedStartDate = LocalDateTime.parse(startDate, formatter);
            }
            if (endDate != null && !endDate.isEmpty()) {
                parsedEndDate = LocalDateTime.parse(endDate, formatter);
            }
        } catch (Exception e) {
            System.out.println("예외 발생");
            return ResponseEntity.badRequest().body(null); // 예외 처리
        }

        PaymentListRequestDto requestDto = PaymentListRequestDto.builder()
                .userId(userId)
                .datetimeRange(new DateTimeRange(parsedStartDate, parsedEndDate))
                .page(page)
                .size(size)
                .build();

        ListResponseDto<PaymentInfoResponseDto> list = paymentService.getList(requestDto);
        return ResponseEntity.ok(list);
    }
}
