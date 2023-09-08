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

@Entity
@Getter @ToString
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Exclude
    private Token token;

    private Long amount;

    @Nullable
    private Long userId;

    public Recipient() {
    }

    public void setUserId(@Nullable Long userId) {
        this.userId = userId;
    }

    public Recipient(Token token, Long amount) {
        this.token = token;
        this.amount = amount;
    }

    public Recipient(Token token, Long amount, @Nullable Long userId) {
        this.token = token;
        this.amount = amount;
        this.userId = userId;
    }
}
