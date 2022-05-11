package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

/**
 * DB 연결
 */
@Slf4j
public class DBConnectionUtil {

    /**
     * JDBC 표준 인터페이스가 제공하는 커넥션
     * @return
     */
    public static Connection getConnection(){
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info( "get connection={}, class={}", connection, connection.getClass() );
            // INFO hello.jdbc.connection.DBConnectionUtil - get connection=conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class org.h2.jdbc.JdbcConnection
            return connection;
        }
        // Checked Exception -> Runtime Exception 으로 변경!
        catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
