import javafx.application.Application;
import javafx.stage.Stage;
import Service.BankService;
import View.MainStage;

public class Bank extends Application {
    @Override
    public void start(Stage primaryStage) {
        BankService bankService = new BankService();
        bankService.loadData();

        MainStage mainStage = new MainStage(bankService);
        mainStage.show();

        // 注册关闭事件保存数据
        mainStage.setOnCloseRequest(e -> bankService.saveData());
    }

    public static void main(String[] args) {
        launch(args);
    }
}