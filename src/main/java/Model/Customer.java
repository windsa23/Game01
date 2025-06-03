package Model;

public class Customer {
    private final String customerId;
    private String name;
    private String phone;
    private String address;

    public Customer(String customerId, String name, String phone) {
        if (customerId == null || customerId.trim().isEmpty())
            throw new IllegalArgumentException("客户ID不能为空");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("客户姓名不能为空");

        this.customerId = customerId;
        this.name = name;
        this.setPhone(phone);
    }

    // Getters and Setters with validation
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("姓名不能为空");
        this.name = name;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        if (phone != null && !phone.matches("^\\d{11}$"))
            throw new IllegalArgumentException("无效的手机号格式");
        this.phone = phone;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return name + " (" + customerId + ")";
    }
}