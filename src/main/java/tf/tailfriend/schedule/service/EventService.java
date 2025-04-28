package tf.tailfriend.schedule.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.schedule.entity.dto.EventDTO;
import tf.tailfriend.schedule.repository.EventDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventDao eventDao;

    @Transactional(readOnly = true)
    public List<EventDTO> getAllEvent() {
        return eventDao.findAll()
                .stream()
                .map(EventDTO::new)
                .collect(Collectors.toList());
    }

}
