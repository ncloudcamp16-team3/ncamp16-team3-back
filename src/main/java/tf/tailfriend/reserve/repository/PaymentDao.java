package tf.tailfriend.reserve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tf.tailfriend.reserve.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentDao extends JpaRepository<Payment, Integer>, CustomPaymentDao {
    Optional<Payment> findByReserveId(Integer reserveId);

    List<Payment> findByReserveUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Integer userId, LocalDateTime start, LocalDateTime end);
}
