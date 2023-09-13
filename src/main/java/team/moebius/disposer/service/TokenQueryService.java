package team.moebius.disposer.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.domain.ReceiveInfo;
import team.moebius.disposer.domain.TokenInfo;
import team.moebius.disposer.domain.TokenInfoMapper;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.RecipientResult;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.RecipientResultRepository;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@Service
@RequiredArgsConstructor
public class TokenQueryService {

    private final TokenRepository tokenRepository;

    private final TokenRedisService tokenRedisService;
    private final RecipientRepository recipientRepository;
    private final RecipientResultRepository recipientResultRepository;

    @Transactional(readOnly = true)
    public TokenInfo provideTokenInfo(long userId, String roomId, String tokenKey,
        String createTime, Long targetTime) {

        Token token = checkIsPresentAndGetToken(roomId, tokenKey, createTime);
        checkIsDistributor(userId, token);
        checkReadExpTime(token, targetTime);

        return isExpiredReceive(token, targetTime) ?
            TokenInfoMapper.toTokenInfo(getRecipientResultInfo(token).getResult()) :
            assembleTokenInfo(token);
    }

    public TokenInfo provideTokenInfo(Token token) {
        return assembleTokenInfo(token);
    }

    private TokenInfo assembleTokenInfo(Token token) {
        List<Recipient> receiveRecipients = getReceiveRecipients(token);

        return buildTokenInfo(
            token,
            sumReceiveAmounts(receiveRecipients),
            getReceiveInfoList(receiveRecipients)
        );
    }

    private RecipientResult getRecipientResultInfo(Token token) {

        return recipientResultRepository.findResultByTokeId(token.getId())
            .orElseThrow(NotFoundTokenException::new);
    }

    private boolean isExpiredReceive(Token token, long targetTime) {
        return token.getReceiveExp() <= targetTime;
    }

    // 조회 작업에 대한 응답값을 구성 한다.
    private TokenInfo buildTokenInfo(Token token, long totalAmount,
        List<ReceiveInfo> receiveInfos) {

        return TokenInfo.builder()
            .distributeTime(
                DateTimeSupporter.convertUnixTime(token.getCreatedDateTime())
            )
            .distributeAmount(token.getAmount())
            .receiveTotalAmount(totalAmount)
            .receiveInfoList(receiveInfos)
            .build();
    }

    // Token에 해당하는 받기 작업이 가능한
    private List<Recipient> getReceiveRecipients(Token token) {
        return recipientRepository.findReceiveAllByToken(token.getId());
    }

    private List<ReceiveInfo> getReceiveInfoList(List<Recipient> receiveRecipients) {
        return receiveRecipients.stream()
            .map(recipient -> new ReceiveInfo(recipient.getAmount(), recipient.getUserId()))
            .toList();
    }

    private long sumReceiveAmounts(List<Recipient> receiveRecipients) {
        return receiveRecipients.stream()
            .mapToLong(Recipient::getAmount)
            .sum();
    }

    private void checkReadExpTime(Token token, long targetTime) {
        if (token.getReadExp() <= targetTime) {
            throw new TokenException("The token has expired for reading.");
        }
    }


    public Token checkIsPresentAndGetToken(String roomId, String tokenKey, String createdTime)
        throws NotFoundTokenException {

        return findTokenFromRedis(roomId, tokenKey, createdTime)
            .orElseGet(() -> findTokenFromDB(roomId, tokenKey,createdTime));
    }

    private Token findTokenFromDB(String roomId, String tokenKey,String createdTime) {
        return tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey,Long.parseLong(createdTime))
            .orElseThrow(() -> new NotFoundTokenException("The specified token does not exist."));
    }

    private Optional<Token> findTokenFromRedis(String roomId, String tokenKey, String createTime) {
        return tokenRedisService.loadTokenFromRedis(tokenKey, roomId, createTime);
    }


    private void checkIsDistributor(long userId, Token token)
        throws TokenException {

        if (!token.isDistributor(userId)) {
            throw new TokenException("You can only query tokens you distributed.");
        }
    }

}
