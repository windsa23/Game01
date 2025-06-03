package View;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import Model.*;
import Service.BankService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import Model.Transaction;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.DateUtil;
import java.time.format.DateTimeParseException;
import javafx.scene.layout.VBox;

public class AccountPane extends BorderPane {
    private final BankService bankService;
    private final TableView<Account> accountTable = new TableView<>();
    private final MainStage mainStage;

    public AccountPane(BankService bankService, MainStage mainStage) {
        this.bankService = bankService;
        this.mainStage = mainStage;
        setupUI();
        refreshAccountTable();
    }

    private void setupUI() {
        // 账户表格
        TableColumn<Account, String> numCol = new TableColumn<>("账号");
        numCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<Account, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getClass().getSimpleName()));

        TableColumn<Account, Number> balanceCol = new TableColumn<>("余额");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        accountTable.getColumns().addAll(numCol, typeCol, balanceCol);
        accountTable.setPrefHeight(300);

        // 账户选择监听
        accountTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mainStage.selectAccount(newValue);
            }
        });

        // 操作按钮
        Button openBtn = new Button("开户");
        openBtn.setPrefWidth(120);
        openBtn.setOnAction(e -> showOpenAccountDialog());

        Button depositBtn = new Button("存款");
        depositBtn.setPrefWidth(120);
        depositBtn.setOnAction(e -> showDepositDialog());

        Button withdrawBtn = new Button("取款");
        withdrawBtn.setPrefWidth(120);
        withdrawBtn.setOnAction(e -> showWithdrawDialog());

        Button importBtn = new Button("导入账户信息");
        importBtn.setPrefWidth(120);
        importBtn.setOnAction(e -> importAccounts());

        Button importTransactionBtn = new Button("导入交易记录");
        importTransactionBtn.setPrefWidth(120);
        importTransactionBtn.setOnAction(e -> importTransactions());

// AccountPane类的setupUI方法片段
        Button exportBtn = new Button("导出账户信息");
        exportBtn.setPrefWidth(120);
        exportBtn.setOnAction(e -> exportAccounts());

        VBox buttonBox = new VBox(10, openBtn, depositBtn, withdrawBtn, importBtn, importTransactionBtn, exportBtn);
        buttonBox.setPadding(new Insets(10));

