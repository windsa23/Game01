package Model;

public class SavingAccount extends Account {
    private final double interestRate;

    public SavingAccount(String number, Customer owner, double rate) {
        super(number, owner);
        this.interestRate = rate;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("取款金额必须为正数");
        if (balance >= amount) {
            balance -= amount;
            // 记录取款交易
            transactions.add(new Transaction("WITHDRAW", -amount, accountNumber));
            return true;
        }
        return false;
    }

    @Override
    public void applyInterest() {
        double interest = balance * interestRate;
        deposit(interest);
        transactions.add(new Transaction("INTEREST", interest, accountNumber));
    }

    public double getInterestRate() { return interestRate; }
}
