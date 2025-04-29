package tf.tailfriend.reserve.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.PaymentInfoResponseDto;
import tf.tailfriend.reserve.dto.PaymentListRequestDto;
import tf.tailfriend.reserve.repository.PaymentDao;

@Service
@RequiredArgsConstructor
public class PaymentService {

     private final PaymentDao paymentDao;

     public ListResponseDto<PaymentInfoResponseDto> getList(PaymentListRequestDto requestDto) {
        return paymentDao.findPaymentsByRequestDto(requestDto);
     }


}
