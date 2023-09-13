package team.moebius.disposer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.TokenFactory;

@Service
@RequiredArgsConstructor
public class DistributionCommandService {
    private final TokenRepository tokenRepository;
    private final DistributionRedisService distributionRedisService;
    private final TokenFactory tokenFactory;

    @Transactional
    public DistributionTokenDto distribute(Long userId, String roomId, Long amount,
        int recipientCount, long nowDataTime) {

        DistributionToken distributionToken =
            tokenFactory.buildToken(nowDataTime,userId,roomId,amount,recipientCount);

        DistributionToken savedDistributionToken = tokenRepository.save(distributionToken);
        distributionRedisService.saveTokenToRedis(new DistributionTokenDto(savedDistributionToken));

        return new DistributionTokenDto(distributionToken);
    }



}
