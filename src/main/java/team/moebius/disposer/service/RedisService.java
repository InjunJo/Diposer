package team.moebius.disposer.service;

import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import team.moebius.disposer.entity.Token;

@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, Token> redisTemplate;

    private SetOperations<String, Token> operations;

    @PostConstruct
    public void setUp(){
        this.operations = redisTemplate.opsForSet();
    }

    public void saveTokenToRedis(Token token) {
        operations.add(createHashKey(token),token);
    }

    public Optional<Token> loadTokenFromRedis(String tokenKey,String roomId, String createdDateTime){
        return Optional.of(
            operations.randomMember(createHashKey(tokenKey, roomId, createdDateTime))
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
