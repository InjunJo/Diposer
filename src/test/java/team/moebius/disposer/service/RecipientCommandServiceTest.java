package team.moebius.disposer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.exception.RecipientException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@ExtendWith(MockitoExtension.class)
class RecipientCommandServiceTest {

    @InjectMocks
    RecipientCommandService recipientCommandService;

    long distributorUserId = 1234567L;

    String tokenKey = "abc";

    String roomId = "ABC";

    DistributionTokenDto distributionTokenDto;

    ReqDistribution reqDistribution;

    long nowDataTime;

    long targetTime;

    String createTime;

    @Mock
    RecipientRepository recipientRepository;

    long anotherUserId = 11111L;

    @BeforeEach
    public void setUp() {
        reqDistribution = new ReqDistribution(10000L, 3);
        nowDataTime = DateTimeSupporter.getNowUnixTime();

        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        targetTime = now;
        createTime = String.valueOf(now);

        distributionTokenDto = new DistributionTokenDto(buildToken(now));
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

    @Test
    @DisplayName("자신이 뿌리한 건을 자신이 받으려고 할 때 예외가 던져진다")
    public void test7() {
        /* given */

        /* when */
        Executable e = () -> recipientCommandService.provideShare(distributorUserId,
            distributionTokenDto,targetTime);

        /* then */

        assertThrows(TokenException.class, e);
    }

    @Test
    @DisplayName("요구 조건에 맞는 사용자가 받기 요청을 할 때, 분배된 금액을 반환할 수 있다")
    public void test10() {
        /* given */

        List<Recipient> recipientList = List.of(
            new Recipient(distributionTokenDto, 3000L),
            new Recipient(distributionTokenDto, 3000L),
            new Recipient(distributionTokenDto, 3000L)
        );

        /* when */
        when(recipientRepository.findAllByTokenId(distributionTokenDto.getId())).thenReturn(recipientList);

        long amount = recipientCommandService.provideShare(anotherUserId, distributionTokenDto,targetTime);

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

        Executable e = () -> recipientCommandService.provideShare(anotherUserId,
            distributionTokenDto,overTime);

        /* then */
        assertThrows(TokenException.class, e);
    }

    @Test
    @DisplayName("이미 뿌리기 건에 대해 받아간 유저가 재차 시도시 예외를 던질 수 있다.")
    public void test12() {
        /* given */

        String tokenKey = "ByC";

        List<Recipient> recipientList = List.of(
            new Recipient(distributionTokenDto, 3000L, anotherUserId),
            new Recipient(distributionTokenDto, 3000L),
            new Recipient(distributionTokenDto, 3000L)
        );

        /* when */
        when(recipientRepository.findAllByTokenId(distributionTokenDto.getId())).thenReturn(recipientList);

        Executable e = () -> recipientCommandService.provideShare(anotherUserId,
            distributionTokenDto,targetTime);

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
            new Recipient(distributionTokenDto, 3000L, userId1),
            new Recipient(distributionTokenDto, 3000L, userId2),
            new Recipient(distributionTokenDto, 3000L, userId3)
        );

        /* when */
        when(recipientRepository.findAllByTokenId(distributionTokenDto.getId())).thenReturn(recipientList);

        Executable e = () -> recipientCommandService.provideShare(newUserId, distributionTokenDto,targetTime);

        /* then */
        assertThrows(RecipientException.class, e);
    }

//    @Test @DisplayName("Json형태의 조회 API 데이터를 생성 할 수 있다")
//    public void test14(){
//        /* given */
//
//        Set<DistributionToken> expiredReceiveDistributionTokens = Set.of(distributionToken);
//
//        /* when */
//
//        when(distributionRedisService.findExpiredReceiveToken()).thenReturn(
//            expiredReceiveDistributionTokens);
//        recipientCommandService.savePreComputedResult();
//
//        /* then */
//    }

}