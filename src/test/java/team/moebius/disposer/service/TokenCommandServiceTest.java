package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.RecipientException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@ExtendWith(MockitoExtension.class)
class TokenCommandServiceTest {

    @InjectMocks
    TokenCommandService tokenCommandService;

    @Mock
    TokenRepository tokenRepository;

    @Mock
    RecipientRepository recipientRepository;

    Token token;

    long nowDataTime;

    ReqDistribution reqDistribution;

    long distributorUserId = 1234567L;

    long anotherUserId = 11111L;

    String roomId = "ABC";

    long targetTime;

    String tokenKey = "abc";

    @BeforeEach
    public void setUp() {
        reqDistribution = new ReqDistribution(10000L, 3);
        nowDataTime = DateTimeSupporter.getNowUnixTime();

        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;

        token = buildToken(now);
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


    @Test
    @DisplayName("getReceiveExpTime을 통해 token receive 유효기간을 반환 받을 수 있다")
    public void test() {
        /* given */
        long now = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        long expect = 10 * 60 * 1000;

        /* when */
        long receiveExpTime = tokenCommandService.getReceiveExpTime(now);

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
        long receiveExpTime = tokenCommandService.getReadExpTime(now);

        /* then */
        assertEquals(expect, receiveExpTime - now);
    }

    @Test
    @DisplayName("생성된 token key는 3자리 문자열이다")
    public void test3() {
        /* given */

        /* when */
        String tokenKey = tokenCommandService.generateTokenKey();

        /* then */
        assertEquals(3, tokenKey.length());
    }

    @Test
    @DisplayName("생성된 token key는 중복되지 않는다.")
    public void test4() {
        /* given */

        /* when */
        Set<String> stringSet = Stream.generate(() -> tokenCommandService.generateTokenKey())
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

        String tokenKey = tokenCommandService.generateToken(
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
        Token buildToken = tokenCommandService.buildToken(
            now,
            1234L,
            "ABC",
            reqDistribution.getAmount(),
            reqDistribution.getRecipientCount()
        );

        /* then */

    }

    @Test
    @DisplayName("자신이 뿌리한 건을 자신이 받으려고 할 때 예외가 던져진다")
    public void test7() {
        /* given */

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey)).thenReturn(
            Optional.ofNullable(token));

        Executable e = () -> tokenCommandService.provideShare(distributorUserId, roomId, tokenKey, targetTime);

        /* then */

        assertThrows(TokenException.class, e);
    }

    @Test
    @DisplayName("roomId과 일치하는 token이 아닐 때 예외가 던져진다.")
    public void test8() {
        /* given */

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.empty());

        Executable e = () -> tokenCommandService.provideShare(anotherUserId, roomId, tokenKey,
            targetTime);

        /* then */

        assertThrows(NotFoundTokenException.class, e);
    }

    @Test
    @DisplayName("tokenKey에 해당하는 token을 찾지 못하면 예외가 던져진다.")
    public void test9() {
        /* given */

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.empty());

        Executable e = () -> tokenCommandService.provideShare(anotherUserId, roomId, tokenKey,
            targetTime);

        /* then */

        assertThrows(NotFoundTokenException.class, e);
    }

    @Test
    @DisplayName("요구 조건에 맞는 사용자가 받기 요청을 할 때, 분배된 금액을 반환할 수 있다")
    public void test10() {
        /* given */

        List<Recipient> recipientList = List.of(
            new Recipient(token, 3000L),
            new Recipient(token, 3000L),
            new Recipient(token, 3000L)
        );

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));
        when(recipientRepository.findAllByTokenId(token.getId())).thenReturn(recipientList);

        long amount = tokenCommandService.provideShare(anotherUserId, roomId, tokenKey, targetTime);

        /* then */
        assertEquals(3000L, amount);
    }

    @Test
    @DisplayName("유효 시간이 지난 token에 대한 받기 요청은 예외를 던질 수 있다")
    public void test11() {
        /* given */

        String tokenKey = "ByC";
        Long overTime = targetTime + 1000000L;

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));

        Executable e = () -> tokenCommandService.provideShare(anotherUserId, roomId, tokenKey, overTime);

        /* then */
        assertThrows(TokenException.class, e);
    }

    @Test
    @DisplayName("이미 뿌리기 건에 대해 받아간 유저가 재차 시도시 예외를 던질 수 있다.")
    public void test12() {
        /* given */

        String tokenKey = "ByC";

        List<Recipient> recipientList = List.of(
            new Recipient(token, 3000L, anotherUserId),
            new Recipient(token, 3000L),
            new Recipient(token, 3000L)
        );

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));
        when(recipientRepository.findAllByTokenId(token.getId())).thenReturn(recipientList);

        Executable e = () -> tokenCommandService.provideShare(anotherUserId, roomId, tokenKey, targetTime);

        /* then */
        assertThrows(RecipientException.class, e);
    }

    @Test
    @DisplayName("이미 모두 받아간 뿌리기에 대해 요청하면 예외를 던질 수 있다")
    public void test13() {
        /* given */

        long userId1 = 1234567L;
        long userId2 = 1234568L;
        long userId3 = 1234569L;
        long newUserId = 1234570L;

        String tokenKey = "ByC";

        List<Recipient> recipientList = List.of(
            new Recipient(token, 3000L, userId1),
            new Recipient(token, 3000L, userId2),
            new Recipient(token, 3000L, userId3)
        );

        /* when */
        when(tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey))
            .thenReturn(Optional.ofNullable(token));
        when(recipientRepository.findAllByTokenId(token.getId())).thenReturn(recipientList);

        Executable e = () -> tokenCommandService.provideShare(newUserId, roomId, tokenKey, targetTime);

        /* then */
        assertThrows(RecipientException.class, e);
    }

}