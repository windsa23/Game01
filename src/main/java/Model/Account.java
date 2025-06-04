package Model;

import java.util.*;

public abstract class Account {
    protected final String accountNumber;
    protected double balance;
    protected final Customer owner;
    protected final List<Transaction> transactions;

    public Account(String number, Customer owner) {
        this.accountNumber = number;
        this.owner = owner;
        this.transactions = new ArrayList<>();
        this.balance = 0;
    }

    public abstract boolean withdraw(double amount);
    public abstract void applyInterest();

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("存款金额必须为正数");
        balance += amount;
        // 记录存款交易
        Transaction transaction = new Transaction("DEPOSIT", amount, accountNumber);
        transactions.add(transaction);
        System.out.println("存款交易记录添加成功: " + transaction); // 添加日志输出
    }

    public void transfer(Account target, double amount) {
        if (this.withdraw(amount)) {
            target.deposit(amount);
            // 记录转出交易
            transactions.add(new Transaction("TRANSFER_TO_" + target.getAccountNumber(), -amount, accountNumber));
            // 目标账户记录转入交易
            target.transactions.add(new Transaction("TRANSFER_FROM_" + this.accountNumber, amount, target.accountNumber));
        }
    }
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public Customer getOwner() { return owner; }
}