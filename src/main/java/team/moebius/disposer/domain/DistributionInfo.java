package team.moebius.disposer.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 *  조회 요청에 대한 관련 데이터를 담는다
 */
@Getter @Builder
@ToString
@AllArgsConstructor
public class DistributionInfo {

    private String distributeTime;

    private long distributeAmount;

    private long receiveTotalAmount;

    private List<ReceiveInfo> receiveInfoList;

    public DistributionInfo() {
    }
}
