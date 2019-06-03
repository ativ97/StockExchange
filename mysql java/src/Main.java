import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.sql.*;
import java.util.*;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!





public class Main {

    public static void printResultSet(ResultSet rs) throws SQLException
    {
        if (rs!=null)
        {
            int columnsNumber = rs.getMetaData().getColumnCount();
            while (rs.next())
            {
                for (int i = 1; i <= columnsNumber; i++)
                {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rs.getMetaData().getColumnName(i));
                }
                System.out.println("");
            }
        }
    }




    public static void main(String[] args) {
        Connection conn = null;
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            // handle the error
            ex.printStackTrace();
        }

        try {
            conn =
                    DriverManager.getConnection(
                            "jdbc:mysql://cs480fa2018.mysql.uic.edu:3306?" +
                                    "user=student&password=a");

            // Do something with the Connection

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        Statement stmt = null;
        Statement stmt2 = null;
        Statement stmt3 = null;
        ResultSet rs = null, user_rs = null, buyUser_rs = null;
        Scanner sc = new Scanner(System.in);
        boolean transactionComplete = false;
        int checkValidUser;




        while(!transactionComplete)
        {
            System.out.print("Which user are you? ");
            String user = sc.nextLine();
            long dong = System.currentTimeMillis();

//            for(int k = 0; k<10000000; k++)
//            {
//                System.out.println((System.currentTimeMillis()-dong)/(long)1000);
//            }

            System.out.print("Are you buying or selling (1 to buy, 2 to sell)? ");
            String option = sc.nextLine();
            if(!option.equals("1") && !option.equals("2"))
            {
                System.out.println("Invalid option");
                System.out.println();
                continue;
            }


            try {
                stmt = conn.createStatement();
                stmt2 = conn.createStatement();
                stmt3 = conn.createStatement();
                stmt.execute("USE cs480fa2018;");
                stmt2.execute("USE cs480fa2018;");
                stmt3.execute("USE cs480fa2018;");
                user_rs = stmt2.executeQuery("SELECT * FROM Person WHERE AccountName = " + "\"" + user + "\"" + ";");
                if(option.equals("1"))
                {
                    String query = "SELECT *" +
                            "FROM SellOrder " +
                            "INNER JOIN CompanyInfo ON SellOrder.CompanyID = CompanyInfo.CompanyID;";

                    rs = stmt.executeQuery(query);
                }
                else if(option.equals("2"))
                {
                    rs = stmt.executeQuery("SELECT * " +
                            "FROM BuyOrder " +
                            "INNER JOIN CompanyInfo ON BuyOrder.CompanyID = CompanyInfo.CompanyID;");
                }

                //rs = stmt.executeQuery("SHOW Databases;");
                //rs = stmt.executeQuery("SHOW Tables;");
                //rs = stmt.executeQuery("SELECT * FROM Stock;");

                //rs = stmt.executeQuery("DESCRIBE test;");

                if (rs!=null)
                {
                    ArrayList<String> selectedStocks = new ArrayList<>();
                    String selectedStock = "";
                    while(!selectedStock.equals("0"))
                    {
                        if(option.equals("1"))
                            System.out.println("Here are the currently available lots to buy from:");
                        else
                            System.out.println("Here are the currently available lots to sell to:");

                        rs.absolute(1);
                        while(rs.next())
                        {
                            if(!selectedStocks.contains(rs.getString("TransactionID")))
                            {
                                System.out.println(rs.getString("TransactionID") + " " + rs.getString("TickerName") + " " +
                                        rs.getString("Quantity") + " at $" + rs.getString("Price") + " a share");
                            }

                        }

                        if(option.equals("1"))
                            System.out.print("Which lot would you like to purchase (0 to exit)?" );
                        else
                            System.out.print("Which lot would you like to sell to (0 to exit)?" );
                        selectedStock = sc.nextLine();
                        //System.out.print("ID: " + rs.getString("TransactionID"));

                        rs.absolute(1);
                        while(rs.next())
                        {
                            if(rs.getString("TransactionID").equals(selectedStock))
                            {
                                selectedStocks.add(selectedStock);
                            }
                        }
                    }


                    double totalPrice = 0.0, curBalance = 0.0, price = 0.0;
                    int rowsUpdated = 0, invalidSales = 0, totalStockOfSeller = 0, remainingStockOfSeller = 0;
                    boolean lowBuyerFunds = false, lowSellerStock = false;
                    String confirmPurchase = "n";
                    ArrayList<String> leftWith = new ArrayList<>();

                    if(option.equals("1"))
                        System.out.println("You are purchasing: ");
                    else
                        System.out.println("You are selling: ");
                    rs.absolute(1);
                    while(rs.next())
                    {
                        if(selectedStocks.contains(rs.getString("TransactionID")))
                        {
                            System.out.println(rs.getString("TransactionID") + " " + rs.getString("TickerName") + " " +
                                    rs.getString("Quantity") + " at $" + rs.getString("Price") + " a share");

                            if(option.equals("1"))
                            {
                                totalPrice += Double.parseDouble(rs.getString("Quantity")) * Double.parseDouble(rs.getString("Price"));
                                user_rs.absolute(1);
                                curBalance = Double.parseDouble(user_rs.getString("Balance"));

                                if(curBalance < totalPrice)
                                {
                                    invalidSales++;
                                }

                            }
                            else
                            {
                                price = Double.parseDouble(rs.getString("Quantity")) * Double.parseDouble(rs.getString("Price"));
                                totalPrice += price;
                                buyUser_rs = stmt3.executeQuery("SELECT Balance " +
                                        "FROM BuyOrder " +
                                        "INNER JOIN Person ON BuyOrder.AccountID = Person.AccountID " +
                                        "WHERE BuyOrder.AccountID = " + rs.getString("AccountID") + ";");
                                if(buyUser_rs.next())
                                    curBalance = Double.parseDouble(buyUser_rs.getString("Balance"));
                                if(price > curBalance)
                                {
                                    invalidSales++;
                                    lowBuyerFunds = true;
                                }

                                //Check if seller has enough stock to sell
                                user_rs.absolute(1);
                                buyUser_rs = stmt3.executeQuery("SELECT SUM(Quantity) AS TotalStock " +
                                        "From Stock " +
                                        "WHERE CompanyID = " + rs.getString("CompanyID") +
                                        " AND AccountID = " + user_rs.getString("AccountID") +
                                        " GROUP BY AccountID " + ";");
                                buyUser_rs.absolute(1);
                                if(buyUser_rs.next())
                                    totalStockOfSeller = Integer.parseInt(buyUser_rs.getString("TotalStock"));
                                remainingStockOfSeller = totalStockOfSeller - Integer.parseInt(rs.getString("Quantity"));
                                if(remainingStockOfSeller < 0)
                                {
                                    invalidSales++;
                                    lowSellerStock = true;
                                }
                                buyUser_rs = stmt3.executeQuery("SELECT * " +
                                        "FROM BuyOrder " +
                                        "INNER JOIN CompanyInfo ON BuyOrder.CompanyID = CompanyInfo.CompanyID " +
                                        "WHERE BuyOrder.CompanyID = " + rs.getString("CompanyID") + ";");
                                if(buyUser_rs.next())
                                {
                                    leftWith.add(buyUser_rs.getString("TickerName") + " " + Integer.toString(remainingStockOfSeller) + " shares");
                                }
                            }
                        }
                    }

                    if(option.equals("1"))
                        System.out.print("Your total comes to $" + totalPrice + " Confirm purchase (y/n): ");
                    else
                    {
                        System.out.println("This will leave you with");
                        for(String lw: leftWith)
                        {
                            System.out.println(lw);
                        }
                        System.out.print("Confirm purchase (y/n): ");
                    }
                    confirmPurchase = sc.nextLine();
                    if(confirmPurchase.equalsIgnoreCase("y"))
                    {

                        boolean option1 = false, option2 = false;

                        if(option.equals("1"))
                        {
                            option1 = true;
                            option2 = false;
                        }
                        else
                        {
                            option1 = false;
                            option2 = true;
                        }

                        if((option1 || option2) && invalidSales == 0)
                        {
                            Double newBalance;
                            if(option1)
                                newBalance = curBalance - totalPrice;
                            else
                                newBalance = curBalance + totalPrice;

                            rowsUpdated = stmt3.executeUpdate("UPDATE Person " +
                                    "SET Balance = " + newBalance + " WHERE AccountName = " + "\"" + user + "\"" + ";");
                            rs.absolute(1);
                            while(rs.next())
                            {
                                if(selectedStocks.contains(rs.getString("TransactionID")))
                                {
                                    if(rowsUpdated == 0) //should be > 0
                                    {
                                        if(option1)
                                            rowsUpdated = stmt3.executeUpdate("INSERT INTO Stock (CompanyID, AccountID, Quantity) " +
                                                    "VALUES(" + rs.getString("CompanyID") + ","
                                                    + user_rs.getString("AccountID") + "," + rs.getString("Quantity") + ");");
                                    }

                                    if(rowsUpdated > 0)
                                    {
                                        for(String tb: selectedStocks)
                                        {
                                            //System.out.print("DELETE FROM SellOrder WHERE TransactionID = " + tb + ";");
                                            if(option1)
                                                rowsUpdated = stmt3.executeUpdate("DELETE FROM SellOrder WHERE TransactionID = " + tb + ";");
                                            else
                                                rowsUpdated = stmt3.executeUpdate("DELETE FROM BuyOrder WHERE TransactionID = " + tb + ";");
                                        }
                                        if(option2)
                                        {
                                            rowsUpdated = stmt3.executeUpdate("DELETE " +
                                                    "From Stock " +
                                                    "WHERE CompanyID = " + rs.getString("CompanyID") +
                                                    " AND AccountID = " + user_rs.getString("AccountID") + ";");
                                            rowsUpdated = stmt3.executeUpdate("INSERT INTO Stock (CompanyID, AccountID, Quantity) VALUES(" + rs.getString("CompanyID") + ","
                                                    + rs.getString("AccountID") + "," + remainingStockOfSeller + ");");
                                        }
                                    }
                                }
                            }
                            System.out.print("Transaction Completed. Your balance is now $" + newBalance);
                            transactionComplete = true;
                            //printResultSet(stmt3.executeQuery("SELECT * FROM SellOrder;"));
                        }
                        else
                        {
                            if(option1)
                            {
                                System.out.println("Insufficient funds to complete transaction, returning to main menu.");
                                System.out.println();
                            }
                            else
                            {
                                if(lowBuyerFunds)
                                {
                                    System.out.println("One or more buyers has insufficient funds to complete transaction, returning to main menu.");
                                    System.out.println();
                                }
                                else if(lowSellerStock)
                                {
                                    System.out.println("Seller has insufficient stock to complete transaction, returning to main menu.");
                                    System.out.println();
                                }
                            }
                        }
                    }
                    else
                    {
                        return;
                    }
                }
            }
            catch (SQLException ex){
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }

    }
}