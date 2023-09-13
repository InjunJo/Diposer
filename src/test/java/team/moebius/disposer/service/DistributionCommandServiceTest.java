package team.moebius.disposer.service;

import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.repo.DistributionTokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;
import team.moebius.disposer.util.TokenFactory;

@ExtendWith(MockitoExtension.class)
class DistributionCommandServiceTest {

    @InjectMocks
    DistributionCommandService distributionCommandService;

    @Mock
    DistributionTokenRepository distributionTokenRepository;

    @Mock
    DistributionRedisService distributionRedisService;

    DistributionToken distributionToken;

    @Mock
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

    @BeforeEach
    public void setUp() {
        reqDistribution = new ReqDistribution(10000L, 3);
        nowDataTime = DateTimeSupporter.getNowUnixTime();

        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;
        createTime = String.valueOf(now);

        this.distributionToken = buildToken(now);
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
            .id(234L)
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


    @Test @DisplayName("전달 받은 Token을 저장하고 저장된 Token의 Dto를 반환할 수 있다")
    public void test(){
        /* given */

        /* when */

        when(tokenFactory.buildToken(nowDataTime,distributorUserId,roomId,amount,recipientCount)).thenReturn(distributionToken);
        when(distributionTokenRepository.save(distributionToken)).thenReturn(distributionToken);
        DistributionTokenDto distributionTokenDto = distributionCommandService.distribute(distributorUserId,
            roomId, amount, recipientCount, nowDataTime);
        /* then */

        Assertions.assertNotNull(distributionTokenDto);
    }



}