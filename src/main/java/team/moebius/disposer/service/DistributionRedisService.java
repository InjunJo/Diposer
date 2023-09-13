package team.moebius.disposer.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import team.moebius.disposer.dto.DistributionTokenDto;

/**
 *  빈번하게 조회될 뿌리기 관련 Token을 memory(redis)에 저장하는 클래스
 */
@RequiredArgsConstructor
@Service
public class DistributionRedisService {

    private final RedisTemplate<String, DistributionTokenDto> redisTemplate;

    private SetOperations<String, DistributionTokenDto> opsForSet;

    private ZSetOperations<String, DistributionTokenDto> opsForZSet;

    @Value("${token.receive_exp}")
    private long TOKEN_RECEIVE_EXP;
    private final String ZSET_KEY = "token_timestamps";

    @PostConstruct
    public void setUp() {
        this.opsForSet = redisTemplate.opsForSet();
        this.opsForZSet = redisTemplate.opsForZSet();
    }

    public void saveTokenToRedis(DistributionTokenDto distributionTokenDto) {
        String hashKey = createHashKey(distributionTokenDto);

        opsForSet.add(hashKey, distributionTokenDto);
        saveZsetToken(distributionTokenDto);

        // 일정 시간이 지나면 Memory에서 Token 데이터를 삭제 한다.
        redisTemplate.expire(hashKey, 10, TimeUnit.MINUTES);
    }

    // 받기 요청 만료된 Token을 효율적으로 찾기 위해 토큰 생성된 시점을 기준으로 Redis Zset에 추가 한다.
    private void saveZsetToken(DistributionTokenDto distributionTokenDto) {
        ZSetOperations<String, DistributionTokenDto> opsForZSet = redisTemplate.opsForZSet();
        opsForZSet.add(ZSET_KEY, distributionTokenDto, distributionTokenDto.getCreatedDateTime());
    }

    // 받기 요청이 만료된 Token을 Redis에서 찾아 메모리에서 제거 후 반환 한다.
    Set<DistributionTokenDto> findExpiredReceiveToken(long targetUnixTime) {

        // 원하는 시간 범위에 해당하는 Token in Redis를 찾는다.
        Set<DistributionTokenDto> distributionTokens = opsForZSet.rangeByScore(
            ZSET_KEY,
            0,
            targetUnixTime
        );

        opsForZSet.removeRangeByScore(
            ZSET_KEY,
            0,
            targetUnixTime + TOKEN_RECEIVE_EXP
        );

        return distributionTokens;
    }

    // 사용자 요청 데이터를 Redis용 HashKey로 변환해 memory에 Token이 있는지 찾는다. 검색 실패에 의해 Null 가능성을 포함한다.
    public Optional<DistributionTokenDto> loadTokenFromRedis(String tokenKey, String roomId,
        String createdDateTime) {
        return Optional.ofNullable(
            opsForSet.randomMember(createHashKey(tokenKey, roomId, createdDateTime)
            )
        );
    }

    // Redis에서 해당 Token을 찾기 위해 고유 HashKey을 만든다.
    private String createHashKey(String tokenKey, String roomId, String createdDateTime) {
        return String.join("-",
            tokenKey,
            roomId,
            createdDateTime
        );
    }

    private String createHashKey(DistributionTokenDto distributionTokenDto) {
        return createHashKey(
            distributionTokenDto.getTokenKey(),
            distributionTokenDto.getRoomId(),
            String.valueOf(distributionTokenDto.getCreatedDateTime()));
    }


}
