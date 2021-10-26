package in.org.iudx.adaptor.codegen;

import java.io.Serializable;

/**
 * {@link MinioConfig} - Minio configuration class
 * Encapsulates Minio client information such as endpoint,
 * credentials, bucket name etc.,
 * Note: This must be serializable since flink passes
 * this across its instances.
 *
 */

public class MinioConfig implements Serializable {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
    private final String objectName;


    private MinioConfig(Builder builder) {
        this.endpoint = builder.endpoint;
        this.accessKey = builder.accessKey;
        this.secretKey = builder.secretKey;
        this.bucketName = builder.bucketName;
        this.objectName = builder.objectName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }


    public static class Builder {

        private final String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName = "default-bucket";
        private String objectName = "default-object";


        public Builder(String endpoint) {
            this.endpoint = endpoint;
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

        public Builder object(String objectName) {
            this.objectName = objectName;
            return this;
        }

        public MinioConfig build() {
            return new MinioConfig(this);
        }
    }

}

