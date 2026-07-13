package hei.exam.prog.endpoint.rest.controller.health;

import hei.exam.prog.endpoint.event.EventProducer;
import hei.exam.prog.endpoint.event.gen.EmailNotificationEvent;
import hei.exam.prog.entity.Submission;
import hei.exam.prog.file.BucketComponent;
import hei.exam.prog.repository.SubmissionRepository;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@RequestMapping("/submission")
public class SubmissionController {

  private final SubmissionRepository repository;
  private final BucketComponent bucketComponent;
  private final EventProducer eventProducer;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Submission createSubmission(
      @RequestParam("email") String email, @RequestParam("file") MultipartFile multipartFile)
      throws IOException {

    // 1. Validation stricte du format PNG ou JPEG
    String contentType = multipartFile.getContentType();
    if (contentType == null
        || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Seuls les formats PNG et JPEG sont acceptés.");
    }

    // 2. Préparation de l'entité (L'ID String sera généré par Hibernate grâce à @GeneratedValue)
    Submission submission = new Submission();
    submission.setEmail(email);
    submission.setCreatedAt(Instant.now());

    // Nom temporaire pour S3 avant d'avoir l'ID généré
    String originalName = multipartFile.getOriginalFilename();
    submission.setFile_name("images/" + Instant.now().toEpochMilli() + "_" + originalName);

    // 3. Sauvegarde synchrone en Base de Données (génère l'ID String)
    Submission savedSubmission = repository.save(submission);

    // 4. Upload du fichier vers AWS S3 avec le bon dossier
    File tempFile = File.createTempFile("upload-", null);
    multipartFile.transferTo(tempFile);
    bucketComponent.upload(tempFile, savedSubmission.getFile_name());

    // 5. Envoi asynchrone dans la file d'attente de Poja
    EmailNotificationEvent event =
        EmailNotificationEvent.builder()
            .submissionId(savedSubmission.getId())
            .email(savedSubmission.getEmail())
            .fileName(savedSubmission.getFile_name())
            .build();
    eventProducer.accept(List.of(event));

    // 6. Retour synchrone immédiat de l'entité créée
    return savedSubmission;
  }

  @GetMapping("/{id}")
  public Submission getSubmissionById(@PathVariable String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Soumission introuvable."));
  }
}
