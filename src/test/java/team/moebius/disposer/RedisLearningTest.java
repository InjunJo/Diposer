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
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.repo.DistributionTokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@SpringBootTest
public class RedisLearningTest {

    @Autowired
    RedisTemplate<String, DistributionToken> redisTemplate;

    @Autowired
    DistributionTokenRepository distributionTokenRepository;

    @Test
    @Transactional
    public void test(){
        /* given */

        Optional<DistributionToken> optionalToken = distributionTokenRepository.findById(2L);

        DistributionToken distributionToken = optionalToken.get();

        SetOperations<String, DistributionToken> operations1 = redisTemplate.opsForSet();

        ZSetOperations<String, DistributionToken> operations = redisTemplate.opsForZSet();

        /* when */

        String key = String.join("-",
                distributionToken.getTokenKey(),
                distributionToken.getRoomId(),
                String.valueOf(distributionToken.getCreatedDateTime())
            );

        operations1.add(key, distributionToken);


//        /* then */
        System.out.println(operations1.members(key));
        System.out.println(operations1.members(key));
    }

    @Test
    @Transactional
    public void test2(){
        /* given */

        Optional<DistributionToken> optionalToken = distributionTokenRepository.findById(8L);
        Optional<DistributionToken> optionalToken2 = distributionTokenRepository.findById(10L);

        DistributionToken distributionToken = optionalToken.get();
        DistributionToken distributionToken2 = optionalToken2.get();

        ZSetOperations<String, DistributionToken> operations = redisTemplate.opsForZSet();

        /* when */

        String key = String.join("-",
            distributionToken.getTokenKey(),
            distributionToken.getRoomId(),
            String.valueOf(distributionToken.getCreatedDateTime())
        );

        String key2 = String.join("-",
            distributionToken2.getTokenKey(),
            distributionToken2.getRoomId(),
            String.valueOf(distributionToken2.getCreatedDateTime())
        );

        String zsetKey = "tokens";

        operations.add(zsetKey, distributionToken, distributionToken.getCreatedDateTime());
        operations.add(zsetKey, distributionToken2, distributionToken2.getCreatedDateTime());


//        /* then */

        Set<DistributionToken> distributionTokens = operations.rangeByScore(zsetKey, 0, DateTimeSupporter.getNowUnixTime()-10 * 60 * 1000);
        System.out.println(distributionTokens);
    }

}
