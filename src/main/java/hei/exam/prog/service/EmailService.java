package hei.exam.prog.service;

import hei.exam.prog.file.BucketComponent;
import hei.exam.prog.mail.Email;
import jakarta.mail.internet.InternetAddress;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import hei.exam.prog.mail.Mailer;

import java.net.URL;
import java.time.Duration;
import java.util.List;

@Service
@AllArgsConstructor
public class EmailService {

    private final BucketComponent bucketComponent;
    private final Mailer mailer;

    @Async
    public void sendEmailWithS3LinkAsync(String email, String fileName) {
        try {
            URL s3Url = bucketComponent.presign(fileName, Duration.ofDays(1));

            System.out.println("Envoi asynchrone du mail à : " + email);
            System.out.println("Lien de l'image joint : " + s3Url.toString());


            mailer.accept(new hei.exam.prog.mail.Email(
                    new InternetAddress("noreply@hei.com"),
                    List.of(new InternetAddress(email)),
                    List.of(),
                    "Votre image est en ligne !",
                    "Voici votre lien : " + s3Url.toString(),
                    List.of()
            ));

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement asynchrone de l'email: " + e.getMessage());
        }
    }
}
