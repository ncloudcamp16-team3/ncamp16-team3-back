package tf.tailfriend.reserve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tf.tailfriend.reserve.entity.Payment;

@Repository
public interface PaymentDao extends JpaRepository<Payment, Integer>, CustomPaymentDao {
}
