package team.moebius.disposer.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.moebius.disposer.entity.DistributionToken;

public interface DistributionTokenRepository extends JpaRepository<DistributionToken,Long> {

    @Query("SELECT t from DistributionToken t where t.roomId = :roomId and t.tokenKey = :tokenKey and t.createdDateTime = :createdTime")
    Optional<DistributionToken> findTokenByRelatedData(@Param("roomId") String roomId,
        @Param("tokenKey") String tokenKey, @Param("createdTime") long createdTime);

}
