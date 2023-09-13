package team.moebius.disposer.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.springframework.lang.Nullable;
import team.moebius.disposer.dto.DistributionTokenDto;

@Entity
@Getter @ToString
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Exclude
    private DistributionToken distributionToken;

    private Long amount;

    @Nullable
    private Long userId;

    public Recipient() {
    }

    public void setUserId(@Nullable Long userId) {
        this.userId = userId;
    }

    public Recipient(DistributionTokenDto distributionTokenDto, Long amount) {
        this.distributionToken = distributionTokenDto.convertDistributionToken();
        this.amount = amount;
    }

    public Recipient(DistributionTokenDto distributionTokenDto, Long amount, @Nullable Long userId) {
        this.distributionToken = distributionTokenDto.convertDistributionToken();
        this.amount = amount;
        this.userId = userId;
    }
}
