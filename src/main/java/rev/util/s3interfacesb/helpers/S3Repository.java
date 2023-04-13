package rev.util.s3interfacesb.helpers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientV2Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Repository {
    @Getter
    private AmazonS3 s3Client;
    S3Repository(
            @Value("${s3.access_key}") String accesskey,
            @Value("${s3.secret_key}") String secretkey,
            @Value("${s3.provider_url}") String url,
            @Value("${s3.region}") String region
    ) {
        // remove / at the end of URL if any
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, region))
                .build();
    }

}
