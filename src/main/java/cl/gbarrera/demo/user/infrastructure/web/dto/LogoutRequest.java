package cl.gbarrera.demo.user.infrastructure.web.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {
    @JsonProperty("refresh_token")
    private String refreshToken;
}
