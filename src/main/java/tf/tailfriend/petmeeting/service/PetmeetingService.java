package tf.tailfriend.petmeeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.petmeeting.dto.PetFriendDTO;
import tf.tailfriend.petmeeting.repository.PetmeetingDAO;
import tf.tailfriend.user.entity.User;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PetmeetingService {

    private final PetmeetingDAO petmeetingDAO;

    public Page<PetFriendDTO> getFriends(String activityStatus, String dongName,
                                         String distance, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Pet> friends = petmeetingDAO.findByDongNameAndActivityStatus(dongName, Pet.ActivityStatus.valueOf(activityStatus), pageable);

        return friends.map(pet -> PetFriendDTO.buildByEntity(pet));
    }
}
