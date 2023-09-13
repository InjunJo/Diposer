package team.moebius.disposer.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.entity.DistributionToken;

@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DataJpaTest
class DistributionTokenRepositoryTest {

    @Autowired
    TokenRepository tokenRepository;

    long createdTime;

    DistributionToken distributionToken;

    final String TOKEN_KEY = "abc";

    final String ROOM_ID = "ABC";

    @BeforeEach
    private void setUp(){

        long now = ZonedDateTime.now().toInstant().toEpochMilli();

        createdTime = now;

        this.distributionToken = DistributionToken.builder()
            .tokenKey(TOKEN_KEY)
            .createdDateTime(now)
            .receiveExp(now+10 * 60 * 1000)
            .readExp(now+7L * 24 * 60 * 60 * 1000)
            .amount(10000L)
            .recipientCount(3)
            .distributorId(1234L)
            .roomId(ROOM_ID)
            .build();
    }

    @Test
    @DisplayName("token을 저장할 수 있다")
    @Transactional
    public void test(){
        /* given */

        /* when */
        DistributionToken savedDistributionToken = tokenRepository.save(distributionToken);

        /* then */

        assertNotNull(savedDistributionToken);
        assertEquals(distributionToken.getTokenKey(), savedDistributionToken.getTokenKey());
    }

    @Test
    @DisplayName("roomId와 tokenKey로 해당 토큰을 찾을 수 있다")
    @Transactional
    public void test2(){
        /* given */

        /* when */
        tokenRepository.save(distributionToken);
        Optional<DistributionToken> token = tokenRepository.findTokenByRoomIdAndTokenKey(ROOM_ID,
            TOKEN_KEY,createdTime);

        /* then */

        assertTrue(token.isPresent());
    }

}