package team.moebius.disposer.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.entity.Token;

@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DataJpaTest
class TokenRepositoryTest {

    @Autowired
    TokenRepository tokenRepository;

    @Test
    @DisplayName("token을 저장할 수 있다")
    @Transactional
    public void test(){
        /* given */
        long now = ZonedDateTime.now().toInstant().toEpochMilli();

        Token token = Token.builder()
            .tokenKey("abc")
            .createdDateTime(now)
            .receiveExp(now+10 * 60 * 1000)
            .readExp(now+7L * 24 * 60 * 60 * 1000)
            .amount(10000L)
            .recipientCount(3)
            .distributorId(1234L)
            .roomId("ABC")
            .build();

        /* when */
        Token savedToken = tokenRepository.save(token);

        /* then */

        assertNotNull(savedToken);
        assertEquals(token.getTokenKey(), savedToken.getTokenKey());
    }

    @Test @DisplayName("roomId와 tokenKey로 해당 토큰을 찾을 수 있다")
    public void test2(){
        /* given */

        String roomId = "abe";
        String tokenKey = "XhK";

        /* when */

        Optional<Token> token = tokenRepository.findTokenByRoomIdAndTokenKey(roomId,
            tokenKey);

        /* then */

        assertTrue(token.isPresent());
    }

}