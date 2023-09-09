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
import team.moebius.disposer.exception.RecipientException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.TokenRepository;
import team.moebius.disposer.util.DateTimeSupporter;

@Service
@RequiredArgsConstructor
public class TokenManager {

    private final TokenRepository tokenRepository;

    private final RecipientRepository recipientRepository;

    @Transactional
    public Long provideShare(long userId, String roomId, String tokenKey, Long targetTime) {

        Token token = checkIsPresentAndGetToken(roomId, tokenKey);

        filterDistributorRequest(userId, token, true);
        checkReceiveExpTime(token, targetTime);

        return findReceivableRecipient(token, userId);
    }

    @Transactional(readOnly = true)
    public TokenInfo provideInfo(long userId, String roomId, String tokenKey, Long targetTime) {

        Token token = checkIsPresentAndGetToken(roomId, tokenKey);
        filterDistributorRequest(userId, token, false);
        checkReadExpTime(token, targetTime);

        List<Recipient> receiveRecipients = getReceiveRecipients(token);

        return buildTokenInfo(
            token,
            sumReceiveAmounts(receiveRecipients),
            getReceiveInfoList(receiveRecipients)
        );
    }


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

    private Long findReceivableRecipient(Token token, long userId) {

        List<Recipient> recipients =
            recipientRepository.findAllByTokenId(token.getId());

        checkAlreadyReceiveUser(recipients, userId);

        Recipient recipient = getReceivableRecipient(recipients);
        recipient.setUserId(userId);

        return recipient.getAmount();
    }

    private Recipient getReceivableRecipient(List<Recipient> recipients) {

        Optional<Recipient> optionalRecipient = recipients.stream()
            .filter(recipient -> recipient.getUserId() == null)
            .findAny();

        if (optionalRecipient.isEmpty()) {
            throw new RecipientException("The distribution has been fully received");
        }

        return optionalRecipient.get();
    }

    private void checkAlreadyReceiveUser(List<Recipient> recipients, long userId) {

        boolean isAlreadyReceiveUser = recipients.stream()
            .anyMatch(
                recipient -> recipient.getUserId() != null && recipient.getUserId() == userId
            );

        if (isAlreadyReceiveUser) {
            throw new RecipientException("User has already received a share from the distribution");
        }
    }


    private void checkReceiveExpTime(Token token, long targetTime) {
        if (token.getReceiveExp() <= targetTime) {
            throw new TokenException("The token has expired for receiving.");
        }
    }

    private void checkReadExpTime(Token token, long targetTime) {
        if (token.getReadExp() <= targetTime) {
            throw new TokenException("The token has expired for reading.");
        }
    }

    private Token checkIsPresentAndGetToken(String roomId, String tokenKey) {

        Optional<Token> optionalToken =
            tokenRepository.findTokenByRoomIdAndTokenKey(roomId, tokenKey);

        if (optionalToken.isEmpty()) {
            throw new NotFoundTokenException("The specified token does not exist.");
        }

        return optionalToken.get();
    }


    private void filterDistributorRequest(long userId, Token token, boolean isExcludeDistributor) {

        if (isExcludeDistributor && token.isDistributor(userId)) {
            throw new TokenException("You cannot receive a share from a token you distributed.");
        }

        if (!isExcludeDistributor && !token.isDistributor(userId)) {
            throw new TokenException("You can only query tokens you distributed.");
        }
    }

}
