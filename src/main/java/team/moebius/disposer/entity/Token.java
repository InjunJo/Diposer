package team.moebius.disposer.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString @Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenKey;

    private Long distributorId;

    private String roomId;

    private Long amount;

    private Integer recipientCount;

    private Long createdDateTime;

    private Long receiveExp;

    private Long readExp;


    public boolean isDistributor(long distributorId){
        return this.distributorId == distributorId;
    }
}
