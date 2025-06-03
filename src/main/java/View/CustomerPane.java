package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import Model.Customer;
import Service.BankService;
import org.apache.poi.ss.usermodel.*; // 添加导入语句
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomerPane extends BorderPane {
    private final BankService bankService;
    private final TableView<Customer> customerTable = new TableView<>();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    public CustomerPane(BankService bankService) {
        this.bankService = bankService;
        setupUI();
        loadData();
    }

    private void setupUI() {
        // 表格列定义
        TableColumn<Customer, String> idCol = new TableColumn<>("客户ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Customer, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Customer, String> phoneCol = new TableColumn<>("手机号");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        customerTable.setItems(customerList);
        customerTable.getColumns().addAll(idCol, nameCol, phoneCol);

        // 操作按钮
        Button addBtn = new Button("添加客户");
        addBtn.setOnAction(e -> showAddDialog());

        Button editBtn = new Button("编辑");
        editBtn.setOnAction(e -> showEditDialog());

        Button importBtn = new Button("导入客户信息");
        importBtn.setOnAction(e -> importCustomers());

        HBox buttonBox = new HBox(10, addBtn, editBtn, importBtn);

        // 布局
        setCenter(new ScrollPane(customerTable));
        setBottom(buttonBox);
    }

    private void loadData() {
        customerList.clear(); // 清空原有数据
        customerList.addAll(bankService.getAllCustomers()); // 添加新数据
    }

    private void showAddDialog() {
        // 原有添加客户对话框逻辑
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("添加客户");
        dialog.setHeaderText("请输入客户信息");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField idField = new TextField();
        idField.setPromptText("客户ID");
        TextField nameField = new TextField();
        nameField.setPromptText("姓名");
        TextField phoneField = new TextField();
        phoneField.setPromptText("手机号");

        grid.add(new Label("客户ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("姓名:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("手机号:"), 0, 2);
        grid.add(phoneField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();

                try {
                    bankService.createCustomer(id, name, phone);
                    loadData();
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("添加客户失败");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditDialog() {
        // 原有编辑客户对话框逻辑
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText("未选中客户");
            alert.setContentText("请先选择一个客户进行编辑。");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("编辑客户");
        dialog.setHeaderText("请修改客户信息");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selectedCustomer.getName());
        nameField.setPromptText("姓名");
        TextField phoneField = new TextField(selectedCustomer.getPhone());
        phoneField.setPromptText("手机号");

        grid.add(new Label("姓名:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("手机号:"), 0, 1);
        grid.add(phoneField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();

                try {
                    bankService.updateCustomer(selectedCustomer.getCustomerId(), name, phone);
                    loadData();
                    customerTable.refresh();
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("编辑客户失败");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void importCustomers() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0); // 明确指定类名
                List<Customer> customers = new ArrayList<>();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i); // 明确指定类名
                    String customerId = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String phone = getCellValueAsString(row.getCell(2));

                    try {
                        Customer customer = new Customer(customerId, name, phone);
                        customers.add(customer);
                    } catch (IllegalArgumentException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("错误");
                        alert.setHeaderText("导入客户信息失败");
                        alert.setContentText("第 " + (i + 1) + " 行数据错误: " + e.getMessage());
                        alert.showAndWait();
                    }
                }

                for (Customer customer : customers) {
                    try {
                        bankService.createCustomer(customer.getCustomerId(), customer.getName(), customer.getPhone());
                    } catch (IllegalArgumentException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("错误");
                        alert.setHeaderText("导入客户信息失败");
                        alert.setContentText("客户 " + customer.getCustomerId() + " 导入失败: " + e.getMessage());
                        alert.showAndWait();
                    }
                }

                loadData();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("导入客户信息失败");
                alert.setContentText("文件读取错误: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) { // 明确指定类名
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) { // 使用导入的 DateUtil 类
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
}
