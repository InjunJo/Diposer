package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.moebius.disposer.domain.ReceiveInfo;
import team.moebius.disposer.domain.TokenInfo;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.TokenRepository;

@ExtendWith(MockitoExtension.class)
class TokenQueryServiceTest {

    @InjectMocks
    TokenQueryService tokenQueryService;

    @Mock
    TokenRepository tokenRepository;

    @Mock
    RecipientRepository recipientRepository;

    @Mock
    RedisService redisService;

    Token token;

    long distributorUserId = 1234567L;

    long anotherUserId = 11111L;

    String roomId = "ABC";

    long targetTime;

    String createTime;

    String tokenKey = "abc";

    TokenInfo tokenInfo;

    @BeforeEach
    public void setUp() {
        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;
        createTime = String.valueOf(now);

        token = buildToken(now);
        tokenInfo = buildTokenInfo();
    }

    private Token buildToken(long now) {
        return Token.builder()
            .tokenKey(tokenKey)
            .createdDateTime(now)
            .receiveExp(now + 10 * 60 * 1000)
            .readExp(now + 7L * 24 * 60 * 60 * 1000)
            .amount(10000L)
            .recipientCount(3)
            .distributorId(distributorUserId)
            .roomId(roomId)
            .build();
    }
    private TokenInfo buildTokenInfo() {

        List<ReceiveInfo> receiveInfoList = List.of(
            new ReceiveInfo(30000L, 234L),
            new ReceiveInfo(30000L, 222L)
        );

        return TokenInfo.builder()
            .distributeTime(LocalDateTime.now())
            .distributeAmount(90000L)
            .receiveTotalAmount(60000L)
            .receiveInfoList(receiveInfoList)
            .build();
    }


    @Test @DisplayName("뿌린 사람 자신은 뿌린 건에 대해 조회할 수 있다")
    public void test1() {
        /* given */

        List<Recipient> recipients = List.of(
            new Recipient(token,10000L,113L),
            new Recipient(token,10000L,114L)
        );

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));
        when(recipientRepository.findReceiveAllByToken(token.getId())).thenReturn(recipients);

        TokenInfo tokenInfo = tokenQueryService.provideInfo(distributorUserId, roomId, tokenKey,createTime,targetTime);

        /* then */
        assertNotNull(tokenInfo);
    }

    @Test @DisplayName("뿌린 사람이 아닌 사람이 뿌린 건에 대해 조회하려고 할 때 예외를 반환할 수 있다.")
    public void test2() {
        /* given */

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));

        Executable e = () -> tokenQueryService.provideInfo(anotherUserId, roomId, tokenKey,createTime, targetTime);

        /* then */
        assertThrows(TokenException.class, e);
    }

    @Test @DisplayName("조회 유효 기간이 만료되면 예외를 던질 수 있다.")
    public void test3() {
        /* given */

        Token token = buildToken(targetTime-7L * 24 * 60 * 60 * 1000);

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));

        Executable e = () -> tokenQueryService.provideInfo(distributorUserId, roomId, tokenKey,createTime, targetTime);

        /* then */
        assertThrows(TokenException.class, e);
    }



}