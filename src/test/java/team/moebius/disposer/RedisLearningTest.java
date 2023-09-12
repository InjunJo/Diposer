package team.moebius.disposer;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@SpringBootTest
public class RedisLearningTest {

    @Autowired
    RedisTemplate<String, Token> redisTemplate;

    @Autowired
    TokenRepository tokenRepository;

    @Test
    @Transactional
    public void test(){
        /* given */

        Optional<Token> optionalToken = tokenRepository.findById(2L);

        Token token = optionalToken.get();

        SetOperations<String, Token> operations1 = redisTemplate.opsForSet();

        ZSetOperations<String, Token> operations = redisTemplate.opsForZSet();

        /* when */

        String key = String.join("-",
                token.getTokenKey(),
                token.getRoomId(),
                String.valueOf(token.getCreatedDateTime())
            );

        operations1.add(key,token);


//        /* then */
        System.out.println(operations1.members(key));
        System.out.println(operations1.members(key));
    }

    @Test
    @Transactional
    public void test2(){
        /* given */

        Optional<Token> optionalToken = tokenRepository.findById(8L);
        Optional<Token> optionalToken2 = tokenRepository.findById(10L);

        Token token = optionalToken.get();
        Token token2 = optionalToken2.get();

        ZSetOperations<String, Token> operations = redisTemplate.opsForZSet();

        /* when */

        String key = String.join("-",
            token.getTokenKey(),
            token.getRoomId(),
            String.valueOf(token.getCreatedDateTime())
        );

        String key2 = String.join("-",
            token2.getTokenKey(),
            token2.getRoomId(),
            String.valueOf(token2.getCreatedDateTime())
        );

        String zsetKey = "tokens";

        operations.add(zsetKey,token,token.getCreatedDateTime());
        operations.add(zsetKey,token2,token2.getCreatedDateTime());


//        /* then */

        Set<Token> tokens = operations.rangeByScore(zsetKey, 0, DateTimeSupporter.getNowUnixTime()-10 * 60 * 1000);
        System.out.println(tokens);
    }

}
