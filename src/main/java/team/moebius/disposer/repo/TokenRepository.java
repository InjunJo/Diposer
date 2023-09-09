package team.moebius.disposer.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.moebius.disposer.entity.Token;

public interface TokenRepository extends JpaRepository<Token,Long> {

    @Query("SELECT t from Token t where t.roomId = :roomId and t.tokenKey = :tokenKey")
    Optional<Token> findTokenByRoomIdAndTokenKey(@Param("roomId") String roomId, @Param("tokenKey") String tokenKey);

}
