package View;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Model.*;
import Service.BankService;
import java.util.List; // 添加 List 导入
import java.util.ArrayList; // 如果需要使用 ArrayList 也可添加

public class MainStage extends Stage {
    private final BankService bankService;
    private final TabPane tabPane = new TabPane();
    private TransactionPane transactionPane;

    public MainStage(BankService bankService) {
        this.bankService = bankService;
        setupUI();
    }

    private void setupUI() {
        // 创建标签页
        Tab customerTab = new Tab("客户管理", new CustomerPane(bankService));
        AccountPane accountPane = new AccountPane(bankService, this);
        Tab accountTab = new Tab("账户管理", accountPane);

        // 初始化交易记录面板
        transactionPane = new TransactionPane(bankService);
        Tab transactionTab = new Tab("交易记录", transactionPane);

        tabPane.getTabs().addAll(customerTab, accountTab, transactionTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 设置主场景
        Scene scene = new Scene(tabPane, 800, 600);
        setScene(scene);
        setTitle("银行管理系统");
    }

    public void selectAccount(Account account) {
        if (account != null) {
            transactionPane.updateTransactions(account.getTransactions());
        }
    }

    // 添加更新交易记录的方法
    public void updateTransactions(List<Transaction> transactions) {
        if (transactionPane != null) {
            transactionPane.updateTransactions(transactions);
        }
    }
}
