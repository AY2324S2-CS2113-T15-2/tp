package budgetbuddy.transaction;

import budgetbuddy.account.Account;

import budgetbuddy.account.AccountManager;
import budgetbuddy.exceptions.EmptyArgumentException;
import budgetbuddy.exceptions.InvalidAddTransactionSyntax;
import budgetbuddy.exceptions.InvalidIndexException;
import budgetbuddy.exceptions.InvalidTransactionTypeException;
import budgetbuddy.exceptions.InvalidEditTransactionData;

import budgetbuddy.categories.Category;
import budgetbuddy.insights.Insight;
import budgetbuddy.parser.Parser;
import budgetbuddy.storage.DataStorage;
import budgetbuddy.transaction.type.Transaction;
import budgetbuddy.ui.UserInterface;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TransactionList {

    public static final int DELETE_BEGIN_INDEX = 7;
    public static final int INDEX_OFFSET = 1;
    public static final int LOWER_BOUND = 0;
    public static final int EDIT_BEGIN_INDEX = 5;

    public static final String ALL = "all";
    public static final String ADD = "add";
    public static final String DELETE = "delete";
    public static final String EDIT = "edit";
    public static final String LIST = "list";
    private static final int DAYS_IN_WEEK = 7;
    private static final int DAYS_IN_MONTH = 30;
    private static final int DAYS_OFFSET = 1;

    private final ArrayList<Transaction> transactions;
    private final Parser parser;

    private final DataStorage dataStorage = new DataStorage();

    public TransactionList() throws IOException {
        // Initialise ArrayList in the constructor
        this.transactions = dataStorage.readFileContents();
        assert transactions != null : "Transaction list is null after reading from file";
        this.parser = new Parser();
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void printTransactions(Account account) {
        UserInterface.printAllTransactions(transactions, account.getBalance());
    }

    public void removeTransaction(String input, AccountManager accountManager) throws EmptyArgumentException,
            NumberFormatException, InvalidIndexException {
        if (input.trim().length() < DELETE_BEGIN_INDEX) {
            throw new EmptyArgumentException("delete index");
        }
        String data = input.substring(DELETE_BEGIN_INDEX).trim();
        if (isNotInteger(data)) {
            throw new NumberFormatException(data);
        }
        int id = Integer.parseInt(data) - INDEX_OFFSET;
        int size = transactions.size();
        if (id >= LOWER_BOUND && id < size) {
            String itemRemoved = transactions.get(id).toString();
            Account account = accountManager.getAccountByAccountNumber(transactions.get(id).getAccountNumber());
            assert itemRemoved != null : "String representation of item to remove is null";
            account.setBalance(account.getBalance() - transactions.get(id).getAmount());
            transactions.remove(id);
            assert transactions.size() == size - 1 : "Transaction list size did not decrease after removal";
            UserInterface.printDeleteMessage(itemRemoved, account.getBalance());
        } else {
            throw new InvalidIndexException(String.valueOf(size));
        }
    }

    public static boolean isNotInteger(String data) {
        try {
            Integer.parseInt(data);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static boolean isNotDouble(String data) {
        try {
            Double.parseDouble(data);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public void processTransaction(String input, Account account)
            throws InvalidTransactionTypeException, InvalidAddTransactionSyntax, EmptyArgumentException {
        // Check for syntax for add transaction
        String[] arguments = {"/a/","/t/", "/n/", "/$/", "/d/"};
        for (String argument : arguments) {
            if (!input.contains(argument)) {
                throw new InvalidAddTransactionSyntax("Invalid add syntax.");
            }
        }

        Transaction t = parser.parseTransaction(input, account);
        assert t != null : "Parsed transaction is null";
        if (t.getCategory() == null) {
            UserInterface.listCategories();
            int category = UserInterface.getCategoryNum();
            t.setCategory(Category.fromNumber(category));
        }
        addTransaction(t);
        assert transactions.get(transactions.size() - 1) != null : "Added transaction is null after adding to the list";
        String fetchData = String.valueOf(transactions.get(transactions.size() - 1));
        UserInterface.printAddMessage(fetchData, account.getBalance());
    }

    public void saveTransactionList() throws IOException {
        dataStorage.saveTransactions(transactions);
    }

    public void updateBalance(Account account) {
        account.setBalance(dataStorage.getBalance());
    }

    //@@author isaaceng7
    public static ArrayList<Transaction> getPastWeekTransactions(ArrayList<Transaction> transactions) {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(DAYS_IN_WEEK + DAYS_OFFSET);
        ArrayList<Transaction> pastWeekTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getDate().isAfter(lastWeek)) {
                pastWeekTransactions.add(transaction);
            }
        }
        return pastWeekTransactions;
    }

    public static ArrayList<Transaction> getPastMonthTransactions(ArrayList<Transaction> transactions) {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusDays(DAYS_IN_MONTH + DAYS_OFFSET);
        ArrayList<Transaction> pastWeekTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getDate().isAfter(lastMonth)) {
                pastWeekTransactions.add(transaction);
            }
        }
        return pastWeekTransactions;
    }

    public static ArrayList<Transaction> getCustomDateTransactions(ArrayList<Transaction> transactions) {
        String start = UserInterface.getStartDate();
        String end = UserInterface.getEndDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDate = LocalDate.parse(start, formatter).minusDays(DAYS_OFFSET);
        LocalDate endDate = LocalDate.parse(end, formatter).plusDays(DAYS_OFFSET);
        ArrayList<Transaction> customDateTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getDate().isAfter(startDate) && transaction.getDate().isBefore(endDate)) {
                customDateTransactions.add(transaction);
            }
        }
        return customDateTransactions;
    }


    public void processList(Account account) throws InvalidIndexException {
        UserInterface.printListOptions();
        String data = UserInterface.getListOption().trim();
        int option = Integer.parseInt(data);
        switch (option) {
        // 1 - ALL TRANSACTIONS
        case 1:
            printTransactions(account);
            break;
        // 2 - PAST WEEK TRANSACTIONS
        case 2:
            ArrayList<Transaction> pastWeekTransactions = getPastWeekTransactions(transactions);
            UserInterface.printPastWeekTransactions(pastWeekTransactions);
            break;
        // 3 - PAST MONTH TRANSACTIONS
        case 3:
            ArrayList<Transaction> pastMonthTransactions = getPastMonthTransactions(transactions);
            UserInterface.printPastMonthTransactions(pastMonthTransactions);
            break;
        // 4 - CUSTOM DATE TRANSACTIONS
        case 4:
            ArrayList<Transaction> customDateTransactions = getCustomDateTransactions(transactions);
            UserInterface.printCustomDateTransactions(customDateTransactions);
            break;

        default:
            throw new InvalidIndexException("4");
        }

    }

    //@@author Vavinan
    public void processEditTransaction(String input, AccountManager accountManager) throws EmptyArgumentException,
            NumberFormatException, InvalidIndexException, InvalidEditTransactionData {
        if (input.trim().length() < EDIT_BEGIN_INDEX) {
            throw new EmptyArgumentException("edit index ");
        }
        String data = input.substring(EDIT_BEGIN_INDEX).trim();

        if (isNotInteger(data)) {
            throw new NumberFormatException(data);
        }
        int index = Integer.parseInt(data) - INDEX_OFFSET;
        if ((index >= LOWER_BOUND) && (index < transactions.size())) {
            Transaction transaction = transactions.get(index);
            Account account = accountManager.getAccountByAccountNumber(transaction.getAccountNumber());
            String newTransaction = UserInterface.getEditInformation(transaction.toString());
            Transaction t = parser.parseTransactionType(newTransaction, account);
            transactions.set(index, t);
            UserInterface.printUpdatedTransaction();
        } else {
            throw new InvalidIndexException(String.valueOf(transactions.size()));
        }
    }

    public void helpWithUserCommands(String input){
        String helpCommand = parser.parseHelpCommand(input);
        switch(helpCommand){
        case ALL:
            UserInterface.printAllCommands();
            break;
        case ADD:
            UserInterface.printAddHelp();
            break;
        case DELETE:
            UserInterface.printDeleteHelp();
            break;
        case EDIT:
            UserInterface.printEditHelp();
            break;
        case LIST:
            UserInterface.printListHelp();
            break;
        default:
            UserInterface.printUseAvailableHelp();
            break;
        }
    }
    //@@author
    public void displayInsights() {
        Insight.displayCategoryInsight(transactions);
    }
}
