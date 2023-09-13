package team.moebius.disposer.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.service.DistributionCommandService;

@ExtendWith(MockitoExtension.class)
class TokenFactoryTest {

    TokenFactory tokenFactory;

    long nowDataTime;

    String createTime;

    ReqDistribution reqDistribution;

    long distributorUserId = 1234567L;


    String roomId = "ABC";

    long targetTime;

    long amount = 30000L;

    int recipientCount = 3;

    String tokenKey = "abc";

    DistributionToken distributionToken;

    @BeforeEach
    public void setUp() {
        reqDistribution = new ReqDistribution(10000L, 3);
        nowDataTime = DateTimeSupporter.getNowUnixTime();

        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;
        createTime = String.valueOf(now);

        distributionToken = buildToken(now);

        tokenFactory = configTokenFactory();
    }

        private TokenFactory configTokenFactory() {

        String keySource = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        long receiveExp = 10 * 60 * 1000;
        long readExp = 7 * 24 * 60 * 60 * 1000;
        int keyLength = 3;

        return new TokenFactory(keySource, receiveExp, readExp, keyLength);
    }

    private DistributionToken buildToken(long now) {
        return DistributionToken.builder()
            .tokenKey(tokenKey)
            .createdDateTime(now)
            .receiveExp(now + 10 * 60 * 1000)
            .readExp(now + 7L * 24 * 60 * 60 * 1000)
            .amount(amount)
            .recipientCount(3)
            .distributorId(distributorUserId)
            .roomId(roomId)
            .build();
    }

    @Test
    @DisplayName("getReceiveExpTime을 통해 token receive 유효기간을 반환 받을 수 있다")
    public void test() {
        /* given */
        long now = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        long expect = 10 * 60 * 1000;

        /* when */
        long receiveExpTime = tokenFactory.getReceiveExpTime(now);


        /* then */
        assertEquals(expect, receiveExpTime - now);
    }

    @Test
    @DisplayName("getReadExpTime을 통해 token read 유효기간을 반환 받을 수 있다")
    public void test2() {
        /* given */
        long now = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        long expect = 7L * 24 * 60 * 60 * 1000;

        /* when */
        long receiveExpTime = tokenFactory.getReadExpTime(now);

        /* then */
        assertEquals(expect, receiveExpTime - now);
    }

    @Test
    @DisplayName("생성된 token key는 3자리 문자열이다")
    public void test3() {
        /* given */

        /* when */
        String tokenKey = tokenFactory.generateTokenKey();

        /* then */
        assertEquals(3, tokenKey.length());
    }


    @Test
    @DisplayName("token을 생성하여 임의의 3자리 tokenKey를 반환할 수 있다.")
    public void test5() {
        /* given */

        /* when */

        DistributionToken token = tokenFactory.buildToken(nowDataTime,
            distributorUserId, roomId, amount, recipientCount);

        /* then */
        assertEquals(3, token.getTokenKey().length());
    }



}