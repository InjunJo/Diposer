package team.moebius.disposer.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.moebius.disposer.entity.Recipient;

public interface RecipientRepository extends JpaRepository<Recipient,Long> {

    @Query("SELECT r from Recipient r where r.token.id = :tokenId and r.userId is null")
    List<Recipient> findReceivableAllByToken(@Param("tokenId") Long tokenId);

    @Query("SELECT r from Recipient r where r.token.id = :tokenId and r.userId is not null")
    List<Recipient>
    findReceiveAllByToken(@Param("tokenId") Long tokenId);

    @Query("SELECT r from Recipient r where r.token.id = :tokenId")
    List<Recipient> findAllByTokenId(@Param("tokenId") Long tokenId);

    @Query("SELECT r from Recipient r where r.token.id = :tokenId and r.userId = :userId")
    Optional<Recipient> findAlreadyReceiveUser(@Param("tokenId") Long tokenId,@Param("userId") Long userId);
}
