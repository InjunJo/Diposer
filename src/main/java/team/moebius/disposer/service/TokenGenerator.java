package team.moebius.disposer.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.TokenRepository;

@Service
@RequiredArgsConstructor
public class TokenGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 3;
    private static final long RECEIVE_EXP = 10 * 60 * 1000;
    private static final long READ_EXP = 7L * 24 * 60 * 60 * 1000;
    private static final int LIMIT_TRIAL = 10;
    private final TokenRepository tokenRepository;
    private final RecipientRepository recipientRepository;

    @Transactional
    public String generateToken(Long userId,String roomId,Long amount,int recipientCount,long nowDataTime){

        Token token = buildToken(nowDataTime,userId,roomId,amount,recipientCount);

        int trial = 0;

        while (isDuplicateToken(roomId,token)){
            if(++trial >= LIMIT_TRIAL){
                throw new TokenException();
            }
            token = buildToken(nowDataTime,userId,roomId,amount,recipientCount);
        }

        tokenRepository.save(token);
        generateRecipients(token,amount,recipientCount);

        return token.getTokenKey();
    }

    private boolean isDuplicateToken(String roomId,Token token){
        Optional<Token> optionalToken =
            tokenRepository.findTokenByRoomIdAndTokenKey(roomId, token.getTokenKey());

        return optionalToken.isPresent();
    }

    // 뿌릴 금액을 인원 수에 맞게 분배하여 저장 한다.
    private void generateRecipients(Token token,Long amount,int recipientCount){

        List<Recipient> recipients = divideAmount(amount, recipientCount).stream()
            .map(money -> new Recipient(token, money))
            .toList();

        recipientRepository.saveAll(recipients);
    }

    // 뿌리기 요청을 받아 각각 처리된 Token 데이터를 저장 한다.
    Token buildToken(long now,Long userId,String roomId,Long amount,int recipientCount){
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
    public String generateTokenKey() {
        Random random = new Random();

        return Stream.generate(
                () -> CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))
            )
            .limit(KEY_LENGTH)
            .map(String::valueOf)
            .collect(Collectors.joining());
    }

    // 뿌릴 전체 금액에 대해  뿌릴 인원을 나누고, 나누어 떨어지지 않아 남는 나머지는 한쪽에 저장 한다.
    public List<Long> divideAmount(Long amount,int recipientCount){

        long bonus = amount % recipientCount;
        long baseAmount = amount / recipientCount;

        return Stream.iterate(baseAmount + bonus, aLong -> baseAmount)
            .limit(recipientCount)
            .collect(Collectors.toList());
    }

    // unix time 형식으로 뿌리기 건에 대한 받기 요청 10분 유효 시간을 계산해서 반환 한다.
    public long getReceiveExpTime(long epochMilli){
        return epochMilli+RECEIVE_EXP;
    }

    // unix time 형식으로 뿌리기 건에 대한 조회 요청 7일 유효 시간을 계산해서 반환 한다.
    public long getReadExpTime(long epochMilli){
        return epochMilli+READ_EXP;
    }



}
