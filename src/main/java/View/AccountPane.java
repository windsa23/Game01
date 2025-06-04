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
import javafx.stage.Modality;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import Model.Transaction;

import java.io.*;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.DateUtil;
import javafx.scene.layout.VBox;


public class AccountPane extends BorderPane {
    private BankService bankService;
    private MainStage mainStage;
    private TableView<Account> accountTable;
    private Account selectedAccount;
    private TransactionPane transactionPane;

    public AccountPane(BankService bankService, MainStage mainStage) {
        this.bankService = bankService;
        this.mainStage = mainStage;
        this.transactionPane = new TransactionPane(bankService);
        setupUI();
        refreshAccountTable();
    }

    private void setupUI() {
        // 账户表格
        accountTable = new TableView<>();
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
                // 当选择账户时，更新交易记录表格
                List<Transaction> transactions = newValue.getTransactions();
                transactionPane.updateTransactions(transactions);
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
        // 水平布局，左侧账户表格，右侧交易记录表格
        HBox tableBox = new HBox(10);
        tableBox.getChildren().addAll(accountTable, transactionPane);
        setCenter(tableBox); // 将账户表格和交易记录表格的水平布局设置为中心区域
        setRight(buttonBox); // 将按钮区域设置为右侧区域
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
            List<ErrorInfo> errorInfos = new ArrayList<>(); // 存储每行的详细错误信息
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy/M/dd h:mm:ss");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/M/dd hh:mm:ss");

            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 从第 2 行（索引 1）开始，第 1 行是表头
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        // 修复：使用正确的构造函数，然后添加错误详情
                        ErrorInfo errorInfo = new ErrorInfo(i + 1);
                        errorInfo.addDetail("整行数据为空，请补充数据后重新导入");
                        errorInfos.add(errorInfo);
                        continue;
                    }

                    ErrorInfo rowError = new ErrorInfo(i + 1); // 初始化当前行错误信息
                    boolean hasError = false;

                    // 解析交易时间
                    Cell timeCell = row.getCell(0);
                    String timestampStr = "";

                    // 处理 Excel 单元格类型：如果是日期类型，强制转成字符串
                    if (timeCell != null) {
                        if (timeCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(timeCell)) {
                            // 直接转成字符串，用 Excel 配置的格式
                            timestampStr = timeCell.getLocalDateTimeCellValue()
                                    .format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"));
                        } else {
                            timestampStr = getCellValueAsString(timeCell).trim();
                        }
                    }

                    LocalDateTime timestamp = null;
                    if (timestampStr.isEmpty()) {
                        rowError.addDetail("交易时间为空，请填写格式：yyyy/M/d HH:mm:ss（如 2025/5/2 09:15:22 ）");
                        hasError = true;
                    } else {
                        // 尝试多种常见格式兼容（按优先级排序）
                        List<DateTimeFormatter> formatters = Arrays.asList(
                                DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"), // 标准格式（优先）
                                DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"), // 兼容分隔符 '-'
                                DateTimeFormatter.ofPattern("yyyy/M/d h:mm:ss a"),// 兼容 12 小时制（带 AM/PM）
                                DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss")  // 原格式兜底
                        );

                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                timestamp = LocalDateTime.parse(timestampStr, formatter);
                                break; // 解析成功则跳出循环
                            } catch (DateTimeParseException e) {
                                // 继续尝试下一种格式
                            }
                        }

                        if (timestamp == null) {
                            rowError.addDetail("交易时间格式错误！正确格式：yyyy/M/d HH:mm:ss（如 2025/5/2 09:15:22 ）\n当前内容：" + timestampStr);
                            hasError = true;
                        }
                    }

                    // 解析交易账户
                    Cell accountCell = row.getCell(1);
                    String accountNumber = getCellValueAsString(accountCell);
                    if (accountNumber.isEmpty()) {
                        rowError.addDetail("交易账户为空，请填写有效的账户编号");
                        hasError = true;
                    } else {
                        Account account = bankService.findAccount(accountNumber);
                        if (account == null) {
                            rowError.addDetail("交易账户不存在，请检查账户编号是否正确，或先添加对应账户");
                            hasError = true;
                        }
                    }

                    // 解析交易类型
                    Cell typeCell = row.getCell(2);
                    String type = getCellValueAsString(typeCell);
                    if (type.isEmpty()) {
                        rowError.addDetail("交易类型为空，请填写（如 存款、取款 等）");
                        hasError = true;
                    }

                    // 解析交易金额
                    Cell amountCell = row.getCell(3);
                    String amountStr = getCellValueAsString(amountCell);
                    double amount = 0;
                    if (amountStr.isEmpty()) {
                        rowError.addDetail("交易金额为空，请填写数字（如 5000、-1800.5 等）");
                        hasError = true;
                    } else {
                        try {
                            amount = Double.parseDouble(amountStr);
                        } catch (NumberFormatException e) {
                            rowError.addDetail("交易金额格式错误，需为数字（如 5000、-1800.5 ，不能包含字母、汉字等非数字内容）");
                            hasError = true;
                        }
                    }

                    // 如果当前行有错误，记录错误信息；否则添加交易记录
                    if (hasError) {
                        errorInfos.add(rowError);
                    } else {
                        Transaction transaction = new Transaction(type, amount, accountNumber, timestamp);
                        Account account = bankService.findAccount(accountNumber);
                        account.addTransaction(transaction);
                    }
                }

                // 保存数据到系统
                if (errorInfos.isEmpty()) {
                    bankService.saveData();
                    // 刷新账户表格和交易视图（假设有对应的刷新方法）
                    refreshAccountTable();
                    mainStage.refreshTransactionView();
                }

                // 根据错误信息展示提示
                if (!errorInfos.isEmpty()) {
                    showDetailedErrorAlert(errorInfos);
                } else {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("导入成功");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("交易记录导入成功！共导入 " + (sheet.getLastRowNum()) + " 条有效数据");
                    successAlert.showAndWait();
                }

            } catch (IOException e) {
                showErrorAlert("文件读取错误: " + e.getMessage());
            }
        }
    }

    private void saveTransactionToFile(Transaction transaction) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("bankdata.txt", true))) {
            writer.write(transaction.toString());
            writer.newLine();
        } catch (IOException e) {
            showErrorAlert("保存交易记录失败: " + e.getMessage());
        }
    }
    private List<Transaction> parseTransactionsFromExcel(File file) {
        List<Transaction> transactions = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String accountNumber = getCellValueAsString(row.getCell(0));
                String type = getCellValueAsString(row.getCell(1));
                double amount = Double.parseDouble(getCellValueAsString(row.getCell(2)));
                String timestampStr = getCellValueAsString(row.getCell(3));
                Transaction transaction = new Transaction(type, amount, accountNumber, timestampStr);
                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    //获取单元格内容（处理空单元格等情况）
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
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

    // 辅助方法：展示详细错误提示
    private void showDetailedErrorAlert(List<ErrorInfo> errorInfos) {
        // 1. 创建自定义弹窗容器
        javafx.stage.Stage errorStage = new javafx.stage.Stage();
        errorStage.setTitle("导入失败 - 详细错误");
        errorStage.initModality(Modality.APPLICATION_MODAL); // 阻塞主窗口
        errorStage.setResizable(true);

        // 2. 标题
        Label titleLabel = new Label("交易记录导入失败");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D9534F;");

        // 3. 错误详情区域（带滚动）
        TextArea errorTextArea = new TextArea();
        errorTextArea.setEditable(false);
        errorTextArea.setWrapText(true);
        errorTextArea.setPrefHeight(300);
        errorTextArea.setPrefWidth(500);
        errorTextArea.setStyle("-fx-font-size: 14px; -fx-background-color: #F9F9F9;");

        // 拼接错误信息，排版更清晰
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("共 ").append(errorInfos.size()).append(" 行数据异常：\n\n");
        for (ErrorInfo info : errorInfos) {
            errorMsg.append("第 ").append(info.getRowNum()).append(" 行：\n");
            for (String detail : info.getDetails()) {
                errorMsg.append("  - ").append(detail).append("\n");
            }
            errorMsg.append("\n");
        }
        errorTextArea.setText(errorMsg.toString());

        // 4. 按钮区域（关闭/复制）
        Button closeBtn = new Button("关闭");
        closeBtn.setStyle("-fx-background-color: #428BCA; -fx-text-fill: white; -fx-padding: 6px 12px;");
        closeBtn.setOnAction(e -> errorStage.close());

        Button copyBtn = new Button("复制错误信息");
        copyBtn.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-padding: 6px 12px;");
        copyBtn.setOnAction(e -> {
            // 复制文本到剪贴板
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(errorTextArea.getText());
            clipboard.setContent(content);
       });

        HBox buttonBox = new HBox(10, copyBtn, closeBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // 5. 组装布局
        VBox mainBox = new VBox(15, titleLabel, errorTextArea, buttonBox);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: white; -fx-border-color: #DDD; -fx-border-width: 1px;");

        errorStage.setScene(new javafx.scene.Scene(mainBox));
        errorStage.showAndWait();
    }



    // 辅助方法：展示简单错误提示（可复用）
    private void showErrorAlert(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("导入失败");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    // 内部类：用于存储每行的错误信息（行号 + 详细错误描述）
    private static class ErrorInfo {
        private int rowNum; // 行号（Excel 中显示的行号，从 1 开始）
        private List<String> details; // 该行的详细错误描述

        public ErrorInfo(int rowNum) {
            this.rowNum = rowNum;
            this.details = new ArrayList<>();
        }

        public int getRowNum() {
            return rowNum;
        }

        public List<String> getDetails() {
            return details;
        }

        public void addDetail(String detail) {
            this.details.add(detail);
        }
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }




    private void refreshAccountTable() {
        List<Account> accounts = bankService.getAllAccounts();
        accountTable.setItems(FXCollections.observableArrayList(accounts));
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
