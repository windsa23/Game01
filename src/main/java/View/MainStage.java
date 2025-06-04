package View;

import javafx.scene.Scene;
import javafx.scene.control.*;
import Model.Account;
import javafx.stage.Stage;
import Model.*;
import Service.BankService;
import java.util.List;

public class MainStage extends Stage {
    private BankService bankService;
    private final TabPane tabPane = new TabPane();
    private TransactionPane transactionPane;
    private AccountPane accountPane;

    public MainStage(BankService bankService) {
        this.bankService = bankService;
        setupUI();
    }

    private void setupUI() {
        // 创建标签页
        Tab customerTab = new Tab("客户管理", new CustomerPane(bankService));
        accountPane = new AccountPane(bankService, this);
        Tab accountTab = new Tab("账户管理", accountPane);

        transactionPane = new TransactionPane(bankService);


        tabPane.getTabs().addAll(customerTab, accountTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 800, 600);
        setScene(scene);
        setTitle("银行管理系统");
    }

    public void selectAccount(Account account) {
        if (account != null) {
            List<Transaction> transactions = account.getTransactions();
            transactionPane.updateTransactions(transactions);
        }
    }


    public Account getSelectedAccount() {
        return accountPane.getSelectedAccount();
    }

    public void refreshTransactionView() {
        Account selected = getSelectedAccount();
        if (selected != null) {
            List<Transaction> transactions = selected.getTransactions();
            transactionPane.updateTransactions(transactions);
            //saveTransactionsToFile(transactions);
        }
    }

    public void updateTransactions(List<Transaction> transactions) {
        if (transactionPane != null) {
            transactionPane.updateTransactions(transactions);
        }
    }
}
