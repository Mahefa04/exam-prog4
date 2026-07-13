package hei.exam.prog.endpoint.event.gen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailNotificationEvent {
  @JsonProperty("submission_id")
  private String submissionId;

  @JsonProperty("email")
  private String email;

  @JsonProperty("file_name")
  private String fileName;
}
