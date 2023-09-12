package team.moebius.disposer.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Getter @Builder
@ToString
@AllArgsConstructor
public class TokenInfo {

    private String distributeTime;

    private long distributeAmount;

    private long receiveTotalAmount;

    private List<ReceiveInfo> receiveInfoList;

    public TokenInfo() {
    }
}
