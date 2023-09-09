package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@ExtendWith(MockitoExtension.class)
class TokenGeneratorTest {

    @InjectMocks
    TokenGenerator tokenGenerator;

    @Mock
    TokenRepository tokenRepository;

    @Mock
    RecipientRepository recipientRepository;

    @Mock
    Token token;

    long nowDataTime;

    ReqDistribution reqDistribution;

    @BeforeEach
    public void setUp() {
        reqDistribution = new ReqDistribution(10000L, 3);
        nowDataTime = DateTimeSupporter.getNowUnixTime();
    }


    @Test
    @DisplayName("getReceiveExpTime을 통해 token receive 유효기간을 반환 받을 수 있다")
    public void test() {
        /* given */
        long now = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        long expect = 10 * 60 * 1000;

        /* when */
        long receiveExpTime = tokenGenerator.getReceiveExpTime(now);

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
        long receiveExpTime = tokenGenerator.getReadExpTime(now);

        /* then */
        assertEquals(expect, receiveExpTime - now);
    }

    @Test
    @DisplayName("생성된 token key는 3자리 문자열이다")
    public void test3() {
        /* given */

        /* when */
        String tokenKey = tokenGenerator.generateTokenKey();

        /* then */
        assertEquals(3, tokenKey.length());
    }

    @Test
    @DisplayName("생성된 token key는 중복되지 않는다.")
    public void test4() {
        /* given */

        /* when */
        Set<String> stringSet = Stream.generate(() -> tokenGenerator.generateTokenKey())
            .limit(100)
            .collect(Collectors.toSet());

        /* then */

        assertEquals(100, stringSet.size());
    }

    @Test
    @DisplayName("token을 생성하여 임의의 3자리 tokenKey를 반환할 수 있다.")
    public void test5() {
        /* given */

        ReqDistribution reqDistribution = new ReqDistribution(10000L, 3);

        /* when */
        when(tokenRepository.save(any())).thenReturn(token);
        when(recipientRepository.saveAll(any())).thenReturn(any());

        String tokenKey = tokenGenerator.generateToken(
            234254L,
            "Rbdc",
            reqDistribution.getAmount(),
            reqDistribution.getRecipientCount(),
            nowDataTime
        );

        /* then */
        assertEquals(3, tokenKey.length());
    }

    @Test
    public void test6() {
        /* given */
        long now = ZonedDateTime.now().toInstant().toEpochMilli();

        /* when */
        Token buildToken = tokenGenerator.buildToken(
            now,
            1234L,
            "ABC",
            reqDistribution.getAmount(),
            reqDistribution.getRecipientCount()
        );

        /* then */

    }

}