package hello.jdbc.connection;

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


    /**
     * DataSource 인터페이스를 통해서 커넥션을 조회해온다
     *
     * @param dataSource
     * @throws SQLException
     */
    private void useDataSource( DataSource dataSource ) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info( "connection={}, class={}", con1, con1.getClass() );
        log.info( "connection={}, class={}", con2, con2.getClass() );

    }

}
