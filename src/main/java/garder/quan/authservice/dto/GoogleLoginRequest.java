package garder.quan.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    /** ID token (JWT) issued by Google to the front-end after the user signs in. */
    @NotBlank
    private String idToken;
}
