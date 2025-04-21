package tf.tailfriend.petsitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.entity.PetSitter;
import tf.tailfriend.petsitter.repository.PetSitterDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetSitterService {

    private final PetSitterDao petSitterDao;

    @Transactional(readOnly = true)
    public Page<PetSitterResponseDto> findAll(Pageable pageable) {
        Page<PetSitter> petSitters = petSitterDao.findAll(pageable);

        List<PetSitterResponseDto> petSitterDtos = petSitters.getContent().stream()
                .map(PetSitterResponseDto::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(petSitterDtos, pageable, petSitters.getTotalElements());
    }
}
