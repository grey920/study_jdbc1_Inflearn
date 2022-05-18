package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DataSource 사용, JdbcUtils 사용
 *
 * 1. DataSource 의존관계 주입
 *  - 이제 DBConnectionUtil을 사용하지 않아도 된다.
 *  - 외부에서 DataSource를 주입 받아서 사용한다. DataSource는 표준 인터페이스이기 때문에 DriverManagerDataSource에서 HikariCP로 변경되어도
 *      해당 코드를 변경하지 않아도 된다.
 *
 * 2. JdbcUtil 편의 메서드 사용
 *  - 스프링이 제공하는 JDBC를 편리하게 다룰 수 있는 편의 메서드
 *  - JdbcUtils를 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다.
 *          JdbcUtils.closeResultSet( rs );
 *         JdbcUtils.closeStatement( stmt );
 *         JdbcUtils.closeConnection( con );
 */
@Slf4j
public class MemberRepositoryV1 {

    // 의존관계 주입
    private final DataSource dataSource;

    public MemberRepositoryV1 ( DataSource dataSource ) {
        this.dataSource = dataSource;
    }

    public Member save( Member member ) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        // Connection이 있어야 연결을 할 수 있다.
        Connection con = null;
        PreparedStatement pstmt = null;


        try {

            con = getConnection();
            pstmt = con.prepareStatement( sql );
            // 파라미터 바인딩
            pstmt.setString(1, member.getMemberId() );
            pstmt.setInt(2, member.getMoney() );

            // 실행
            int count = pstmt.executeUpdate(); // count: 영향받은 row 수

            return member;
        }
        // error log 남기고 밖으로 Exception 던지기
        catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        // [중요!] 오픈과 역순으로 닫아주기! 항상 닫는것이 보장되도록 finally에서 리소스를 정리하기
        // TCP/IP 커넥션에 걸려서 외부 리소스를 쓰는 것이기 때문에 닫아주지 않으면 계~~속 유지될 수 있다.
        finally {
            close( con, pstmt, null );
            }


    }

    public Member findById( String memberId ) throws SQLException {

        String sql = "select * from Member where member_id = ?";

        Connection con = null; // finally 구문에서 호출해야 하기 떄문에 밖으로 빼서 선언한다
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId );

            // executeUpdate()는 변경할때 사용하고, select 쿼리는 executeQuery()를 사용한다
            rs = pstmt.executeQuery();

            // rs.next()를 한 번은 호출해줘야 그 다음부터 실제 응답을 출력한다
            if ( rs.next() ){

                Member member = new Member();
                member.setMemberId( rs.getString("member_id") );
                member.setMoney( rs.getInt("money") );
                return member;
            }
            // 데이터가 없는 경우
            else{
                /* 에러 메시지를 작성할 떄 key값을 잘 넣어줘야 실제 운영에서 문제를 찾을 때 편리하다 */
                throw new NoSuchElementException("member not found, memberId = " + memberId );
            }
        } catch (SQLException e) {
            log.error( "db error", e );
            throw e;
        }
        finally {
            close( con, pstmt, rs );
        }

    }

    public void update( String memberId, int money ) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement( sql );
            // 파라미터 바인딩
            pstmt.setInt(1, money );
            pstmt.setString(2, memberId );

            // 실행
            int count = pstmt.executeUpdate(); // count: 영향받은 row 수
            log.info( "result size={}", count );

        }
        // error log 남기고 밖으로 Exception 던지기
        catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        // [중요!] 오픈과 역순으로 닫아주기! 항상 닫는것이 보장되도록 finally에서 리소스를 정리하기
        // TCP/IP 커넥션에 걸려서 외부 리소스를 쓰는 것이기 때문에 닫아주지 않으면 계~~속 유지될 수 있다.
        finally {
            close( con, pstmt, null );
        }

    }

    public void delete( String memberId ) throws SQLException {

        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement( sql );
            pstmt.setString(1, memberId );

            // 실행
            int count = pstmt.executeUpdate(); // count: 영향받은 row 수
            log.info( "result size={}", count );

        }
        catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        finally {
            close( con, pstmt, null );
        }

    }

    /**
     * 따로 if문으로 묶어주는 이유??
     * => 따로 처리하지 않으면 Statement를 close하다가 Exception이 발생하면 그대로 끝나버려서 Connection을 닫아주지 못하는 상황이 발생하기 때문에!
     * => 지금은 만약에 stmt에서 SQLException이 발생해도 catch문에서 잡히고 다음 Connection을 닫는 if절로 이동할 수 있다.
     * @param con
     * @param stmt
     * @param rs
     */
    // Prestatement를 보냈는데 Statement로 받는다? -> 상속받은거라 가능
    private void close(Connection con, Statement stmt, ResultSet rs ){

        JdbcUtils.closeResultSet( rs );
        JdbcUtils.closeStatement( stmt );
        JdbcUtils.closeConnection( con );

    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info( "get connection={}, class={}" , con, con.getClass() );
        return con;
    }


}
