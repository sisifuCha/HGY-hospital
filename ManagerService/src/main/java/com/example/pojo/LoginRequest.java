package pojo;

// 这个类定义了您希望从JSON中接收哪些字段
public class LoginRequest {
    private String password;

    // 必须有无参构造函数
    public LoginRequest() {}

    // 必须有getter和setter方法
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}