package hei.exam.prog.repository;

import hei.exam.prog.entity.Submission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {}
