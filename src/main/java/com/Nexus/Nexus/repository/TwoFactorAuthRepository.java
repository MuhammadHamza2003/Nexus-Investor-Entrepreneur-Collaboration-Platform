package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.TwoFactorAuth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends MongoRepository<TwoFactorAuth, String> {
    
    Optional<TwoFactorAuth> findByUserIdAndPurposeAndVerifiedFalse(String userId, String purpose);
    
    Optional<TwoFactorAuth> findByEmailAndOtpAndPurposeAndVerifiedFalse(String email, String otp, String purpose);
    
    List<TwoFactorAuth> findByExpiresAtBeforeAndVerifiedFalse(LocalDateTime dateTime);
    
    void deleteByUserIdAndPurpose(String userId, String purpose);
    
    void deleteByExpiresAtBeforeAndVerifiedFalse(LocalDateTime dateTime);
}