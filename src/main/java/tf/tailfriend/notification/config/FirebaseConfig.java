package tf.tailfriend.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;

    @PostConstruct
    public void initialize() {
        try {
            String credentialsJson = buildCredentialsJson();
            ByteArrayInputStream serviceAccount =
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildCredentialsJson() {
        return "{"
                + "\"type\": \"" + firebaseProperties.getType() + "\","
                + "\"project_id\": \"" + firebaseProperties.getProjectId() + "\","
                + "\"private_key_id\": \"" + firebaseProperties.getPrivateKeyId() + "\","
                + "\"private_key\": \"" + firebaseProperties.getPrivateKey().replace("\\n", "\n") + "\","
                + "\"client_email\": \"" + firebaseProperties.getClientEmail() + "\","
                + "\"client_id\": \"" + firebaseProperties.getClientId() + "\","
                + "\"auth_uri\": \"" + firebaseProperties.getAuthUri() + "\","
                + "\"token_uri\": \"" + firebaseProperties.getTokenUri() + "\","
                + "\"auth_provider_x509_cert_url\": \"" + firebaseProperties.getAuthProviderX509CertUrl() + "\","
                + "\"client_x509_cert_url\": \"" + firebaseProperties.getClientX509CertUrl() + "\","
                + "\"universe_domain\": \"" + firebaseProperties.getUniverseDomain() + "\""
                + "}";
    }
}
