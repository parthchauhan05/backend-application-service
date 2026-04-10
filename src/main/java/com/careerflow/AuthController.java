package com.careerflow;

import com.careerflow.DTO.ForgotPasswordRequest;
import com.careerflow.DTO.ResetPasswordRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.now;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${supabase.auth.url}")
    private String supabaseAuthUrl;

    @Value("${supabase.auth.key}")
    private String supabaseKey;

    @Value("${app.frontend.reset-password-url}")
    private String frontendResetPasswordUrl;


    @Autowired
    private JwtUtil jwtUtil;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    record LoginRequest(String email, String password) {}
    record SignupRequest(String email, String password, String name) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws JsonProcessingException {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("apikey", supabaseKey);


            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(Map.of(
                            "email", request.email(),
                            "password", request.password()
                    )),
                    httpHeaders
            );
            ResponseEntity<String> response = restTemplate.postForEntity(
                    supabaseAuthUrl + "/token?grant_type=password",
                    entity,
                    String.class
            );
            if(response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String userId = root.path("user").path("id").asText();
                String email = root.path("user").path("email").asText(request.email());

                String token = jwtUtil.generateToken(userId, email);
                return ResponseEntity.ok(Map.of("token", token, "userId", userId, "email", email));
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (HttpClientErrorException.Unauthorized e) {
            AuthError error = buildErrorFromSupabase(e, 401, "Invalid email or password");
            return ResponseEntity.status(401).body(error);
        } catch (HttpClientErrorException e) {
            AuthError error = buildErrorFromSupabase(e, e.getStatusCode().value(), "Auth error");
            return ResponseEntity.status(e.getStatusCode()).body(error);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Auth error: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("apikey", supabaseKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                    Map.of("email", request.email(), "password", request.password(),
                            "data", Map.of("full_name", request.name())),
                    httpHeaders
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    supabaseAuthUrl + "/signup",
                    entity,
                    String.class
            );

            if(response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String userId = root.path("user").path("id").asText();
                String email = root.path("user").path("email").asText(request.email());

                String token = jwtUtil.generateToken(userId, email);
                return ResponseEntity.ok(Map.of("token", token, "userId", userId, "email", email));
            }
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "code", 500,
                    "message", "Signup failed",
                    "timestamp", now()
            ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            if(request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "code", 400,
                        "message", "Email is required",
                        "timestamp", now()
                ));
            }
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("apikey", supabaseKey);

            Map<String, Object> body = Map.of(
                    "email", request.getEmail(),
                    "code_challenge", "",
                    "code_challenge_method", "",
                    "gotrue_meta_security", new HashMap<>(),
                    "redirect_to", frontendResetPasswordUrl
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                    body,
                    httpHeaders
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    supabaseAuthUrl + "/recover",
                    entity,
                    String.class
            );
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "If the email exists, a password reset link has been sent."
            ));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "code", 500,
                    "message", "Forgot password request failed",
                    "timestamp", now()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) throws JsonProcessingException {
        try {
            if (request.getAccessToken() == null || request.getAccessToken().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "code", 400,
                        "message", "Access token is required",
                        "timestamp", now()
                ));
            }

            if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "code", 400,
                        "message", "Password must be at least 8 characters",
                        "timestamp", now()
                ));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);
            headers.setBearerAuth(request.getAccessToken());

            Map<String, Object> body = Map.of(
                    "password", request.getNewPassword()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    supabaseAuthUrl + "/user",
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Password reset successful"
            ));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(buildErrorFromSupabase(e, e.getRawStatusCode(),e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "code", 500,
                    "message", "Reset password failed",
                    "timestamp", now()
            ));
        }
    }

    private AuthError buildErrorFromSupabase(HttpClientErrorException e, int defaultCode, String defaultMessage) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(e.getResponseBodyAsString());
        int code = root.has("code") ? root.get("code").asInt(defaultCode) : defaultCode;
        String message = root.has("msg") ? root.get("msg").asText(defaultMessage) : defaultMessage;
        return new AuthError(code, message, now().toString());
    }

}
