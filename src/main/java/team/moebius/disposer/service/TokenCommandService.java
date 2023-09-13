package team.moebius.disposer.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.domain.TokenInfoMapper;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.RecipientResult;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.RecipientException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.RecipientResultRepository;
import team.moebius.disposer.repo.TokenRepository;

@Service
@RequiredArgsConstructor
public class TokenCommandService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 3;
    private static final long RECEIVE_EXP = 10 * 60 * 1000;
    private static final long READ_EXP = 7L * 24 * 60 * 60 * 1000;
    private final TokenRepository tokenRepository;
    private final TokenQueryService tokenQueryService;
    private final RecipientResultRepository recipientResultRepository;

    private final TokenRedisService tokenRedisService;


    @Transactional
    public Token generateToken(Long userId, String roomId, Long amount, int recipientCount,
        long nowDataTime) {

        Token token = buildToken(nowDataTime, userId, roomId, amount, recipientCount);

        Token savedToken = tokenRepository.save(token);
        tokenRedisService.saveTokenToRedis(savedToken);

        return token;
    }



    @Scheduled(fixedRate = RECEIVE_EXP)
    @Transactional
    void savePreComputedResult() {
        Set<Token> readExpiredTokens = tokenRedisService.findExpiredReceiveToken();

        List<RecipientResult> recipientResults = mapTokensToRecipientResults(readExpiredTokens);

        recipientResultRepository.saveAll(recipientResults);
    }

    private List<RecipientResult> mapTokensToRecipientResults(Set<Token> readExpiredTokens) {
        return readExpiredTokens.stream()
            .map(token ->
                new RecipientResult(
                    token,
                    TokenInfoMapper.toJson(tokenQueryService.provideTokenInfo(token)))
            )
            .toList();
    }

    // 뿌리기 요청을 받아 각각 처리된 Token 데이터를 저장 한다.
    Token buildToken(long now, Long userId, String roomId, Long amount, int recipientCount) {
        return Token.builder()
            .tokenKey(generateTokenKey())
            .createdDateTime(now)
            .receiveExp(getReceiveExpTime(now))
            .readExp(getReadExpTime(now))
            .amount(amount)
            .recipientCount(recipientCount)
            .distributorId(userId)
            .roomId(roomId)
            .build();
    }

    // Stream generate를 통해 임의의 3자리 문자열 token key를 생성 한다.
    String generateTokenKey() {
        Random random = new Random();

        return Stream.generate(
                () -> CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))
            )
            .limit(KEY_LENGTH)
            .map(String::valueOf)
            .collect(Collectors.joining());
    }



    // unix time 형식으로 뿌리기 건에 대한 받기 요청 10분 유효 시간을 계산해서 반환 한다.
    long getReceiveExpTime(long epochMilli) {
        return epochMilli + RECEIVE_EXP;
    }

    // unix time 형식으로 뿌리기 건에 대한 조회 요청 7일 유효 시간을 계산해서 반환 한다.
    long getReadExpTime(long epochMilli) {
        return epochMilli + READ_EXP;
    }

    static long getReceiveExp() {
        return RECEIVE_EXP;
    }


}
