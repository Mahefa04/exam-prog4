package hei.exam.prog.endpoint.rest.controller;

import hei.exam.prog.entity.Submission;
import hei.exam.prog.file.BucketComponent;
import hei.exam.prog.repository.SubmissionRepository;
import hei.exam.prog.service.EmailService;
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
@RequestMapping("/submissions")
public class SubmissionController {

  private final SubmissionRepository repository;
  private final BucketComponent bucketComponent;
  private final EmailService emailService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Submission createSubmission(
      @RequestParam("email") String email, @RequestParam("file") MultipartFile multipartFile)
      throws IOException {

    String contentType = multipartFile.getContentType();
    if (contentType == null
        || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Seuls les formats PNG et JPEG sont acceptés.");
    }

    Submission submission = new Submission();
    submission.setEmail(email);
    submission.setCreatedAt(Instant.now());

    String originalName = multipartFile.getOriginalFilename();
    submission.setFile_name("images/" + Instant.now().toEpochMilli() + "_" + originalName);

    Submission savedSubmission = repository.save(submission);

    File tempFile = File.createTempFile("upload-", null);
    multipartFile.transferTo(tempFile);
    bucketComponent.upload(tempFile, savedSubmission.getFile_name());

    emailService.sendEmailWithS3LinkAsync(
        savedSubmission.getEmail(), savedSubmission.getFile_name());

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
