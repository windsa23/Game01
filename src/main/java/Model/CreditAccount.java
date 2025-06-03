package Model;

public class CreditAccount extends Account {
    private final double creditLimit;
    private final double annualFee;

    public CreditAccount(String number, Customer owner, double limit, double fee) {
        super(number, owner);
        this.creditLimit = limit;
        this.annualFee = fee;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("取款金额必须为正数");
        if (balance - amount >= -creditLimit) {
            balance -= amount;
            // 记录取款交易
            transactions.add(new Transaction("CREDIT_WITHDRAW", -amount, accountNumber));
            return true;
        }
        return false;
    }

    @Override
    public void applyInterest() {
        balance -= annualFee;
        transactions.add(new Transaction("ANNUAL_FEE", -annualFee, accountNumber));
    }

    public double getCreditLimit() { return creditLimit; }
    public double getAnnualFee() { return annualFee; }
}
