package garder.quan.authservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Verifies Google-issued ID tokens (JWTs) from the "Sign in with Google" flow.
 * On success, returns the JWT payload (sub, email, email_verified, name, picture, …).
 */
@Service
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
            }
            Payload payload = idToken.getPayload();
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account email is not verified");
            }
            return payload;
        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not verify Google ID token", e);
        }
    }
}
