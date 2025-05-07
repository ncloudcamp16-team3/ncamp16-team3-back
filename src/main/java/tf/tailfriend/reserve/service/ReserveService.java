package tf.tailfriend.reserve.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.exception.GetPostException;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.repository.FacilityDao;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.reserve.dto.ReserveDetailResponseDto;
import tf.tailfriend.reserve.dto.ReserveListResponseDto;
import tf.tailfriend.reserve.dto.ReserveRequestDto;
import tf.tailfriend.reserve.entity.Payment;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.PaymentDao;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.exception.UnauthorizedException;
import tf.tailfriend.user.repository.UserDao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReserveService {

    private final ReserveDao reserveDao;
    private final DateTimeFormatProvider dateTimeFormatProvider;
    private final UserDao userDao;
    private final RedisService redisService;
    private final FacilityDao facilityDao;
    private final StorageService storageService;
    private final PaymentDao paymentDao;

    @Transactional
    public Reserve saveReserveAfterPayment(String merchantPayKey) {
        ReserveRequestDto dto = redisService.getTempReserve(merchantPayKey); // ✅ prefix 없이 그대로 사용


        if (dto == null) throw new IllegalArgumentException("Redis에 예약 정보 없음");

        User user = userDao.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Facility facility = facilityDao.findById(dto.getFacilityId())
                .orElseThrow(() -> new IllegalArgumentException("시설 없음"));

        Reserve reserve = Reserve.builder()
                .user(user)
                .facility(facility)
                .entryTime(dto.getEntryTime())
                .exitTime(dto.getExitTime() != null
                        ? dto.getExitTime()
                        : dto.getEntryTime().plusHours(1)) // ✅ 없으면 entryTime + 1시간
                .amount(dto.getAmount())
                .reserveStatus(true)
                .build();

        Reserve savedReserve = reserveDao.save(reserve);

        Payment payment = Payment.builder()
                .uuid(merchantPayKey) // 또는 uuidService.create()
                .reserve(savedReserve)
                .price(dto.getAmount())
                .build();

        paymentDao.save(payment);
        redisService.deleteTempReserve("reserve:" + merchantPayKey);

        return savedReserve;
    }

    @Transactional
    public List<ReserveListResponseDto> getReserveListByUser(Integer userId) {
        List<Reserve> reserves = reserveDao.findByUserId(userId);

        return reserves.stream()
                .map(reserve -> {
                    var facility = reserve.getFacility();

                    // 첫 번째 이미지 경로 추출
                    String imagePath = facility.getPhotos().stream()
                            .findFirst()
                            .map(photo -> photo.getFile().getPath())
                            .orElse(null);

                    String imageUrl = (imagePath != null)
                            ? storageService.generatePresignedUrl(imagePath)
                            : null;

                    return ReserveListResponseDto.builder()
                            .id(reserve.getId())
                            .name(facility.getName())
                            .address(facility.getAddress())
                            .type(facility.getFacilityType().getName()) // Enum 또는 객체에 따라 toString/직접 접근
                            .status(reserve.getReserveStatus())
                            .entryTime(reserve.getEntryTime())
                            .exitTime(reserve.getExitTime())
                            .amount(reserve.getAmount())
                            .image(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ReserveDetailResponseDto getReserveDetail(Integer reserveId) {
        Reserve reserve = reserveDao.findById(reserveId)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

        Facility facility = reserve.getFacility();
        String imagePath = facility.getPhotos().stream()
                .findFirst()
                .map(p -> p.getFile().getPath())
                .orElse(null);

        String imageUrl = imagePath != null ? storageService.generatePresignedUrl(imagePath) : null;

        return ReserveDetailResponseDto.builder()
                .id(reserve.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .type(facility.getFacilityType().getName())
                .status(reserve.getReserveStatus())
                .entryTime(reserve.getEntryTime())
                .exitTime(reserve.getExitTime())
                .amount(reserve.getAmount())
                .image(imageUrl)
                .latitude(reserve.getFacility().getLatitude())
                .longitude(reserve.getFacility().getLongitude())
                .build();
    }

    @Transactional
    public void cancelReserve(Integer reserveId, Integer userId) {
        Reserve deleteEntity = reserveDao.findById(reserveId)
                .orElseThrow(() -> new GetPostException());

        if (!deleteEntity.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        reserveDao.delete(deleteEntity);
    }
}

