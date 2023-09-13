package team.moebius.disposer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.repo.DistributionTokenRepository;
import team.moebius.disposer.util.TokenFactory;

/**
 *  뿌리기 요청와 관련된 데이터를 저장하기 위한 Write 클래스
 */
@Service
@RequiredArgsConstructor
public class DistributionCommandService {
    private final DistributionTokenRepository distributionTokenRepository;
    private final DistributionRedisService distributionRedisService;
    private final TokenFactory tokenFactory;

    // 뿌리기 요청에 대해 전달 받은 Token을 저장 한다.
    @Transactional
    public DistributionTokenDto distribute(Long userId, String roomId, Long amount,
        int recipientCount, long nowDataTime) {

        // 사용자의 요청 데이터에 따라 Token을 만든다.
        DistributionToken distributionToken =
            tokenFactory.buildToken(nowDataTime,userId,roomId,amount,recipientCount);

        // DB에 먼저 저장하여 Generated된 Id를 받아 다시 Redis에 저장 한다.
        DistributionToken savedDistributionToken = distributionTokenRepository.save(distributionToken);
        distributionRedisService.saveTokenToRedis(new DistributionTokenDto(savedDistributionToken));

        return new DistributionTokenDto(distributionToken);
    }



}
