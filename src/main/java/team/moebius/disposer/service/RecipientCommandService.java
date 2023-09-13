package team.moebius.disposer.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.RecipientResult;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.RecipientException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.RecipientResultRepository;

/**
 * 뿌리기 요청 이후 만들어진 받기 요청 대기 데이터(Recipient)에 받기 요청을 한 사용자를 저장하여 할당 한다.
 */
@Service
@RequiredArgsConstructor
public class RecipientCommandService {

    private final RecipientRepository recipientRepository;

    private final RecipientResultRepository recipientResultRepository;

    // 뿌리기에 대한 받기 작업을 처리 한다.
    @Transactional
    public Long provideShare(long userId, DistributionTokenDto distributionTokenDto,
        Long targetTime)
        throws NotFoundTokenException, TokenException {

        // 뿌리기를 한 사용자가 받기 작업을 요청 했다면 에러를 반환 한다.
        filterDistributorRequest(userId, distributionTokenDto);

        // 요청 작업을 한 Token이 받기 작업 만료 시간이 초과 됐다면 에러를 반환 한다.
        checkReceiveExpTime(distributionTokenDto, targetTime);

        // Token에 대해 받기 작업이 가능한 데이터를 찾아 반환 한다.
        return allocateRecipientForUser(distributionTokenDto, userId);
    }

    @Transactional
    public void savePreComputedResult(List<RecipientResult> recipientResults) {
        recipientResultRepository.saveAll(recipientResults);
    }

    private void filterDistributorRequest(long userId, DistributionTokenDto distributionTokenDto)
        throws TokenException {

        if (distributionTokenDto.isDistributor(userId)) {
            throw new TokenException("You cannot receive a share from a token you distributed.");
        }
    }

    private void checkReceiveExpTime(DistributionTokenDto distributionTokenDto, long targetTime)
        throws TokenException {

        if (distributionTokenDto.getReceiveExp() <= targetTime) {
            throw new TokenException("The token has expired for receiving.");
        }
    }

    private Long allocateRecipientForUser(DistributionTokenDto distributionTokenDto, long userId) {

        List<Recipient> recipients =
            recipientRepository.findAllByTokenId(distributionTokenDto.getId());

        checkAlreadyReceiveUser(recipients, userId);

        Recipient recipient = findUnallocatedRecipient(recipients);
        recipient.setUserId(userId);

        return recipient.getAmount();
    }

    // 이미 받기 할당된 유저가 다시 요청하면 에러를 반환 한다.
    private void checkAlreadyReceiveUser(List<Recipient> recipients, long userId)
        throws RecipientException {

        boolean isAlreadyReceiveUser = recipients.stream()
            .anyMatch(
                recipient -> recipient.getUserId() != null && recipient.getUserId() == userId
            );

        if (isAlreadyReceiveUser) {
            throw new RecipientException("User has already received a share from the distribution");
        }
    }

    // 받기 요청을 할당 가능한지 찾아보고, 모두 할당 돼 있으면 에러를 반환 한다.
    private Recipient findUnallocatedRecipient(List<Recipient> recipients)
        throws RecipientException {

        Optional<Recipient> optionalRecipient = recipients.stream()
            .filter(recipient -> recipient.getUserId() == null)
            .findAny();

        return optionalRecipient.orElseThrow(() ->
            new RecipientException("The distribution has been fully received")
        );
    }

    // 뿌릴 금액을 인원 수에 맞게 분배하여 저장 한다.
    public void generateRecipients(DistributionTokenDto distributionToken, Long amount,
        int recipientCount) {

        List<Recipient> recipients = divideAmount(amount, recipientCount).stream()
            .map(money -> new Recipient(distributionToken, money))
            .toList();

        recipientRepository.saveAll(recipients);
    }

    // 뿌릴 전체 금액에 대해  뿌릴 인원을 나누고, 나누어 떨어지지 않아 남는 나머지는 한쪽에 저장 한다.
    private List<Long> divideAmount(Long amount, int recipientCount) {

        long bonus = amount % recipientCount;
        long baseAmount = amount / recipientCount;

        return Stream.iterate(baseAmount + bonus, aLong -> baseAmount)
            .limit(recipientCount)
            .collect(Collectors.toList());
    }

}
