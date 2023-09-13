package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@ExtendWith(MockitoExtension.class)
class DistributionCommandServiceTest {

    @InjectMocks
    DistributionCommandService distributionCommandService;

    @Mock
    TokenRepository tokenRepository;

    @Mock
    DistributionRedisService distributionRedisService;

    DistributionToken distributionToken;

    long nowDataTime;

    String createTime;

    ReqDistribution reqDistribution;

    long distributorUserId = 1234567L;


    String roomId = "ABC";

    long targetTime;

    long amount = 30000L;

    int recipientCount = 3;

    String tokenKey = "abc";

    @BeforeEach
    public void setUp() {
        reqDistribution = new ReqDistribution(10000L, 3);
        nowDataTime = DateTimeSupporter.getNowUnixTime();

        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;
        createTime = String.valueOf(now);

        distributionToken = buildToken(now);
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
    @DisplayName("token을 생성하여 임의의 3자리 tokenKey를 반환할 수 있다.")
    public void test() {
        /* given */

        ReqDistribution reqDistribution = new ReqDistribution(10000L, 3);

        /* when */
        when(tokenRepository.save(any())).thenReturn(this.distributionToken);

        DistributionTokenDto distributionToken = distributionCommandService.distribute(
            234254L,
            "Rbdc",
            reqDistribution.getAmount(),
            reqDistribution.getRecipientCount(),
            nowDataTime
        );

        /* then */
        assertEquals(3, distributionToken.getTokenKey().length());
    }



}