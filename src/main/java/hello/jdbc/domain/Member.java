package hello.jdbc.domain;

import lombok.Data;

@Data
public class Member {

    // 회원 ID
    private String memberId;
    // 회원이 소지한 금액
    private int money;

    public Member(){}

    public Member( String memberId, int money ){
        this.memberId = memberId;
        this.money = money;
    }

}
