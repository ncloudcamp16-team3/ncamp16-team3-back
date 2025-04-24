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
import tf.tailfriend.pet.repository.PetPhotoDao;
import tf.tailfriend.petmeeting.dto.PetFriendDTO;
import tf.tailfriend.petmeeting.dto.PetPhotoDTO;
import tf.tailfriend.petmeeting.exception.ActivityStatusNoneException;
import tf.tailfriend.petmeeting.exception.FindDongException;
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
    private final PetPhotoDao petPhotoDao;
    private final NCPObjectStorageService ncpObjectStorageService;

    public Page<PetFriendDTO> getFriends(String activityStatus, String dongName,
                                         String distance, int page, int size, double latitude, double longitude) {

        if( Pet.ActivityStatus.valueOf(activityStatus) == Pet.ActivityStatus.NONE){
            throw new ActivityStatusNoneException();
        }

        Pageable pageable = PageRequest.of(page, size);
        List<String> dongs = getNearbyDongs(dongName, Distance.valueOf(distance).getValue());

        Page<PetFriendDTO> friends = petmeetingDAO.findByDongNamesAndActivityStatus(
                dongs, activityStatus, latitude, longitude, pageable);

        /*//테스트용 전부 요청
        Page<PetFriendDTO> friends = petmeetingDAO.findAllWithoutFilters(latitude, longitude, pageable);*/

        for(PetFriendDTO item: friends.getContent()){
            List<PetPhotoDTO> photos = petPhotoDao.findByPetId(item.getId());
            item.setPhotosAndThumbnail(photos);
        }

        File defaultImgFile = fileDao.findById(1)
                .orElseThrow(() -> new FindFileException());
        String defaultImgUrl = ncpObjectStorageService.generatePresignedUrl(defaultImgFile.getPath());
        for(PetFriendDTO friend: friends.getContent()){
            setPresignedUrl(friend, defaultImgFile, defaultImgUrl);
        }

        return friends;
    }

    private void setPresignedUrl(PetFriendDTO friend, File defaultImgFile, String defaultImgUrl) {

        if(friend.getThumbnail() == null){
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
        Dong currentDong = dongDAO.findByName(name)
                .orElseThrow(() -> new FindDongException());

        return dongDAO.findNearbyDongs(
                currentDong.getLatitude(),
                currentDong.getLongitude(),
                count
        );
    }
}
