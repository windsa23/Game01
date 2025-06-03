package View;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import Model.Transaction;
import Service.BankService;

import java.util.List;

public class TransactionPane extends VBox {
    private final BankService bankService;
    private final TableView<Transaction> transactionTable = new TableView<>();

    public TransactionPane(BankService bankService) {
        this.bankService = bankService;
        setupUI();
    }

    private void setupUI() {
        // 交易记录表格
        TableColumn<Transaction, String> timestampCol = new TableColumn<>("交易时间");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        TableColumn<Transaction, String> accountCol = new TableColumn<>("交易账户");
        accountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("交易类型");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("交易金额");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        transactionTable.getColumns().addAll(timestampCol, accountCol, typeCol, amountCol);

        // 将表格添加到布局中
        getChildren().add(transactionTable);
    }

    /**
     * 更新交易记录表格的数据
     * @param transactions 要显示的交易记录列表
     */
    public void updateTransactions(List<Transaction> transactions) {
        transactionTable.getItems().setAll(transactions);
    }
}
