package tf.tailfriend.global.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@Service
public class NCPObjectStorageService implements StorageService {

    @Value("${ncp.end-point}")
    private String endPoint;

    @Value("${ncp.region-name}")
    private String regionName;

    @Value("${ncp.access-key}")
    private String accessKey;

    @Value("${ncp.secret-key}")
    private String secretKey;

    @Value("${ncp.bucket-name}")
    private String bucketName;

    private AmazonS3 s3;

    @PostConstruct
    public void init() {
        System.getProperties().setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");

        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)
                .build();
    }


    @Override
    public void upload(String filePath, InputStream fileIn) throws StorageServiceException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/octet-stream");

            PutObjectRequest request = new PutObjectRequest(bucketName, filePath, fileIn, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            s3.putObject(request);
        } catch (Exception e) {
            throw new StorageServiceException(e);
        }
    }


    @Override
    public void delete(String filePath) throws StorageServiceException {
        try {
            s3.deleteObject(bucketName, filePath);
        } catch (Exception e) {
            throw new StorageServiceException(e);
        }
    }
}