// 布局
        setCenter(accountTable);
        setRight(buttonBox);
        setPadding(new Insets(10));



    }
    private void exportAccounts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            bankService.exportDataToExcel(file.getAbsolutePath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("导出成功");
            alert.setHeaderText(null);
            alert.setContentText("账户信息已成功导出到 " + file.getAbsolutePath());
            alert.showAndWait();
        }
    }
    private void showOpenAccountDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("开户");
        dialog.setHeaderText("请输入账户信息");
        dialog.initOwner(getScene().getWindow());

        ButtonType openButtonType = new ButtonType("开户", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(openButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField customerIdField = new TextField();
        customerIdField.setPromptText("客户ID");
        ComboBox<String> accountTypeComboBox = new ComboBox<>();
        accountTypeComboBox.getItems().addAll("SavingAccount", "CreditAccount");
        accountTypeComboBox.setValue("SavingAccount");
        TextField param1Field = new TextField();
        param1Field.setPromptText("利率/信用额度");
        TextField param2Field = new TextField();
        param2Field.setPromptText("初始余额/年费");

        grid.add(new Label("客户ID:"), 0, 0);
        grid.add(customerIdField, 1, 0);
        grid.add(new Label("账户类型:"), 0, 1);
        grid.add(accountTypeComboBox, 1, 1);
        grid.add(new Label("参数1:"), 0, 2);
        grid.add(param1Field, 1, 2);
        grid.add(new Label("参数2:"), 0, 3);
        grid.add(param2Field, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == openButtonType) {
                try {
                    String customerId = customerIdField.getText();
                    String accountType = accountTypeComboBox.getValue();
                    double param1 = Double.parseDouble(param1Field.getText());
                    double param2 = Double.parseDouble(param2Field.getText());

                    bankService.openAccount(accountType, customerId, param1, param2);
                    refreshAccountTable();
                } catch (Exception e) {
                    showErrorAlert("开户失败: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showDepositDialog() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showErrorAlert("请先选择一个账户进行存款操作。");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("存款");
        dialog.setHeaderText("请输入存款金额");
        dialog.initOwner(getScene().getWindow());

        ButtonType depositButtonType = new ButtonType("存款", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(depositButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("金额");

        grid.add(new Label("金额:"), 0, 0);
        grid.add(amountField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == depositButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    selectedAccount.deposit(amount);
                    bankService.saveData();
                    refreshAccountTable();
                } catch (Exception e) {
                    showErrorAlert("存款失败: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showWithdrawDialog() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showErrorAlert("请先选择一个账户进行取款操作。");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("取款");
        dialog.setHeaderText("请输入取款金额");
        dialog.initOwner(getScene().getWindow());

        ButtonType withdrawButtonType = new ButtonType("取款", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(withdrawButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("金额");

        grid.add(new Label("金额:"), 0, 0);
        grid.add(amountField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == withdrawButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    selectedAccount.withdraw(amount);
                    bankService.saveData();
                    refreshAccountTable();
                } catch (Exception e) {
                    showErrorAlert("取款失败: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void importTransactions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                List<Transaction> transactions = new ArrayList<>();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    // 获取交易时间
                    org.apache.poi.ss.usermodel.Cell timestampCell = row.getCell(0);
                    String timestampStr = getCellValueAsString(timestampCell);
                    LocalDateTime timestamp = null;
                    try {
                        timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"));
                    } catch (DateTimeParseException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("错误");
                        alert.setHeaderText("导入交易记录失败");
                        alert.setContentText("第 " + (i + 1) + " 行交易时间格式错误: " + e.getMessage());
                        alert.showAndWait();
                        continue;
                    }

                    // 获取交易账户
                    org.apache.poi.ss.usermodel.Cell accountCell = row.getCell(1);
                    String accountNumber = getCellValueAsString(accountCell);

                    // 获取交易类型
                    org.apache.poi.ss.usermodel.Cell typeCell = row.getCell(2);
                    String type = getCellValueAsString(typeCell);

                    // 获取交易金额（在条件语句外部声明变量）
                    org.apache.poi.ss.usermodel.Cell amountCell = row.getCell(3);
                    String amountStr = getCellValueAsString(amountCell);
                    double amount = 0.0; // 在外部声明并初始化为默认值
                    boolean isValidAmount = false;

                    // 验证金额是否为有效数字
                    try {
                        amount = Double.parseDouble(amountStr);
                        isValidAmount = true;
                    } catch (NumberFormatException e) {
                        // 尝试移除非数字字符（如货币符号、空格等）
                        String cleanAmount = amountStr.replaceAll("[^0-9.-]", "");
                        try {
                            amount = Double.parseDouble(cleanAmount);
                            isValidAmount = true;
                        } catch (NumberFormatException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("错误");
                            alert.setHeaderText("导入交易记录失败");
                            alert.setContentText("第 " + (i + 1) + " 行交易金额格式错误: " + amountStr);
                            alert.showAndWait();
                            continue;
                        }
                    }

                    // 验证账户是否存在
                    Account account = bankService.findAccount(accountNumber);
                    if (account == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("错误");
                        alert.setHeaderText("导入交易记录失败");
                        alert.setContentText("第 " + (i + 1) + " 行账户不存在: " + accountNumber);
                        alert.showAndWait();
                        continue;
                    }

                    // 创建交易记录（使用外部声明的 amount 变量）
                    Transaction transaction = new Transaction(type, amount, accountNumber, timestamp);
                    account.getTransactions().add(transaction);
                    transactions.add(transaction);
                }

                // 更新交易记录表格
                mainStage.updateTransactions(transactions);

                // 保存数据
                bankService.saveData();

                // 刷新账户表格
                refreshAccountTable();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("导入成功");
                alert.setHeaderText(null);
                alert.setContentText("成功导入 " + transactions.size() + " 条交易记录。");
                alert.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("导入交易记录失败");
                alert.setContentText("文件读取错误: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }


    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }





    private void refreshAccountTable() {
        accountTable.setItems(FXCollections.observableArrayList(bankService.getAllAccounts()));
        accountTable.refresh();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("操作失败");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void importAccounts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                List<AccountImportData> importDataList = new ArrayList<>();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    String accountType = getCellValueAsString(row.getCell(0));
                    String customerId = getCellValueAsString(row.getCell(1));
                    String param1Str = getCellValueAsString(row.getCell(2));
                    String param2Str = getCellValueAsString(row.getCell(3));

                    double param1 = 0;
                    double param2 = 0;

                    if (!param1Str.isEmpty()) {
                        param1 = Double.parseDouble(param1Str);
                    }

                    if (!param2Str.isEmpty()) {
                        param2 = Double.parseDouble(param2Str);
                    }

                    AccountImportData importData = new AccountImportData(accountType, customerId, param1, param2);
                    importDataList.add(importData);
                }

                // 处理导入的数据
                for (AccountImportData importData : importDataList) {
                    try {
                        bankService.openAccount(importData.getAccountType(),
                                importData.getCustomerId(),
                                importData.getParam1(),
                                importData.getParam2());
                    } catch (Exception e) {
                        showErrorAlert("导入账户信息失败: " + e.getMessage());
                    }
                }

                refreshAccountTable();
            } catch (IOException e) {
                showErrorAlert("文件读取错误: " + e.getMessage());
            }
        }
    }
}
