package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] 账户 %s - %s: %s%.2f",
                timestamp.format(formatter),
                accountNumber,
                type,
                amount >= 0 ? "+" : "",
                amount);
    }


    public Transaction(String type, double amount, String accountNumber, String timestampStr) {
        this.type = type;
        this.amount = amount;
        this.accountNumber = accountNumber;

        try {
            // 尝试解析带秒的格式
            DateTimeFormatter formatterWithSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.timestamp = LocalDateTime.parse("2024-01-01 " + timestampStr, formatterWithSeconds);
        } catch (DateTimeParseException e) {
            try {
                // 尝试解析不带秒的格式
                DateTimeFormatter formatterWithoutSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                this.timestamp = LocalDateTime.parse("2024-01-01 " + timestampStr + ":00", formatterWithoutSeconds);
            } catch (DateTimeParseException ex) {
                // 默认使用当前时间
                this.timestamp = LocalDateTime.now();
            }
        }
    }

    // 格式化时间
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
}
