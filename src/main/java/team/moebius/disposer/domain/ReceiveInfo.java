package team.moebius.disposer.domain;

/**
 * 뿌리기 건에 대해 받기 요청을 하여 할당 된 유저에 대한 데이터를 담는다.
 * @param receiveAmount 받기 요청을 통해 받은 금액
 * @param userId 받기 요청을 통해 성공적으로 할당돼 금액을 받은 유저 ID
 */
public record ReceiveInfo(long receiveAmount, long userId) {

}
