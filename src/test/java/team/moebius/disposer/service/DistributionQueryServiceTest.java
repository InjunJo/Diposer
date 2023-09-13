package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import team.moebius.disposer.domain.DistributionInfo;
import team.moebius.disposer.domain.ReceiveInfo;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.repo.DistributionTokenRepository;
import team.moebius.disposer.repo.RecipientRepository;

@ExtendWith(MockitoExtension.class)
class DistributionQueryServiceTest {

    @InjectMocks
    DistributionQueryService distributionQueryService;

    @Mock
    DistributionTokenRepository distributionTokenRepository;

    @Mock
    RecipientRepository recipientRepository;

    @Mock
    DistributionRedisService distributionRedisService;

    DistributionToken distributionToken;

    long distributorUserId = 1234567L;

    long anotherUserId = 11111L;

    String roomId = "ABC";

    long targetTime;

    String createTime;

    String tokenKey = "abc";

    DistributionInfo distributionInfo;

    @BeforeEach
    public void setUp() {
        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;
        createTime = String.valueOf(now);

        distributionToken = buildToken(now);
        distributionInfo = buildTokenInfo();
    }

    private DistributionToken buildToken(long now) {
        return DistributionToken.builder()
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
    private DistributionInfo buildTokenInfo() {

        List<ReceiveInfo> receiveInfoList = List.of(
            new ReceiveInfo(30000L, 234L),
            new ReceiveInfo(30000L, 222L)
        );

        return DistributionInfo.builder()
            .distributeTime(LocalDateTime.now().toString())
            .distributeAmount(90000L)
            .receiveTotalAmount(60000L)
            .receiveInfoList(receiveInfoList)
            .build();
    }

    @Test @DisplayName("DB에서 roomId,tokenKey,createTime과 일치하는 Token을 찾을 수 있다")
    public void test(){
        /* given */

        /* when */

        when(distributionRedisService.loadTokenFromRedis(any(),any(),any())).thenReturn(
            Optional.empty());
        when(distributionTokenRepository.findTokenByRelatedData(roomId, tokenKey,
            Long.parseLong(createTime))).thenReturn(Optional.of(distributionToken));

        DistributionTokenDto token = distributionQueryService.getDistributionToken(
            roomId, tokenKey, createTime);

        System.out.println(token);

        /* then */
    }

    @Test @DisplayName("Redis에서 roomId,tokenKey,createTime과 일치하는 Token을 찾을 수 있다")
    public void test1(){
        /* given */

        /* when */

        when(distributionRedisService.loadTokenFromRedis(tokenKey,roomId,createTime)).thenReturn(
            Optional.of(new DistributionTokenDto(distributionToken)));

        DistributionTokenDto token = distributionQueryService.getDistributionToken(
            roomId, tokenKey, createTime);

        /* then */
        assertEquals(token.getId(),distributionToken.getId());
    }


    @Test
    @DisplayName("roomId과 일치하는 token이 아닐 때 예외가 던져진다.")
    public void test8() {
        /* given */

        String incorrectRoomId = "vcxvd";

        /* when */

        when(distributionRedisService.loadTokenFromRedis(tokenKey,incorrectRoomId,createTime)).thenReturn(
            Optional.empty());
        when(distributionTokenRepository.findTokenByRelatedData(incorrectRoomId, tokenKey,
            Long.parseLong(createTime))).thenReturn(Optional.empty());

        Executable e = () -> distributionQueryService.getDistributionToken(incorrectRoomId,tokenKey,createTime);

        /* then */
        assertThrows(NotFoundTokenException.class, e);
    }

    @Test
    @DisplayName("tokenKey에 해당하는 token을 찾지 못하면 예외가 던져진다.")
    public void test9() {
        /* given */

        String incorrectTokenKey = "czczc";

        /* when */
        when(distributionRedisService.loadTokenFromRedis(incorrectTokenKey,roomId,createTime)).thenReturn(
            Optional.empty());
        when(distributionTokenRepository.findTokenByRelatedData(roomId, incorrectTokenKey,
            Long.parseLong(createTime))).thenReturn(Optional.empty());

        Executable e = () -> distributionQueryService.getDistributionToken(roomId,incorrectTokenKey,createTime);

        /* then */

        assertThrows(NotFoundTokenException.class, e);
    }



}