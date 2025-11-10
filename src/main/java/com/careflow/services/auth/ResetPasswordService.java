package com.careflow.services.auth;

import com.careflow.dtos.auth.reset_password.*;
import com.careflow.exceptions.auth.InvalidTokenException;
import com.careflow.exceptions.auth.PasswordMismatchException;
import com.careflow.exceptions.auth.TooManyResetAttemptsException;
import com.careflow.models.PasswordHistory;
import com.careflow.models.PasswordResetToken;
import com.careflow.models.User;
import com.careflow.repositories.PasswordHistoryRepository;
import com.careflow.repositories.PasswordResetTokenRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.email.EmailService;
import com.careflow.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordService {
    private static final int MAX_ATTEMPTS_PER_HOUR = 100;
    private static final int TOKEN_EXPIRATION_DATE = 1000*60*5;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    public ResetPasswordResponse request (){
        User user = jwtUtils.getAuthenticatedUser();
        Date oneHourAgo = new Date(System.currentTimeMillis()-1000*60*60);
        Long recentAttempts  = tokenRepository.countByUserEmailAndCreatedAtAfter(user.getEmail(), oneHourAgo);
        if (recentAttempts >= MAX_ATTEMPTS_PER_HOUR){
            throw new TooManyResetAttemptsException("too many attempts try again later");
        }
        String verificationCode = generateVerificationCode();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(verificationCode)
                .tokenHash(passwordEncoder.encode(verificationCode))
                .expiresAt(new Date(System.currentTimeMillis()+TOKEN_EXPIRATION_DATE))
                .isUsed(false)
                .user(user)
                .build();
        tokenRepository.save(passwordResetToken);

            sendVerificationEmail(user.getEmail(),verificationCode);
        return ResetPasswordResponse.builder()
                .success(true)
                .message("verification code sent")
                .build();
    }

    public ResetPasswordVerifyResponse verify (ResetPasswordVerifyRequest request){
        log.info("token recieved : {}",request.getToken());
        User user = jwtUtils.getAuthenticatedUser();
        log.info("user authenticated : {]",user.getUsername());

        List<PasswordResetToken> passwordResetTokenList = tokenRepository.findByUserEmailAndIsUsedFalseAndExpiresAtAfter(user.getEmail(),new Date());
        log.info("old token list : {}",passwordResetTokenList);
        for(PasswordResetToken token:passwordResetTokenList){
            if(passwordEncoder.matches(request.getToken(),token.getTokenHash())){
                log.info("found token");
                return ResetPasswordVerifyResponse.builder().success(true)
                        .message("token is valid").build();
            }
        }
        log.info("didn't find token");
        return ResetPasswordVerifyResponse.builder().success(false).message("token is invalid").build();
    }

    @Transactional
    public PasswordResetResponse reset(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordMismatchException("Please make sure your password and confirm password are the same");
        }

        User user = jwtUtils.getAuthenticatedUser();

        // Fetch all valid tokens for the user
        List<PasswordResetToken> tokens = tokenRepository
                .findByUserEmailAndIsUsedFalseAndExpiresAtAfter(user.getEmail(), new Date());

        // Find the token that matches the request
        PasswordResetToken token = tokens.stream()
                .filter(t -> passwordEncoder.matches(request.getToken(), t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("Token is invalid or expired"));

        if (token.isExpired()) {
            throw new InvalidTokenException("Token has expired");
        }

        // Check password history
        List<PasswordHistory> passwordHistory = passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (PasswordHistory ph : passwordHistory) {
            if (passwordEncoder.matches(request.getNewPassword(), ph.getPasswordHash())) {
                return PasswordResetResponse.builder()
                        .success(false)
                        .message("Cannot reuse any of your last passwords.")
                        .build();
            }
        }

        // Save old password in history
        PasswordHistory ph = PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .build();
        passwordHistoryRepository.save(ph);

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all tokens for the user
        tokenRepository.invalidateAllTokensForUser(user.getId());

        // Send notification email
        sendPasswordChangedEmail(user.getEmail());

        return PasswordResetResponse.builder()
                .success(true)
                .message("Password has changed successfully")
                .build();
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    private void sendVerificationEmail(String email, String code) {
        String subject = "Password Reset Verification Code";
        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Password Reset Request</h2>
                <p>Your verification code is:</p>
                <div style="font-size: 32px; font-weight: bold; color: #007bff; 
                            padding: 20px; background-color: #f8f9fa; 
                            border-radius: 5px; text-align: center; 
                            letter-spacing: 5px;">
                    %s
                </div>
                <p>This code will expire in %d minutes.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <hr>
                <small style="color: #6c757d;">
                    For security reasons, never share this code with anyone.
                </small>
            </div>
            """, code, TOKEN_EXPIRATION_DATE/1000/60);

        emailService.sendHtmlEmail(email, subject, htmlContent);
    }
    private void sendPasswordChangedEmail(String email) {
        String subject = "Password Changed Successfully";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Password Changed</h2>
                <p>Your password has been successfully changed.</p>
                <p>If you didn't make this change, please contact support immediately.</p>
                <hr>
                <small style="color: #6c757d;">
                    This is an automated message, please do not reply.
                </small>
            </div>
            """;

        emailService.sendHtmlEmail(email, subject, htmlContent);
    }
}
