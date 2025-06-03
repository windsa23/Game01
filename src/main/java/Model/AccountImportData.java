package Model;

public class AccountImportData {
    private String accountType;
    private String customerId;
    private double param1;
    private double param2;

    public AccountImportData(String accountType, String customerId, double param1, double param2) {
        this.accountType = accountType;
        this.customerId = customerId;
        this.param1 = param1;
        this.param2 = param2;
    }

    // 添加 getter 方法
    public String getAccountType() {
        return accountType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getParam1() {
        return param1;
    }

    public double getParam2() {
        return param2;
    }
}
