package in.org.iudx.adaptor.utils;

import io.minio.*;
import io.minio.messages.Item;
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinioClientHelper {

    private final MinioClient minioClient;

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;

    private static final Logger LOGGER = LogManager.getLogger(MinioClientHelper.class);


    private MinioClientHelper(Builder builder) {
        this.endpoint = builder.endpoint;
        this.accessKey = builder.accessKey;
        this.secretKey = builder.secretKey;
        this.bucketName = builder.bucketName;

        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        makeBucket();
    }

    private void makeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                LOGGER.info(" Bucket: " + bucketName + " created.");
            }
        }catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void putObject(String objectName, byte[] object) {
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

    public static class Builder {

        private String endpoint = "http://minio1:9000";
        private String accessKey = "minio";
        private String secretKey = "minio123";
        private String bucketName = "custom-state";

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder credentials(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            return this;
        }

        public Builder bucket(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public MinioClientHelper build() {
            return new MinioClientHelper(this);
        }
    }

}