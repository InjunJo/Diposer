package team.moebius.disposer.service;

import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.repo.DistributionTokenRepository;

/**
 * 뿌리기 요청와 관련된 데이터를 조회하기 위한 read 클래스
 */
@Service
@RequiredArgsConstructor
public class DistributionQueryService {

    private final DistributionTokenRepository distributionTokenRepository;
    private final DistributionRedisService distributionRedisService;

    // 사용자 요청에 맞는 Token을 찾아 Dto 형태로 변환하여 반환 한다.
    public DistributionTokenDto getDistributionToken(String roomId, String tokenKey,
        String createdTime)
        throws NotFoundTokenException {

        // Redis Cache에서 먼저 Token을 검색 후 cache miss일 경우 DB에서 Token을 찾는다.
        return findTokenFromRedis(roomId, tokenKey, createdTime)
            .orElseGet(() -> findTokenFromDB(roomId, tokenKey, createdTime));
    }

    public Set<DistributionTokenDto> findExpiredDistributionTokens(long targetUnixTime) {
        return distributionRedisService.findExpiredReceiveToken(targetUnixTime);
    }

    private DistributionTokenDto findTokenFromDB(String roomId, String tokenKey, String createdTime)
        throws NotFoundTokenException {

        return distributionTokenRepository.findTokenByRelatedData(roomId, tokenKey,
                Long.parseLong(createdTime)
            )
            .map(DistributionTokenDto::new)
            .orElseThrow(() -> new NotFoundTokenException("The specified token does not exist."));
    }

    private Optional<DistributionTokenDto> findTokenFromRedis(String roomId, String tokenKey,
        String createTime) {

        return distributionRedisService.loadTokenFromRedis(tokenKey, roomId, createTime);
    }


}
