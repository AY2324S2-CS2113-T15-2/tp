package budgetbuddy;

import budgetbuddy.account.Account;
import budgetbuddy.account.AccountManager;
import budgetbuddy.exceptions.EmptyArgumentException;
import budgetbuddy.exceptions.InvalidAddTransactionSyntax;
import budgetbuddy.exceptions.InvalidArgumentSyntaxException;
import budgetbuddy.exceptions.InvalidCategoryException;
import budgetbuddy.exceptions.InvalidEditTransactionData;
import budgetbuddy.exceptions.InvalidIndexException;
import budgetbuddy.exceptions.InvalidTransactionTypeException;
import budgetbuddy.insights.Insight;
import budgetbuddy.parser.Parser;
import budgetbuddy.storage.DataStorage;
import budgetbuddy.transaction.TransactionList;
import budgetbuddy.ui.UserInterface;

import java.io.File;
import java.io.IOException;

import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BudgetBuddy {
    public static final int LIST_LENGTH = 5;
    public static final String BYE = "bye";
    public static final String LIST = "list";
    public static final String DELETE = "delete";
    public static final String ADD = "add";
    public static final String EDIT = "edit";
    public static final String HELP = "help";
    public static final String ADD_ACC = "add-acc";
    public static final String INSIGHTS = "insights";
    public static final String LIST_ACC = "list-acc";
    public static final String DELETE_ACC = "delete-acc";
    public static final String EDIT_ACC = "edit-acc";
    public static final String SEARCH = "search";
    public static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final AccountManager accountManager;
    private final TransactionList transactions;

    /**
     * Creates a BudgetBuddy object with the account manager and transaction list.
     */
    public BudgetBuddy() {
        DataStorage dataStorage = new DataStorage();
        this.accountManager = dataStorage.loadAccounts();
        this.transactions = dataStorage.loadTransactions(accountManager.getExistingAccountNumbers());
    }

    /**
     * Sets up the logger for the BudgetBuddy application.
     */
    private static void setupLogger() {
        LogManager.getLogManager().reset();
        LOGGER.setLevel(java.util.logging.Level.ALL);
        try {
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }
            FileHandler fh = new FileHandler("logs/budgetBuggyLog.log");
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.INFO);
            LOGGER.addHandler(fh);
        } catch (IOException e) {
            UserInterface.printLoggerSetupError();
        }
    }

    /**
     * Main entry-point for the java.BudgetBuddy application.
     */
    public static void main(String[] args) {
        setupLogger();
        new BudgetBuddy().run();
    }

    /**
     * Runs the BudgetBuddy application.
     */
    public void run() {
        Scanner in = UserInterface.in;
        String logo = "BUDGET BUDDY";

        System.out.println("Hello from\n" + logo);
        System.out.println("What can I do for you?");


        boolean isRunning = true;

        while (isRunning) {
            String input = in.nextLine();
            try {
                if (input.contains(",")){
                    throw new InvalidArgumentSyntaxException("Input cannot contain ',' comma.");
                }
                switch (input.split(" ")[0].toLowerCase()) {
                case BYE:
                    Insight.closeInsightFrames();
                    UserInterface.printGoodBye();
                    isRunning = false;
                    break;
                case LIST:
                    transactions.processList(accountManager.getAccounts(), accountManager);
                    break;
                case DELETE:
                    transactions.removeTransaction(input, accountManager);
                    break;
                case ADD:
                    int accountNumber = Parser.parseAccountNumber(input);
                    Account account = accountManager.getAccountByAccountNumber(accountNumber);
                    transactions.processTransaction(input, account);
                    break;
                case EDIT:
                    transactions.processEditTransaction(input, accountManager);
                    break;
                case HELP:
                    transactions.helpWithUserCommands(input);
                    break;
                case ADD_ACC:
                    accountManager.processAddAccount(input);
                    break;
                case INSIGHTS:
                    transactions.displayInsights();
                    break;
                case LIST_ACC:
                    UserInterface.printListOfAccounts(accountManager.getAccounts());
                    break;
                case DELETE_ACC:
                    accountManager.removeAccount(input, transactions);
                    break;
                case EDIT_ACC:
                    accountManager.processEditAccount(input);
                    break;
                case SEARCH:
                    transactions.searchTransactions(input);
                    break;
                default:
                    UserInterface.printNoCommandExists();
                }
                transactions.saveTransactionList();
                accountManager.saveAccounts();

            } catch (InvalidAddTransactionSyntax e) {
                UserInterface.printInvalidAddSyntax(e.getMessage());
            } catch (NumberFormatException e) {
                UserInterface.printNumberFormatError(e.getMessage());
            } catch (InvalidTransactionTypeException e) {
                UserInterface.printTransactionTypeError(e.getMessage());
            } catch (EmptyArgumentException e) {
                UserInterface.printEmptyArgumentError(e.getMessage());
            } catch (InvalidIndexException e) {
                UserInterface.printInvalidIndex("Given index id is out of bound",
                        Integer.parseInt(e.getMessage()));
            } catch (IndexOutOfBoundsException ignored) {
                UserInterface.printInvalidInput("Please check your command syntax");
            } catch (InvalidEditTransactionData e) {
                UserInterface.printInvalidInput(e.getMessage());
            } catch (InvalidArgumentSyntaxException e) {
                UserInterface.printInvalidArgumentSyntax(e.getMessage());
            } catch (InvalidCategoryException e) {
                UserInterface.printInvalidCategoryError();
            } catch (Exception e) {
                UserInterface.printExceptionErrorMessage(e.getMessage());
            }
        }

    }
}
