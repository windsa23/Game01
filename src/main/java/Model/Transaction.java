package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final String type;
    private final double amount;
    private LocalDateTime timestamp;
    private final String accountNumber;

    public Transaction(String type, double amount, String accountNumber) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.accountNumber = accountNumber;
    }

    // 传入交易时间
    public Transaction(String type, double amount, String accountNumber, LocalDateTime timestamp) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.accountNumber = accountNumber;
    }
    public Transaction(String type, double amount, String accountNumber, String timestampStr) {
        this.type = type;
        this.amount = amount;
        this.accountNumber = accountNumber;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d H:m:ss");
        this.timestamp = LocalDateTime.parse(timestampStr, formatter);
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String toString() {
        return String.format("[%s] 账户 %s - %s: %s%.2f",
                timestamp.toLocalTime(),
                accountNumber,
                type,
                amount >= 0 ? "+" : "",
                amount);
    }
}
