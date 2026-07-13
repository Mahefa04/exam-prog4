package hei.exam.prog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "file_submission")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String file_name;
    private String email;
    private Instant createdAt;
}