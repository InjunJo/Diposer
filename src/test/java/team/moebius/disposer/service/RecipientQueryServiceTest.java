package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
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
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;

@ExtendWith(MockitoExtension.class)
class RecipientQueryServiceTest {

    @InjectMocks
    RecipientQueryService recipientQueryService;

    @Mock
    RecipientRepository recipientRepository;

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

    @Test
    @DisplayName("뿌린 사람 자신은 뿌린 건에 대해 조회할 수 있다")
    public void test1() {
        /* given */

        List<Recipient> recipients = List.of(
            new Recipient(new DistributionTokenDto(distributionToken),10000L,113L),
            new Recipient(new DistributionTokenDto(distributionToken)
                ,10000L,114L)
        );

        /* when */
        when(recipientRepository.findAllocatedRecipientsByTokenId(distributionToken.getId())).thenReturn(recipients);

        DistributionInfo distributionInfo =
            recipientQueryService.getDistributionInfo(new DistributionTokenDto(distributionToken),targetTime);

        /* then */
        assertNotNull(distributionInfo);
    }

    @Test @DisplayName("뿌린 사람이 아닌 다른 사람이 뿌린 건에 대해 조회 하려고 할 때 예외를 반환할 수 있다.")
    public void test2() {
        /* given */

        /* when */

        Executable e = () -> recipientQueryService.checkValidRequest(anotherUserId,new DistributionTokenDto(distributionToken),targetTime);

        /* then */
        assertThrows(TokenException.class, e);
    }

    @Test @DisplayName("조회 유효 기간이 만료되면 예외를 던질 수 있다.")
    public void test3() {
        /* given */

        DistributionToken distributionToken = buildToken(targetTime-7L * 24 * 60 * 60 * 1000);

        /* when */

        Executable e = () -> recipientQueryService.checkValidRequest(distributorUserId,new DistributionTokenDto(distributionToken),targetTime);

        /* then */
        assertThrows(TokenException.class, e);
    }

}