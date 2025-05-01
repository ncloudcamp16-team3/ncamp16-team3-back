package tf.tailfriend.global.service;

import java.io.InputStream;

public interface StorageService {


    void upload(String filePath, InputStream fileIn) throws StorageServiceException;
    void openUpload(String filePath, InputStream fileIn) throws StorageServiceException;
    void delete(String filePath) throws StorageServiceException;

    String generatePresignedUrl(String filePath);

}
