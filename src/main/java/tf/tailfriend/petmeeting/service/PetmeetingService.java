package tf.tailfriend.petmeeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.petmeeting.dto.PetFriend;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PetmeetingService {
    public Page<PetFriend> getFriends() {

    }
}
