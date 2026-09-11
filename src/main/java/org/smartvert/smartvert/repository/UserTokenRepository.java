package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.AppUser;
import org.smartvert.smartvert.model.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    Optional<UserToken> findByToken(String token);

    Optional<UserToken> findByTokenAndTokenType(String token, UserToken.TokenType tokenType);

    Optional<UserToken> findTopByUserAndTokenTypeOrderByCreatedAtDesc(AppUser user, UserToken.TokenType tokenType);

    void deleteByUser(AppUser user);
}
