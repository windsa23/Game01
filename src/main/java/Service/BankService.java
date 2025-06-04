package Service;

import Model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.*;

public class BankService {
    private DataService dataService;
    private Map<String, Account> accounts;
    private Map<String, Customer> customers;

    // 构造函数
    public BankService() {
        this.dataService = new DataService();
        this.accounts = new HashMap<>();
        this.customers = new HashMap<>();
        // 加载数据
        dataService.loadData(accounts, customers);
    }
    public void addTransactions(String accountNumber, List<Transaction> transactions) {
        Account account = accounts.get(accountNumber);
        if (account != null) {
            account.getTransactions().addAll(transactions);
        }
    }
    public void importTransactions(List<Transaction> transactions) {
        for (Transaction trans : transactions) {
            Account acc = accounts.get(trans.getAccountNumber());
            if (acc != null) {
                acc.getTransactions().add(trans);
            }
        }
        // 关键：导入后立即持久化到文件
        dataService.saveData(accounts, customers);
    }
    // Customer operations
    public void createCustomer(String id, String name, String phone) {
        customers.put(id, new Customer(id, name, phone));
        saveData();
    }
    // 获取指定账户的交易记录
    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        Account acc = accounts.get(accountNumber);
        return acc != null ? new ArrayList<>(acc.getTransactions()) : new ArrayList<>();
    }

    public Customer findCustomer(String id) {
        return customers.get(id);
    }

    // Account operations
    public String openAccount(String type, String customerId, double... params) {
        Customer owner = customers.get(customerId);
        if (owner == null) throw new IllegalArgumentException("客户不存在");

        String accNumber = generateAccountNumber();
        Account account = switch (type.toUpperCase()) {
            case "SAVINGS" -> new SavingAccount(accNumber, owner, params[0]);
            case "CREDIT" -> new CreditAccount(accNumber, owner, params[0], params[1]);
            default -> throw new IllegalArgumentException("无效的账户类型");
        };

        accounts.put(accNumber, account);
        saveData();
        return accNumber;
    }
    // 在BankService.java中添加
    public boolean checkCustomerExists(String customerId) {
        return customers.containsKey(customerId);
    }
    public void exportDataToExcel(String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("客户账户信息");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"客户ID", "账号", "类型", "余额", "参数1", "参数2"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            Account account = entry.getValue();
            Customer owner = account.getOwner();
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(owner.getCustomerId());
            row.createCell(1).setCellValue(account.getAccountNumber());
            if (account instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) account;
                row.createCell(2).setCellValue("SAVINGS");
                row.createCell(3).setCellValue(savingAccount.getBalance());
                row.createCell(4).setCellValue(savingAccount.getInterestRate());
                row.createCell(5).setCellValue(0); // 储蓄账户没有第二个参数
            } else if (account instanceof CreditAccount) {
                CreditAccount creditAccount = (CreditAccount) account;
                row.createCell(2).setCellValue("CREDIT");
                row.createCell(3).setCellValue(creditAccount.getBalance());
                row.createCell(4).setCellValue(creditAccount.getCreditLimit());
                row.createCell(5).setCellValue(creditAccount.getAnnualFee());
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 保存文件
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            System.err.println("导出数据到Excel文件失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Account findAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<Account> getCustomerAccounts(String customerId) {
        return accounts.values().stream()
                .filter(acc -> acc.getOwner().getCustomerId().equals(customerId))
                .toList();
    }

    // 新增存款方法
    public void deposit(String accountNumber, double amount) {
        Account account = findAccount(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("存款金额必须大于 0");
        }
        account.deposit(amount);
        saveData();
    }

    // 新增取款方法
    public boolean withdraw(String accountNumber, double amount) {
        Account account = findAccount(accountNumber);
        if (account == null) {
            return false;
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("取款金额必须大于 0");
        }
        boolean success = account.withdraw(amount);
        if (success) {
            saveData();
        }
        return success;
    }

    // 新增获取所有账户方法
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    // Data operations
    public void loadData() {
        dataService.loadData(accounts, customers);
    }

    public void saveData() {
        dataService.saveData(accounts, customers);
        System.out.println("数据已保存到文件");
        System.out.println("已从文件加载数据");
    }

    private String generateAccountNumber() {
        return "BK" + (100000 + new Random().nextInt(900000));
    }

    public List<Customer> getAllCustomers() {
        // 将 customers Map 中的所有值转换为列表返回
        return new ArrayList<>(customers.values());
    }


    public void updateCustomer(String id, String name, String phone) {
        Customer customer = customers.get(id);
        if (customer != null) {
            customer.setName(name);
            customer.setPhone(phone);
            saveData(); // 确保调用 saveData 方法
        }
    }
}
