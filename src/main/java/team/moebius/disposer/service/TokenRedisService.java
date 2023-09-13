package team.moebius.disposer.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.util.DateTimeSupporter;

@RequiredArgsConstructor
@Service
public class TokenRedisService {

    private final RedisTemplate<String, Token> redisTemplate;

    private SetOperations<String, Token> opsForSet;

    private ZSetOperations<String, Token> opsForZSet;

    private final String ZSET_KEY = "token_timestamps";

    @PostConstruct
    public void setUp(){
        this.opsForSet = redisTemplate.opsForSet();
        this.opsForZSet = redisTemplate.opsForZSet();
    }

    public void saveTokenToRedis(Token token) {
        String hashKey = createHashKey(token);
        opsForSet.add(hashKey,token);
        saveZsetToken(token);
        redisTemplate.expire(hashKey,10, TimeUnit.MINUTES);
    }

    private void saveZsetToken(Token token){
        ZSetOperations<String, Token> opsForZSet = redisTemplate.opsForZSet();
        opsForZSet.add(ZSET_KEY,token,token.getCreatedDateTime());
    }

    Set<Token> findExpiredReceiveToken(){

        Set<Token> tokens = opsForZSet.rangeByScore(ZSET_KEY, 0,
            DateTimeSupporter.getNowUnixTime());

        opsForZSet.removeRangeByScore(
            ZSET_KEY,
            0,
            DateTimeSupporter.getNowUnixTime()+TokenCommandService.getReceiveExp()
        );

        return tokens;
    }

    public Optional<Token> loadTokenFromRedis(String tokenKey,String roomId, String createdDateTime){
        return Optional.ofNullable(
            opsForSet.randomMember(createHashKey(tokenKey, roomId, createdDateTime))
        );
    }

    private String createHashKey(Token token) {
        return String.join("-",
            token.getTokenKey(),
            token.getRoomId(),
            String.valueOf(token.getCreatedDateTime())
        );
    }

    private String createHashKey(String tokenKey,String roomId, String createdDateTime) {
        return String.join("-",
            tokenKey,
            roomId,
            createdDateTime
        );
    }

}
