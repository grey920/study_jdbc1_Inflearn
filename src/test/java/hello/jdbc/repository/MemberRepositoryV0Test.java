package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        /* save */
        Member member = new Member( "memberV100", 10000 );
        repository.save( member );

        /* findById */
        Member findMember = repository.findById( member.getMemberId() );
        log.info( "findMember={}", findMember );

        // member == findMember false : member와 findMember는 다른 인스턴스이다. findMember는 ResultSet에서 담을때 생성한 인스턴스
        log.info( "member == findMember {}", member == findMember );

        // member.equals( findMember ) true
        // 이유: Member객체 만들때 사용한 @Data 롬복은 EqualsAndHashCode를 가지고 있다. 따라서 Member객체의 필드값들이 같다면 equals는 true가 된다
        log.info( "member.equals( findMember ) {}", member.equals( findMember ) );

        // 찾은 멤버가 방금 등록한 멤버와 같다
        assertThat( findMember ).isEqualTo( member );

        /* update: money : 10000 -> 20000 */
        repository.update( member.getMemberId(), 20000 );
        Member updatedMember = repository.findById( member.getMemberId() );
        assertThat( updatedMember.getMoney() ).isEqualTo( 20000 );

        /* delete -> 테스트 중간에 오류가 발생하는 경우 삭제 로직이 수행되지 않기 때문에 아래같은 코드는 좋은 코드는 아님
        * => 트랜잭션 활용!!!*/
        repository.delete( member.getMemberId() );
        // 방금 생성한 멤버가 NoSuchElement 익셉션을 던지면 정상적으로 삭제되었다고 판단한다.
        Assertions.assertThatThrownBy( () -> repository.findById( member.getMemberId() ) )
                .isInstanceOf( NoSuchElementException.class );
    }

}