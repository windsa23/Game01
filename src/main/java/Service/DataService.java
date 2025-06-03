package Service;

import Model.Account;
import Model.CreditAccount;
import Model.Customer;
import Model.SavingAccount;

import java.io.*;
import java.util.Map;

public class DataService {
    private static final String DATA_FILE = "D:\\javastudyapp\\Game01\\bankdata.txt";

    public void saveData(Map<String, Account> accounts, Map<String, Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            // 保存客户信息
            writer.write("Customers:");
            writer.newLine();
            for (Customer customer : customers.values()) {
                writer.write(customer.getCustomerId() + "," + customer.getName() + "," + customer.getPhone());
                writer.newLine();
            }

            // 保存账户信息
            writer.write("Accounts:");
            writer.newLine();
            for (Account account : accounts.values()) {
                String accountType = account instanceof SavingAccount ? "SAVINGS" : "CREDIT";
                writer.write(accountType + "," + account.getAccountNumber() + "," + account.getOwner().getCustomerId() + "," + account.getBalance());
                if (account instanceof SavingAccount) {
                    writer.write("," + ((SavingAccount) account).getInterestRate());
                } else if (account instanceof CreditAccount) {
                    writer.write("," + ((CreditAccount) account).getCreditLimit() + "," + ((CreditAccount) account).getAnnualFee());
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadData(Map<String, Account> accounts, Map<String, Customer> customers) {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            boolean isReadingCustomers = true;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Customers:")) {
                    isReadingCustomers = true;
                    continue;
                } else if (line.startsWith("Accounts:")) {
                    isReadingCustomers = false;
                    continue;
                }

                if (isReadingCustomers) {
                    try {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String customerId = parts[0];
                            String name = parts[1];
                            String phone = parts[2];
                            Customer customer = new Customer(customerId, name, phone);
                            customers.put(customerId, customer);
                        }
                    } catch (Exception e) {
                        System.err.println("加载客户信息失败，行内容: " + line + ", 错误信息: " + e.getMessage());
                    }
                } else {
                    try {
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            String accountType = parts[0];
                            String accountNumber = parts[1];
                            String ownerId = parts[2];
                            double balance = Double.parseDouble(parts[3]);
                            Customer owner = customers.get(ownerId);

                            if (owner == null) {
                                System.err.println("找不到账户 " + accountNumber + " 的客户 " + ownerId);
                                continue;
                            }

                            Account account;
                            if ("SAVINGS".equals(accountType) && parts.length >= 5) {
                                double interestRate = Double.parseDouble(parts[4]);
                                account = new SavingAccount(accountNumber, owner, interestRate);
                            } else if ("CREDIT".equals(accountType) && parts.length >= 6) {
                                double creditLimit = Double.parseDouble(parts[4]);
                                double annualFee = Double.parseDouble(parts[5]);
                                account = new CreditAccount(accountNumber, owner, creditLimit, annualFee);
                            } else {
                                System.err.println("无效的账户信息: " + line);
                                continue;
                            }

                            // 设置账户余额
                            java.lang.reflect.Field balanceField = Account.class.getDeclaredField("balance");
                            balanceField.setAccessible(true);
                            balanceField.set(account, balance);
                            accounts.put(accountNumber, account);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("数字解析失败，行内容: " + line + ", 错误信息: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("加载账户信息失败，行内容: " + line + ", 错误信息: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("读取数据文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
