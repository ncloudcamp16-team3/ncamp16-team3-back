package tf.tailfriend.reserve.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.PaymentHistoryDto;
import tf.tailfriend.reserve.dto.PaymentInfoResponseDto;
import tf.tailfriend.reserve.dto.PaymentListRequestDto;
import tf.tailfriend.reserve.entity.Payment;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.PaymentDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

     private final PaymentDao paymentDao;
     private final StorageService storageService;

     public ListResponseDto<PaymentInfoResponseDto> getList(PaymentListRequestDto requestDto) {
        return paymentDao.findPaymentsByRequestDto(requestDto);
     }


    public List<PaymentHistoryDto> getPaymentsByPeriod(Integer userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Payment> payments = paymentDao.findByReserveUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDateTime,  endDateTime);

        return payments.stream()
                .map(this::toDto)
                .toList();
    }

    private PaymentHistoryDto toDto(Payment payment) {
        Reserve reserve = payment.getReserve();
        Facility facility = reserve.getFacility();

        return PaymentHistoryDto.builder()
                .id(payment.getId())
                .name(facility.getName())
                .imageUrl(
                        facility.getPhotos().isEmpty()
                                ? "https://kr.object.ncloudstorage.com/tailfriends-buck/uploads/board/join-logo.png"
                                : storageService.generatePresignedUrl(facility.getPhotos().get(0).getFile().getPath())
                )
                .createdAt(payment.getCreatedAt())
                .reserveId(payment.getReserve().getId())
                .entryTime(payment.getReserve().getEntryTime())
                .exitTime(payment.getReserve().getExitTime())
                .price(payment.getPrice())
                .reviewId(
                        reserve.getReview() != null ? reserve.getReview().getId() : null
                )
                .build();
    }
}
