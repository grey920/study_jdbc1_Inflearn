package hello.jdbc.connection;

// abstract를 사용한 이유 : 상수를 모아둔 곳이므로 더 객체를 생성할 수 없도록 막음
public abstract class ConnectionConst {
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

}
