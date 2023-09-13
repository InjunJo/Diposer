package team.moebius.disposer.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.moebius.disposer.entity.RecipientResult;

public interface RecipientResultRepository extends JpaRepository<RecipientResult,Long> {

    @Query("SELECT r from RecipientResult r where r.distributionToken.id = :tokenId")
    Optional<RecipientResult> findResultByTokeId(@Param("tokenId") Long TokenId);
}
