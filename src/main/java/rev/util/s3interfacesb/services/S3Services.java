package rev.util.s3interfacesb.services;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.mq.model.NotFoundException;
import com.amazonaws.services.s3.model.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import rev.util.s3interfacesb.helpers.S3Repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
public class S3Services {
    private final S3Repository s3Repository;
    private final String mainBucket;
    private final int maxfilenamelength;
    S3Services(
        S3Repository s3Repository,
        @Value("${s3.main_bucket}") String mainBucket,
        @Value("${s3.max_file_name_length}") int maxfilenamelength
    ) {
        this.s3Repository = s3Repository;
        this.mainBucket = mainBucket;
        this.maxfilenamelength = maxfilenamelength;
        this.createBucketIfNotExists(mainBucket);
    }
    private String getExtensionWithDot(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        } else {
            return fileName.substring(lastIndexOf);
        }
    }
    private String getPrefixedFileName(String prefixName, String fileName) {
        String fullFileName = prefixName + "-" + fileName;
        String extension = this.getExtensionWithDot(fullFileName);
        if (fullFileName.length() > maxfilenamelength) {
            fullFileName = fullFileName.substring(0, maxfilenamelength - extension.length()) + extension;
        }
        return fullFileName.replace(" ", "_");
    }
    private String getOriginalFileName(String prefixedFileName) {
        return prefixedFileName.substring(prefixedFileName.indexOf("-") + 1);
    }
    public void createBucket(String bucketName) throws RuntimeException {
        if (!s3Repository.getS3Client().doesBucketExistV2(bucketName)) {
            try {
                s3Repository.getS3Client().createBucket(bucketName);
            } catch (Exception e) {
                throw new RuntimeException("Bucket creation failed");
            }
        } else {
            throw new RuntimeException("Bucket already exists");
        }
    }

    public void createBucketIfNotExists(String bucketName) throws RuntimeException {
        if (!s3Repository.getS3Client().doesBucketExistV2(bucketName)) {
            try {
                s3Repository.getS3Client().createBucket(bucketName);
            } catch (Exception e) {
                throw new RuntimeException("Bucket creation failed");
            }
        }
    }
    public void deleteBucket(String bucketName) throws RuntimeException {
        if (s3Repository.getS3Client().doesBucketExistV2(bucketName)) {
            s3Repository.getS3Client().deleteBucket(bucketName);
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }
    public List<String> listBuckets() {
        return s3Repository.getS3Client().listBuckets().stream().map(Bucket::getName).toList();
    }

    public String uploadWithPrefix(String prefixName, String fileName, InputStreamResource isr, String contenttype, Integer contentlength) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contenttype);
            metadata.setContentLength(contentlength);
            String fullFileName = this.getPrefixedFileName(prefixName, fileName);
            s3Repository.getS3Client().putObject(mainBucket, fullFileName, isr.getInputStream(), metadata);
            return this.getOriginalFileName(fullFileName);
        } catch (Exception e) {
            throw new RuntimeException("File upload failed");
        }
    }

    public List<String> listObjectInPrefix(String prefixName) {
        if (s3Repository.getS3Client().doesBucketExistV2(mainBucket)) {
            return s3Repository.getS3Client().listObjectsV2(mainBucket).getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList().
                    stream().filter(s -> s.startsWith(prefixName)).toList();
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }

    public InputStreamResource retrieveFromPrefix(String prefixName, String fileName) {
        if (s3Repository.getS3Client().doesBucketExistV2(mainBucket)) {
            try {
                return new InputStreamResource(s3Repository.getS3Client().getObject(mainBucket, this.getPrefixedFileName(prefixName, fileName)).getObjectContent());
            } catch (SdkClientException e) {
                throw new NotFoundException("File does not exist");
            } catch (Exception e) {
                throw new RuntimeException("Something went wrong");
            }
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }
    public MediaType getContentType(String prefixName, String fileName) {
        if (s3Repository.getS3Client().doesBucketExistV2(mainBucket)) {
            try {
                return MediaType.valueOf(s3Repository.getS3Client().getObjectMetadata(mainBucket, this.getPrefixedFileName(prefixName, fileName)).getContentType());
            } catch (Exception e) {
                throw new RuntimeException("File does not exist");
            }
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }
    public Long getContentLength(String prefixName, String fileName) {
        if (s3Repository.getS3Client().doesBucketExistV2(mainBucket)) {
            try {
                return s3Repository.getS3Client().getObjectMetadata(mainBucket, this.getPrefixedFileName(prefixName, fileName)).getContentLength();
            } catch (Exception e) {
                throw new RuntimeException("File does not exist");
            }
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }
    public void deleteFromPrefix(String prefixName, String fileName) {
        if (s3Repository.getS3Client().doesBucketExistV2(mainBucket)) {
            s3Repository.getS3Client().deleteObject(mainBucket, this.getPrefixedFileName(prefixName, fileName));
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }

    public void renameInPrefix(String prefixName, String oldFileName, String newFileName) {
        if (s3Repository.getS3Client().doesBucketExistV2(mainBucket)) {
            s3Repository.getS3Client().copyObject(mainBucket, this.getPrefixedFileName(prefixName, oldFileName), mainBucket, this.getPrefixedFileName(prefixName, newFileName));
            s3Repository.getS3Client().deleteObject(mainBucket, this.getPrefixedFileName(prefixName, oldFileName));
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }

    public void copyObject(String bucketName, String fileName, String newBucketName, String newFileName) {
        if (s3Repository.getS3Client().doesBucketExistV2(bucketName) && s3Repository.getS3Client().doesBucketExistV2(newBucketName)) {
            s3Repository.getS3Client().copyObject(bucketName, fileName, newBucketName, newFileName);
        } else {
            throw new RuntimeException("Bucket does not exist");
        }
    }
}
