package team.moebius.disposer.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.domain.ReceiveInfo;
import team.moebius.disposer.domain.TokenInfo;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@Service
@RequiredArgsConstructor
public class TokenQueryService {

    private final TokenRepository tokenRepository;
    private final RecipientRepository recipientRepository;

    @Transactional(readOnly = true)
    public TokenInfo provideInfo(long userId, String roomId, String tokenKey, Long targetTime) {

        Token token = checkIsPresentAndGetToken(roomId, tokenKey);
        checkIsDistributor(userId, token, false);
        checkReadExpTime(token, targetTime);

        List<Recipient> receiveRecipients = getReceiveRecipients(token);

        return buildTokenInfo(
            token,
            sumReceiveAmounts(receiveRecipients),
            getReceiveInfoList(receiveRecipients)
        );
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

    private Token checkIsPresentAndGetToken(String roomId, String tokenKey)
        throws NotFoundTokenException {

        Optional<Token> optionalToken =
            tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey);

        if (optionalToken.isEmpty()) {
            throw new NotFoundTokenException("The specified token does not exist.");
        }

        return optionalToken.get();
    }


    private void checkIsDistributor(long userId, Token token, boolean isExcludeDistributor)
        throws TokenException {

        if (!isExcludeDistributor && !token.isDistributor(userId)) {
            throw new TokenException("You can only query tokens you distributed.");
        }
    }

}
