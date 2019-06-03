import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.sql.*;
import java.util.*;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!


public class stockRobot
{


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(), endTime;
        boolean sufficientBalance = true;







        while(sufficientBalance)
        {
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
                startTime = System.currentTimeMillis();
                //System.out.println("Starttime: " + startTime);

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

            String user, ticker;
            double value = 0.0;

            try {
                stmt = conn.createStatement();
                stmt2 = conn.createStatement();
                stmt3 = conn.createStatement();
                stmt.execute("USE cs480fa2018;");
                stmt2.execute("USE cs480fa2018;");
                stmt3.execute("USE cs480fa2018;");
                user = args[0];
                ticker = args[1];
                value = Double.parseDouble(args[2]);

                user_rs = stmt2.executeQuery("SELECT * FROM Person WHERE AccountName = " + "\"" + user + "\"" + ";");


                String query = "SELECT * " +
                        "FROM SellOrder " +
                        "INNER JOIN CompanyInfo ON SellOrder.CompanyID = CompanyInfo.CompanyID;";

                rs = stmt.executeQuery(query);


                if (rs != null)
                {
                    rs.absolute(1);
                    while(rs.next())
                    {

                            System.out.println("Available " + rs.getString("TransactionID") + " " +
                                    rs.getString("Quantity") + " shares of " + rs.getString("TickerName") + " at $"  +
                                    rs.getString("Price") + " per share");
                            //break;
                        //}
                    }


                    double totalPrice = 0.0, curBalance = 0.0, price = 0.0;
                    int rowsUpdated = 0, invalidSales = 0, totalStockOfSeller = 0, remainingStockOfSeller = 0;
                    boolean lowBuyerFunds = false, lowSellerStock = false;

                    rs.absolute(1);
                    while (rs.next())
                    {
                        if (ticker.equalsIgnoreCase(rs.getString("TickerName")) && value == Double.parseDouble(rs.getString("Price"))) {

                            totalPrice += Double.parseDouble(rs.getString("Quantity")) * Double.parseDouble(rs.getString("Price"));
                            user_rs.absolute(1);
                            curBalance = Double.parseDouble(user_rs.getString("Balance"));
                            //System.out.println("CURBALANCE: " + curBalance);

                            if (curBalance < totalPrice) {
                                //invalidSales++;
                                sufficientBalance = false;
                            }


                            buyUser_rs = stmt3.executeQuery("SELECT SUM(Quantity) AS TotalStock " +
                                    "From Stock " +
                                    "WHERE CompanyID = " + rs.getString("CompanyID") +
                                    " AND AccountID = " + rs.getString("AccountID") +
                                    " GROUP BY AccountID " + ";");
                            buyUser_rs.absolute(1);
                            if(buyUser_rs.next())
                                totalStockOfSeller = Integer.parseInt(buyUser_rs.getString("TotalStock"));
                            remainingStockOfSeller = totalStockOfSeller - Integer.parseInt(rs.getString("Quantity"));
                            if(remainingStockOfSeller < 0)
                            {
//                                invalidSales++;
//                                lowSellerStock = true;
                                sufficientBalance = false;
                            }

                            if (sufficientBalance) {
                                Double newBalance;

                                newBalance = curBalance - totalPrice;
                                rowsUpdated = stmt3.executeUpdate("UPDATE Person " +
                                        "SET Balance = " + newBalance + " WHERE AccountName = " + "\"" + user + "\"" + ";");
                                //System.out.println("Done SET Balance");
                                if (rowsUpdated > 0) {
                                    //if(option1)
                                    rowsUpdated = stmt3.executeUpdate("INSERT INTO Stock (CompanyID, AccountID, Quantity) " +
                                            "VALUES(" + rs.getString("CompanyID") + ","
                                            + user_rs.getString("AccountID") + "," + rs.getString("Quantity") + ");");
                                    //System.out.println("Done INSERT");
                                }

                                if (rowsUpdated > 0) {
                                    rowsUpdated = stmt3.executeUpdate("DELETE FROM SellOrder WHERE TransactionID = " + rs.getString("TransactionID") + ";");
                                    //System.out.println("Done DELETE");

                                }


                                rowsUpdated = stmt3.executeUpdate("DELETE " +
                                                    "From Stock " +
                                                    "WHERE CompanyID = " + rs.getString("CompanyID") +
                                                    " AND AccountID = " + rs.getString("AccountID") + ";");
                                            rowsUpdated = stmt3.executeUpdate("INSERT INTO Stock (CompanyID, AccountID, Quantity) VALUES(" + rs.getString("CompanyID") + ","
                                                    + rs.getString("AccountID") + "," + remainingStockOfSeller + ");");





                                System.out.println("Purchased lot " + rs.getString("TransactionID") + " " +
                                        rs.getString("Quantity") + " shares of " + rs.getString("TickerName") + " at $"  +
                                        rs.getString("Price") + " per share");
                                System.out.println("Remaining funds:" + newBalance);
                            }
                            //invalidSales = 0;
                            else
                            {
                                System.out.println("Terminating program due to lack of funds.");
                                return;
                            }

                        }
                    }


                }
            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }

            endTime = System.currentTimeMillis() - startTime;
            //endTime = endTime / (long)1000;
            if (endTime >= (long)0 && sufficientBalance) {
                try {
                    Thread.sleep(60000 - endTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}