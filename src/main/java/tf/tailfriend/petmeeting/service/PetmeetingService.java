package tf.tailfriend.petmeeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.global.entity.Dong;
import tf.tailfriend.global.service.NCPObjectStorageService;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.petmeeting.dto.PetFriendDTO;
import tf.tailfriend.petmeeting.dto.PetPhotoDTO;
import tf.tailfriend.petmeeting.exception.FIndDongException;
import tf.tailfriend.petmeeting.exception.FindFileException;
import tf.tailfriend.petmeeting.repository.DongDAO;
import tf.tailfriend.petmeeting.repository.PetmeetingDAO;
import tf.tailfriend.user.distance.Distance;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PetmeetingService {

    private final PetmeetingDAO petmeetingDAO;
    private final DongDAO dongDAO;
    private final FileDao fileDao;
    private final NCPObjectStorageService ncpObjectStorageService;

    public Page<PetFriendDTO> getFriends(String activityStatus, String dongName,
                                         String distance, int page, int size, double latitude, double longitude) {

        Pageable pageable = PageRequest.of(page, size);

        List<String> dongs = getNearbyDongs(dongName, Distance.fromString(distance).getDistanceValue());

        //테스트용 전부 가져오기
        //Page<Pet> friends = petmeetingDAO.findByActivityStatus(Pet.ActivityStatus.valueOf(activityStatus), pageable);

        //인접한 동 리스트 + 활동상태로 가져오기
        Page<Pet> friends = petmeetingDAO.findByDongNamesAndActivityStatus(dongs, Pet.ActivityStatus.valueOf(activityStatus), pageable);
        Page<PetFriendDTO> friendsDto = friends.map(pet -> PetFriendDTO.buildByEntity(pet));

        for(PetFriendDTO friend: friendsDto.getContent()){
            makePetPhotoPresignedUrl(friend);
        }

        return friendsDto;
    }

    private void makePetPhotoPresignedUrl(PetFriendDTO friend) {

        if(friend.getThumbnail() == null){
            File defaultImgFile = fileDao.findById(1)
                    .orElseThrow(() -> new FindFileException());

            String defaultImgUrl;
            defaultImgUrl = ncpObjectStorageService.generatePresignedUrl(defaultImgFile.getPath());

            friend.setThumbnail(defaultImgFile.getId());
            friend.getPhotos().add(PetPhotoDTO.builder()
                    .id(defaultImgFile.getId())
                    .path(defaultImgUrl)
                    .thumbnail(true)
                    .build()
            );
        }else {
            List<PetPhotoDTO> petPhotos = friend.getPhotos();

            for(PetPhotoDTO petPhotoDTO: petPhotos){
                petPhotoDTO.setPath(ncpObjectStorageService.generatePresignedUrl(petPhotoDTO.getPath()));
            }
        }
    }

    private List<String> getNearbyDongs(String name, int count) {
        Dong current = dongDAO.findByName(name)
                .orElseThrow(() -> new FIndDongException());

        return dongDAO.findNearbyDongs(
                current.getLatitude(),
                current.getLongitude(),
                count
        );
    }
}
