package jobApplicationApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse {

    private HttpStatus status;
    private String message;

    public RequestResponse(HttpStatus status) {
        this.status = status;
    }
}
