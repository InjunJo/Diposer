package team.moebius.disposer.dto;

import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class ReqDistribution {

    private Long amount;

    private int recipientCount;

    public ReqDistribution() {
    }

    public ReqDistribution(Long amount, int recipientCount) {
        this.amount = amount;
        this.recipientCount = recipientCount;
    }
}
