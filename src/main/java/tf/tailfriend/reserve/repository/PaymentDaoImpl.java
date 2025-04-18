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
              AND f.facility_type_id = :facilityTypeId
        """);

        // 날짜 필터링 추가
        if (requestDto.getStartDate() != null) {
            sql.append(" AND p.created_at >= :startDate");
        }
        if (requestDto.getEndDate() != null) {
            sql.append(" AND p.created_at <= :endDate");
        }

        // 정렬 기준
        sql.append(" ORDER BY p.created_at DESC");

        // 쿼리 실행
        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("userId", requestDto.getUserId());
        query.setParameter("facilityTypeId", requestDto.getFacilityTypeId());

        if (requestDto.getStartDate() != null) {
            query.setParameter("startDate", requestDto.getStartDate());
        }
        if (requestDto.getEndDate() != null) {
            query.setParameter("endDate", requestDto.getEndDate());
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
              AND f.facility_type_id = :facilityTypeId
        """;

        if (requestDto.getStartDate() != null) {
            countSql += " AND p.created_at >= :startDate";
        }
        if (requestDto.getEndDate() != null) {
            countSql += " AND p.created_at <= :endDate";
        }

        // 전체 데이터 개수 쿼리 실행
        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("userId", requestDto.getUserId());
        countQuery.setParameter("facilityTypeId", requestDto.getFacilityTypeId());

        if (requestDto.getStartDate() != null) {
            countQuery.setParameter("startDate", requestDto.getStartDate());
        }
        if (requestDto.getEndDate() != null) {
            countQuery.setParameter("endDate", requestDto.getEndDate());
        }

        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = (int) Math.ceil((double) totalElements / requestDto.getSize());

        // ListResponseDto 반환
        return ListResponseDto.<PaymentInfoResponseDto>builder()
                .data(dtoList)
                .currentPage(requestDto.getPage())
                .size(requestDto.getSize())
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }
}