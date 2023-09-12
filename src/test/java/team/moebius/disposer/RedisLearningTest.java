package team.moebius.disposer;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.repo.TokenRepository;

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

}
