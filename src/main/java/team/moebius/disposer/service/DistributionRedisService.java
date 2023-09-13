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
        redisTemplate.expire(hashKey, 10, TimeUnit.MINUTES);
    }

    private void saveZsetToken(DistributionTokenDto distributionTokenDto) {
        ZSetOperations<String, DistributionTokenDto> opsForZSet = redisTemplate.opsForZSet();
        opsForZSet.add(ZSET_KEY, distributionTokenDto, distributionTokenDto.getCreatedDateTime());
    }

    Set<DistributionTokenDto> findExpiredReceiveToken(long targetUnixTime) {

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

    public Optional<DistributionTokenDto> loadTokenFromRedis(String tokenKey, String roomId,
        String createdDateTime) {
        return Optional.ofNullable(
            opsForSet.randomMember(createHashKey(tokenKey, roomId, createdDateTime)
            )
        );
    }

    private String createHashKey(DistributionTokenDto distributionTokenDto) {
        return String.join("-",
            distributionTokenDto.getTokenKey(),
            distributionTokenDto.getRoomId(),
            String.valueOf(distributionTokenDto.getCreatedDateTime())
        );
    }

    private String createHashKey(String tokenKey, String roomId, String createdDateTime) {
        return String.join("-",
            tokenKey,
            roomId,
            createdDateTime
        );
    }

}
