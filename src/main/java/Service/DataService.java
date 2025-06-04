package Service;

import Model.*;

import java.io.*;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class DataService {
    private static final String DATA_FILE = "D:\\javastudyapp\\Game01\\bankdata.txt";

    public void saveData(Map<String, Account> accounts, Map<String, Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            // 保存客户信息
            writer.write("Customers:\n");
            for (Customer customer : customers.values()) {
                writer.write(customer.getCustomerId() + "," + customer.getName() + "," + customer.getPhone() + "\n");
            }

            // 保存账户信息
            writer.write("Accounts:\n");
            for (Account account : accounts.values()) {
                String accountType = account instanceof SavingAccount ? "SAVINGS" : "CREDIT";
                writer.write(accountType + "," + account.getAccountNumber() + "," +
                        account.getOwner().getCustomerId() + "," + account.getBalance());
                if (account instanceof SavingAccount) {
                    writer.write("," + ((SavingAccount) account).getInterestRate());
                } else if (account instanceof CreditAccount) {
                    writer.write("," + ((CreditAccount) account).getCreditLimit() +
                            "," + ((CreditAccount) account).getAnnualFee());
                }
                writer.write("\n");
            }

            // 保存交易记录
            writer.write("Transactions:\n");
            for (Account account : accounts.values()) {
                for (Transaction transaction : account.getTransactions()) {
                    // 使用 Transaction 的 toString() 方法确保格式正确
                    writer.write(transaction.toString() + "\n");
                }
            }

            System.out.println("成功保存 " + accounts.size() + " 个账户和交易记录到 " + DATA_FILE);

        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadData(Map<String, Account> accounts, Map<String, Customer> customers) {
        try (BufferedReader reader = new BufferedReader(new FileReader("D:\\javastudyapp\\Game01\\bankdata.txt"))) {
            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) continue;

                if (line.equals("Customers:")) {
                    currentSection = "customers";
                    continue;
                } else if (line.equals("Accounts:")) {
                    currentSection = "accounts";
                    continue;
                } else if (line.equals("Transactions:")) {
                    currentSection = "transactions";
                    continue;
                }

                switch (currentSection) {
                    case "customers":
                        // 解析客户数据
                        String[] customerParts = line.split(",");
                        String customerId = customerParts[0];
                        String name = customerParts[1];
                        String phone = customerParts[2];
                        customers.put(customerId, new Customer(customerId, name, phone));
                        break;

                    case "accounts":
                        // 解析账户数据
                        String[] accountParts = line.split(",");
                        String accountType = accountParts[0];
                        String accountNumber = accountParts[1]; // 第一个 accountNumber 定义
                        String ownerId = accountParts[2];
                        double balance = Double.parseDouble(accountParts[3]);

                        Customer owner = customers.get(ownerId);
                        if (owner != null) {
                            Account account;
                            if (accountType.equals("SAVINGS")) {
                                double interestRate = Double.parseDouble(accountParts[4]);

                                account = new SavingAccount(accountNumber, owner, balance);

                            } else {
                                double creditLimit = Double.parseDouble(accountParts[4]);
                                double annualFee = Double.parseDouble(accountParts[5]);
                                account = new CreditAccount(accountNumber, owner, balance, creditLimit);

                            }
                            accounts.put(accountNumber, account);
                        }
                        break;

                    case "transactions":
                        // 解析交易数据
                        if (line.startsWith("[")) {
                            // 提取时间、账户、类型和金额
                            String timeStr = line.substring(1, line.indexOf("]"));
                            int startIndex = line.indexOf("账户 ") + 3;
                            int endIndex = line.indexOf(" -");
                            if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) {
                                String transactionAccountNumber = line.substring(startIndex, endIndex);
                                startIndex = line.indexOf(" - ") + 3;
                                endIndex = line.indexOf(":");
                                if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) {
                                    String type = line.substring(startIndex, endIndex);
                                    startIndex = line.indexOf(":") + 1;
                                    if (startIndex >= 0 && startIndex < line.length()) {
                                        double amount = Double.parseDouble(line.substring(startIndex).trim());

                                        Account account = accounts.get(transactionAccountNumber);
                                        if (account != null) {
                                            // 创建交易并添加到账户
                                            Transaction transaction = new Transaction(type, amount, transactionAccountNumber, timeStr);
                                            account.addTransaction(transaction);
                                        }
                                    }
                                }
                            }
                        }
                        break;

                }
            }
        } catch (IOException e) {
            System.err.println("加载数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
