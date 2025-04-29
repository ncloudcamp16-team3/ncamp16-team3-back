package tf.tailfriend.reserve.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.PaymentInfoResponseDto;
import tf.tailfriend.reserve.dto.PaymentListRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class PaymentDaoImpl implements CustomPaymentDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public ListResponseDto<PaymentInfoResponseDto> findPaymentsByRequestDto(PaymentListRequestDto requestDto) {

        // 기본 SQL 쿼리 작성
        StringBuilder sql = new StringBuilder("""
            SELECT
                p.id,
                f.name,
                p.created_at,
                p.price
            FROM payments p
            JOIN reserves r ON p.reserve_id = r.id
            JOIN facilities f ON r.user_id = f.id
            WHERE r.user_id = :userId
        """);

        // 날짜 필터링 추가
        if (requestDto.getDatetimeRange().getOpenDateTime() != null) {
            sql.append(" AND p.created_at >= :startDate");
        }
        if (requestDto.getDatetimeRange().getCloseDateTime() != null) {
            sql.append(" AND p.created_at <= :endDate");
        }

        // 정렬 기준
        sql.append(" ORDER BY p.created_at DESC");

        // 쿼리 실행
        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("userId", requestDto.getUserId());

        if (requestDto.getDatetimeRange().getOpenDateTime() != null) {
            query.setParameter("startDate", requestDto.getDatetimeRange().getOpenDateTime());
        }
        if (requestDto.getDatetimeRange().getCloseDateTime() != null) {
            query.setParameter("endDate", requestDto.getDatetimeRange().getCloseDateTime());
        }

        // 데이터 조회
        List<Object[]> results = query.getResultList();

        // DTO 변환
        List<PaymentInfoResponseDto> dtoList = results.stream()
                .map(row -> PaymentInfoResponseDto.builder()
                        .id((Integer) row[0])
                        .name((String) row[1])
                        .createdAt(row[2].toString())  // 필요한 포맷으로 변환 가능
                        .price((Integer) row[3])
                        .build())
                .collect(Collectors.toList());

        // 전체 데이터 개수 조회 (페이징용)
        String countSql = """
            SELECT COUNT(*) 
            FROM payments p
            JOIN reserves r ON p.reserve_id = r.id
            JOIN facilities f ON r.user_id = f.id
            WHERE r.user_id = :userId
        """;

        if (requestDto.getDatetimeRange().getOpenDateTime() != null) {
            countSql += " AND p.created_at >= :startDate";
        }
        if (requestDto.getDatetimeRange().getCloseDateTime() != null) {
            countSql += " AND p.created_at <= :endDate";
        }

        // 전체 데이터 개수 쿼리 실행
        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("userId", requestDto.getUserId());

        if (requestDto.getDatetimeRange().getOpenDateTime() != null) {
            countQuery.setParameter("startDate", requestDto.getDatetimeRange().getOpenDateTime());
        }
        if (requestDto.getDatetimeRange().getCloseDateTime() != null) {
            countQuery.setParameter("endDate", requestDto.getDatetimeRange().getCloseDateTime());
        }

        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        boolean lastPage = (requestDto.getPage() + 1) * requestDto.getSize() >= totalElements;

        // ListResponseDto 반환
        return ListResponseDto.<PaymentInfoResponseDto>builder()
                .data(dtoList)
                .currentPage(requestDto.getPage())
                .size(requestDto.getSize())
                .last(lastPage)
                .totalElements(totalElements)
                .build();
    }
}