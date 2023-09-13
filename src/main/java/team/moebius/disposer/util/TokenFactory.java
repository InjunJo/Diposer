package team.moebius.disposer.util;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.moebius.disposer.entity.DistributionToken;

@Component
public class TokenFactory {
    private final String TOKEN_KEY_RESOURCE;

    private final long TOKEN_RECEIVE_EXP;

    private final long TOKEN_READ_EXP;

    private final int TOKEN_KEY_LENGTH;

    public TokenFactory(
        @Value("${token.key_resource}") String TOKEN_KEY_RESOURCE,
        @Value("${token.receive_exp}") long TOKEN_RECEIVE_EXP,
        @Value("${token.read_exp}") long TOKEN_READ_EXP,
        @Value("${token.key_lenth}") int TOKEN_KEY_LENGTH)
    {
        this.TOKEN_KEY_RESOURCE = TOKEN_KEY_RESOURCE;
        this.TOKEN_RECEIVE_EXP = TOKEN_RECEIVE_EXP;
        this.TOKEN_READ_EXP = TOKEN_READ_EXP;
        this.TOKEN_KEY_LENGTH = TOKEN_KEY_LENGTH;
    }

    // 뿌리기 요청을 받아 각각 처리된 Token 데이터를 저장 한다.
    public DistributionToken buildToken(long now, Long userId, String roomId, Long amount,
        int recipientCount) {

        return DistributionToken.builder()
            .tokenKey(generateTokenKey())
            .createdDateTime(now)
            .receiveExp(getReceiveExpTime(now))
            .readExp(getReadExpTime(now))
            .amount(amount)
            .recipientCount(recipientCount)
            .distributorId(userId)
            .roomId(roomId)
            .build();
    }

    // Stream generate를 통해 임의의 3자리 문자열 token key를 생성 한다.
    String generateTokenKey() {
        Random random = new Random();

        return Stream.generate(
                () -> TOKEN_KEY_RESOURCE.charAt(random.nextInt(TOKEN_KEY_RESOURCE.length()))
            )
            .limit(TOKEN_KEY_LENGTH)
            .map(String::valueOf)
            .collect(Collectors.joining());
    }

    // unix time 형식으로 뿌리기 건에 대한 받기 요청 10분 유효 시간을 계산해서 반환 한다.
    long getReceiveExpTime(long epochMilli) {
        return epochMilli + TOKEN_RECEIVE_EXP;
    }

    // unix time 형식으로 뿌리기 건에 대한 조회 요청 7일 유효 시간을 계산해서 반환 한다.
    long getReadExpTime(long epochMilli) {
        return epochMilli + TOKEN_READ_EXP;
    }
}
