package team.moebius.disposer.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.moebius.disposer.domain.DistributionInfo;
import team.moebius.disposer.domain.DistributionInfoMapper;
import team.moebius.disposer.domain.ReceiveInfo;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.entity.Recipient;
import team.moebius.disposer.entity.RecipientResult;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.TokenException;
import team.moebius.disposer.repo.RecipientRepository;
import team.moebius.disposer.repo.RecipientResultRepository;
import team.moebius.disposer.util.DateTimeSupporter;

/**
 *  받기 요청 작업에 대한 상태 조회를 할 수 있는 Read 클래스
 */
@Service
@RequiredArgsConstructor
public class RecipientQueryService {
    private final RecipientRepository recipientRepository;
    private final RecipientResultRepository recipientResultRepository;
    @Value("${token.receive_exp}")
    private long TOKEN_RECEIVE_EXP;
    @Value("${dateTime.ZoneId}")
    private String ZONE_ID;

    public DistributionInfo getDistributionInfo(DistributionTokenDto distributionTokenDto, long requestTime) {

        return isExpiredReceive(distributionTokenDto, requestTime) ?
            fetchNonExpiredDistributionInfo(distributionTokenDto) :
            fetchExpiredDistributionInfo(distributionTokenDto);
    }

    public void checkValidRequest(Long userId, DistributionTokenDto distributionTokenDto,
        long targetTime) {

        checkIsDistributor(userId, distributionTokenDto);
        checkReadExpTime(distributionTokenDto, targetTime);
    }

    public List<RecipientResult> mapTokensToRecipientResults(
        Set<DistributionTokenDto> readExpiredDistributionTokens) {

        return readExpiredDistributionTokens.stream()
            .map(token ->
                new RecipientResult(
                    token,
                    getDistributionInfoAsJson(token))
            )
            .toList();
    }

    private String getDistributionInfoAsJson(DistributionTokenDto distributionTokenDto){
        return DistributionInfoMapper.toJson(fetchNonExpiredDistributionInfo(distributionTokenDto));
    }


    private DistributionInfo fetchNonExpiredDistributionInfo(DistributionTokenDto distributionTokenDto) {
        List<Recipient> allocatedRecipients = getAllocatedRecipients(distributionTokenDto);

        return buildDistributionInfo(
            distributionTokenDto,
            sumReceiveAmounts(allocatedRecipients),
            getReceiveInfoList(allocatedRecipients)
        );
    }

    private DistributionInfo fetchExpiredDistributionInfo(DistributionTokenDto distributionTokenDto){
        return DistributionInfoMapper.toTokenInfo(
            getRecipientResultInfo(distributionTokenDto).getResult()
        );
    }

    private boolean isExpiredReceive(DistributionTokenDto distributionTokenDto, long requestTime) {
        return distributionTokenDto.getReceiveExp() <= requestTime + TOKEN_RECEIVE_EXP;
    }


    private void checkReadExpTime(DistributionTokenDto distributionTokenDto, long targetTime) {
        if (distributionTokenDto.getReadExp() <= targetTime) {
            throw new TokenException("The token has expired for reading.");
        }
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

    // 조회 작업에 대한 응답값을 구성 한다.
    private DistributionInfo buildDistributionInfo(DistributionTokenDto distributionTokenDto,
        long totalAmount,
        List<ReceiveInfo> receiveInfos) {

        return DistributionInfo.builder()
            .distributeTime(
                DateTimeSupporter.convertUnixTime(distributionTokenDto.getCreatedDateTime(),ZONE_ID)
            )
            .distributeAmount(distributionTokenDto.getAmount())
            .receiveTotalAmount(totalAmount)
            .receiveInfoList(receiveInfos)
            .build();
    }

    private List<Recipient> getAllocatedRecipients(DistributionTokenDto distributionTokenDto) {
        return recipientRepository.findAllocatedRecipientsByTokenId(distributionTokenDto.getId());
    }

    private RecipientResult getRecipientResultInfo(DistributionTokenDto distributionTokenDto) {
        return recipientResultRepository.findResultByTokeId(distributionTokenDto.getId())
            .orElseThrow(NotFoundTokenException::new);
    }

    private void checkIsDistributor(long userId, DistributionTokenDto distributionTokenDto)
        throws TokenException {

        if (!distributionTokenDto.isDistributor(userId)) {
            throw new TokenException("You can only query tokens you distributed.");
        }
    }

}
