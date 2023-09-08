package team.moebius.disposer.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import team.moebius.disposer.entity.Recipient;

@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DataJpaTest
class RecipientRepositoryTest {

    @Autowired
    RecipientRepository recipientRepository;

    @Test //// TODO: 2023-09-08 2023-09-8, 금, 16:16 좀 더 명확한 DisplayName으로 고치기
    @DisplayName("tokenId로 뿌려진 금액에 대해 할당 받을 수 있는 임의의 자리 List를 반환 할 수 있다")
    public void test(){
        /* given */
        Long tokenId = 4L;
        
        /* when */
        List<Recipient> recipients = recipientRepository.findReceivableAllByToken(tokenId);

        /* then */

        assertFalse(recipients.isEmpty());
    }

}