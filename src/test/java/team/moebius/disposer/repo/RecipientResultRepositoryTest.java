package team.moebius.disposer.repo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import team.moebius.disposer.entity.RecipientResult;

@SpringBootTest
class RecipientResultRepositoryTest {

    @Autowired
    RecipientResultRepository recipientResultRepository;

    @Test
    @DisplayName("TokenId를 통해 미리 저장된 조회 Result 값을 가져 올 수 있다")
    public void test1(){
        /* given */

        long tokenId = 1L;

        /* when */

        Optional<RecipientResult> result =
            recipientResultRepository.findResultByTokeId(tokenId);

        /* then */

        assertNotNull(result.get().getResult());
    }

}