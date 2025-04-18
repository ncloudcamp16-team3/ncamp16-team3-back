package tf.tailfriend.reserve.repository;

import org.springframework.stereotype.Repository;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.PaymentInfoResponseDto;
import tf.tailfriend.reserve.dto.PaymentListRequestDto;

@Repository
public interface CustomPaymentDao {
    ListResponseDto<PaymentInfoResponseDto> findPaymentsByRequestDto(PaymentListRequestDto requestDto); // 유저 ID와 시설 ID를 기준으로 결제 내역 조회
}
