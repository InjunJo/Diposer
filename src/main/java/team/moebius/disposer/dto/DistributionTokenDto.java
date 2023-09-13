package team.moebius.disposer.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.ToString;
import team.moebius.disposer.entity.DistributionToken;

@Getter @ToString
public class DistributionTokenDto implements Serializable {

    private final Long id;
    private final String tokenKey;
    private final Long distributorId;
    private final String roomId;
    private final Long amount;
    private final Integer recipientCount;
    private final Long createdDateTime;
    private final Long receiveExp;
    private final Long readExp;

    public DistributionTokenDto(DistributionToken token) {
        this.id = token.getId();
        this.tokenKey = token.getTokenKey();
        this.distributorId = token.getDistributorId();
        this.roomId = token.getRoomId();
        this.amount = token.getAmount();
        this.recipientCount = token.getRecipientCount();
        this.createdDateTime = token.getCreatedDateTime();
        this.receiveExp = token.getReceiveExp();
        this.readExp = token.getReadExp();
    }

    public DistributionToken convertDistributionToken(){
        return DistributionToken.builder()
            .id(this.id)
            .tokenKey(this.tokenKey)
            .receiveExp(this.receiveExp)
            .amount(this.amount)
            .distributorId(this.distributorId)
            .createdDateTime(this.createdDateTime)
            .readExp(this.readExp)
            .roomId(this.roomId)
            .build();
    }

    public boolean isDistributor(long distributorId){
        return this.distributorId == distributorId;
    }
}
