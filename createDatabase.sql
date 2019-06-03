Create database IF not exists StockMarket;
USE StockMarket

Drop table Stock;
Create table if not exists Stock(
    TITLE VARCHAR(255) NOT NULL,
    sID INT NOT NULL,
    STATUS VARCHAR(255) NOT NULL,
    PRIMARY KEY(sID) 
);

Drop table BuyStock;
Create table if not exists BuyStock(
    ID INT NOT NULL,
    QUANTITY INT NOT NULL,
    sID INT NOT NULL,
    BUY_AMOUNT FLOAT NOT NULL,
    BSTATUS VARCHAR(255) NOT NULL,
    ORDER_DATE DATE NOT NULL,
    PRIMARY KEY(ID, sID, ORDER_DATE) 
);

Drop table SellStock;
Create table if not exists SellStock(
    ID INT NOT NULL,
    QUANTITY INT NOT NULL,
    sID INT NOT NULL,
    SELL_AMOUNT FLOAT NOT NULL,
    SSTATUS VARCHAR(255) NOT NULL,
    ORDER_DATE DATE NOT NULL,
    PRIMARY KEY(ID, sID, ORDER_DATE) 
);

Drop table Customer;
Create table if not exists Customer(
    ID INT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
);   

Drop table History;
Create table if not exists History(
    ID INT NOT NULL,
    QUANTITY INT NOT NULL,
    sID INT NOT NULL,
    AMOUNT FLOAT NOT NULL,
    DATE_OF_TRANSACTION DATE NOT NULL,
    TYPE VARCHAR(255) NOT NULL,
    PRIMARY KEY(ID, QUANTITY, sID, DATE_OF_TRANSACTION, TYPE)
);

Drop table Account;
Create table if not exists Account(
    ID INT NOT NULL,
    TOTAL_FUNDS FLOAT NOT NULL,
    AVAILABLE_FUNDS FLOAT NOT NULL,
    DATE_UPDATED DATE NOT NULL,
    PRIMARY KEY(ID)
);

