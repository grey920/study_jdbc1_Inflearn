# 3. JDBC 개발 - 등록 (INSERT)

- 소스 전문 (MemberRepositoryV0)
    
    ```java
    @Slf4j
    public class MemberRepositoryV0 {
    
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
    
            if ( rs != null ){
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("error", e);
                }
            }
    
            // close에서 에러가 발생하면 특별히 처리해 줄 수 있는게 없기 때문에 로그만 남긴다.
            if( stmt != null ){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error( "error", e );
                }
    
            }
    
            if( con != null ){
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error( "error", e );
                }
            }
    
        }
    
        private Connection getConnection() {
            return DBConnectionUtil.getConnection();
        }
    
        ;
    }
    ```
    

## INSERT

1. 커넥션을 획득한다
2. DB에 전달할 SQL과 파라미터로 전달할 데이터를 준비한다.
3. excuteUpdate로 준비된 SQL을 커넥션을 통해 실제 DB로 전달한다. 실행 후 영향받은 row수를 반환한다.

```java
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
```

> `중요!`
**사용한 리소스는 역순으로 반드시 정리해야 한다!**
→ 외부 리소스를 사용하고 닫아주지 않으면 계속 열린채로 유지되어 누수가 발생하게 된다. 이는 결과적으로 커넥션 부족으로 장애가 발생할 수 있다.
> 

### 리소스 정리

- 사용한 리소스를 닫을 때 그 과정에서 익셉션이 발생하는 경우 다른 리소스를 닫지 못할 위험이 있다. </br>
따라서 if문으로 개별적으로 확인해서 처리해 중간에 익셉션이 발생해도 다른 리소스를 정리하는 데에 영향이 가지 않도록 한다.

```java
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

    if ( rs != null ){
        try {
            rs.close();
        } catch (SQLException e) {
            log.error("error", e);
        }
    }

    // close에서 에러가 발생하면 특별히 처리해 줄 수 있는게 없기 때문에 로그만 남긴다.
    if( stmt != null ){
        try {
            stmt.close();
        } catch (SQLException e) {
            log.error( "error", e );
        }

    }

    if( con != null ){
        try {
            con.close();
        } catch (SQLException e) {
            log.error( "error", e );
        }
    }

}

private Connection getConnection() {
    return DBConnectionUtil.getConnection();
}
```

### 번외) 테스트코드 구동시 에러 발생

- 에러 제목: `org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "MEMBER" not found; SQL statement`
  - 현상 : MEMBER 테이블이 없다고 하는데 h2에서 MEMBER 테이블이 계속 있고 h2 자체에서 insert 수행시 정상 작동
  - 원인 : JDBC를 연결할 때 작성한 URL과 h2에 연결한 URL이 상이함.. 애플리케이션에서 작성한 url로 h2에 접속하니 MEMBER 테이블이 존재하지 않은 상태였다. 여기서 만들고 해결!