package team.moebius.disposer.service;

import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.DistributionToken;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.TokenRepository;

@Service
@RequiredArgsConstructor
public class DistributionQueryService {

    private final TokenRepository tokenRepository;
    private final DistributionRedisService distributionRedisService;

    public DistributionTokenDto getDistributionToken(String roomId, String tokenKey, String createdTime)
        throws NotFoundTokenException{

        return findTokenFromRedis(roomId, tokenKey, createdTime)
            .orElseGet(() -> findTokenFromDB(roomId, tokenKey,createdTime));
    }

    public Set<DistributionTokenDto> findExpiredDistributionTokens(long targetUnixTime){
        return distributionRedisService.findExpiredReceiveToken(targetUnixTime);
    }

    private DistributionTokenDto findTokenFromDB(String roomId, String tokenKey,String createdTime) {
        return tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey,Long.parseLong(createdTime))
            .map(DistributionTokenDto::new)
            .orElseThrow(() -> new NotFoundTokenException("The specified token does not exist."));
    }

    private Optional<DistributionTokenDto> findTokenFromRedis(String roomId, String tokenKey, String createTime) {
        return distributionRedisService.loadTokenFromRedis(tokenKey, roomId, createTime);
    }


}
