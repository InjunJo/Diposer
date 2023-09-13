package team.moebius.disposer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Getter;
import team.moebius.disposer.dto.DistributionTokenDto;

@Entity
@Getter
public class RecipientResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private DistributionToken distributionToken;

    @Column(columnDefinition = "json")
    private String result;

    public RecipientResult(DistributionTokenDto distributionTokenDto, String result) {
        this.distributionToken = distributionTokenDto.convertDistributionToken();
        this.result = result;
    }

    public RecipientResult() {

    }
}
