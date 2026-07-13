package hei.exam.prog.file;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
public class BucketComponent {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucketName = "votre-nom-de-bucket-s3"; // À adapter ou lier à une property

  public BucketComponent(S3Client s3Client, S3Presigner s3Presigner) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
  }

  public void upload(File file, String key) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(key).build();
    s3Client.putObject(putObjectRequest, file.toPath());
  }

  public URL presign(String key, Duration expiration) {
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(expiration)
            .getObjectRequest(builder -> builder.bucket(bucketName).key(key).build())
            .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
    return presignedRequest.url();
  }
}
