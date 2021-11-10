package in.org.iudx.adaptor.server.util;

import io.minio.*;
import io.minio.messages.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MinioClientHelper {

    private final MinioClient minioClient;

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
    private final String objectName;

    private static final Logger LOGGER = LogManager.getLogger(MinioClientHelper.class);


    public MinioClientHelper(MinioConfig minioConfig) {
        this.endpoint = minioConfig.getEndpoint();
        this.accessKey = minioConfig.getAccessKey();
        this.secretKey = minioConfig.getSecretKey();
        this.bucketName = minioConfig.getBucketName();
        this.objectName = minioConfig.getObjectName();

        if(this.accessKey != null && this.secretKey != null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        else {
            minioClient = MinioClient.builder().endpoint(endpoint).build();
        }

        makeBucket();
        makeObject();
    }

    private void makeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                LOGGER.info("MinioClient: Bucket " + bucketName + " created.");
            }
        }
        catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private void makeObject() {
        try {
            if (!isObjectPresent()) {
                putObject(new byte[]{}); // creates empty object
                LOGGER.info("MinioClient: Object " + objectName + " created.");
            }
        }
        catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void putObject(byte[] object) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(object), object.length, -1)
                            .build());
        }
        catch(Exception e){
            LOGGER.error(e);
        }
    }

    public void putObject(byte[] object, String objectName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(object), object.length, -1)
                            .build());
        }
        catch(Exception e){
            LOGGER.error(e);
        }
    }

    public byte[] getObject() {
        try (InputStream stream1 = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
            return stream1.readAllBytes();
        }
        catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    public byte[] getObject(String objectName) {
        try (InputStream stream1 = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
            return stream1.readAllBytes();
        }
        catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    public void removeObject() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        }
        catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void removeObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        }
        catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void listObjects() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build());

            for(Result<Item> temp: results) {
                System.out.println(temp.get().objectName());
            }
        }
        catch (Exception e){
            LOGGER.error(e);
        }
    }

    public boolean isObjectPresent() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build());

            for(Result<Item> temp: results) {
                if(temp.get().objectName().equals(objectName)) {
                    return true;
                }
            }
        }
        catch (Exception e){
            LOGGER.error(e);
        }

        return false;
    }

}