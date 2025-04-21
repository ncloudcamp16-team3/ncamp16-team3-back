package tf.tailfriend.reserve.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.reserve.entity.Reserve;

public interface ReserveDao extends JpaRepository<Reserve, Integer> {

    Slice<Reserve> getReserves(int page, int size);

}
