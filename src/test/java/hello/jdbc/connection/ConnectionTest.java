package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {

        // 커넥션 획득 (사용을 위해 호출할때마다 URL, USERNAME, PASSWORD를 던져야 한다)
        Connection con1 = DriverManager.getConnection( URL, USERNAME, PASSWORD );
        Connection con2 = DriverManager.getConnection( URL, USERNAME, PASSWORD );
        log.info( "connection={}, class={}", con1, con1.getClass() );
        log.info( "connection={}, class={}", con2, con2.getClass() );
    }


    /**
     * [설정과 사용의 분리]
     * - 설정: DataSource 생성, URL/USERNAME/PASSWORD 같은 부분. 설정과 관련된 속성들은 한 곳에 있는 것이 향후 변경에 더 유연하게 대처할 수 있다
     * - 사용: 설정은 신경쓰지 않고, DataSource의 getConnection()만 호출해서 사용하면 된다.
     * => 쉽게 말해 리포지토리는 DataSource만 의존하고 설정 정보는 몰라도 된다.
     *
     *
     * DataSource 인터페이스를 사용해서 커넥션을 획득하는 방식
     * => 처음 객체 생성시에만 필요한 파라미터(URL, USERNAME, PASSWORD)를 넘겨주고, 커넥션 획득시에는 단순히 dataSource.getConnection()만 호출하면 된다
     * @throws SQLException
     */
    @Test
    void dataSourceDriverManager() throws SQLException {

        // DriverManagerDataSource - 항상 새로운 커넥션 획득
        // 스프링에서 제공 (javax.sql.DataSource의 구현체)
        DataSource dataSource = new DriverManagerDataSource( URL, USERNAME, PASSWORD );
        useDataSource( dataSource );
    }



    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {

        //커넥션 풀링
        // HikariDataSource는 DataSource의 구현체
        HikariDataSource dataSource = new HikariDataSource(); //스프링에서 JDBC를 쓰면 자동으로 HikariCP가 제공된다.
        dataSource.setJdbcUrl( URL );
        dataSource.setUsername( USERNAME );
        dataSource.setPassword( PASSWORD );
        dataSource.setMaximumPoolSize( 10 );
        dataSource.setPoolName( "MyPool" ); // 풀의 이름 지정. 안하면 기본값이 나옴

        useDataSource( dataSource );

        /**
         * Thread.sleep(1000) 을 걸어둔 이유?
         * -> 풀에 커넥션을 넣는 작업은 `connection adder` 라는 별도의 쓰레드에서 동작한다.
         * 슬립을 걸어두지 않으면 useDataSource()를 수행하고 바로 끝나버리기 때문에 커넥션 풀에 커넥션을 add하는 로그를 제대로 볼 수 없어서
         * 로그를 보기 위해 잠깐의 대기시간을 걸어둔 것.
         *
         * 로그:
         * 07:49:13.192 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn2: url=jdbc:h2:tcp://localhost/~/test user=SA
         * ...
         * 07:49:13.216 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - After adding stats (total=10, active=2, idle=8, waiting=0)
         *
         *
         * connection adder가 별도의 쓰레드로 동작하는 이유는??
         * => 커넥션 풀에 커넥션을 채우는 것은 상대적으로 오래 걸리는 일이다.
         * 애플리케이션을 실행할 떄 커넥션 풀을 채울 때까지 마냥 대기하고 있다면 `애플리케이션 실행 시간이 늦어진다`
         * 따라서 이렇게 별도의 쓰레드를 사용해서 커넥션 풀을 채워야 애플리케이션 실행 시간에 영향을 주지 않는다
        */
        Thread.sleep( 1000 );
    }


    /**
     * DataSource 인터페이스를 통해서 커넥션을 조회해온다
     *
     * - 만약 커넥션을 획득해야 하는데 풀에 커넥션이 없다면? -> 커넥션을 획득할 때까지 내부적으로 기다린다.
     * - 풀이 다 차서 설정된 대기시간을 넘기면 에러 발생! java.sql.SQLTransientConnectionException: MyPool - Connection is not available, request timed out after 30012ms.
     *                                          at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:696)
     * @param dataSource
     * @throws SQLException
     */
    private void useDataSource( DataSource dataSource ) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        
        // 07:49:13.094 [main] INFO hello.jdbc.connection.ConnectionTest - connection=HikariProxyConnection@479920916 wrapping conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class com.zaxxer.hikari.pool.HikariProxyConnection
        // 07:49:13.095 [main] INFO hello.jdbc.connection.ConnectionTest - connection=HikariProxyConnection@1161322357 wrapping conn1: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class com.zaxxer.hikari.pool.HikariProxyConnection
        log.info( "connection={}, class={}", con1, con1.getClass() );
        log.info( "connection={}, class={}", con2, con2.getClass() );

    }

}
