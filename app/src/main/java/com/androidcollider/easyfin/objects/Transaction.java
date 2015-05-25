package com.androidcollider.easyfin.objects;


public class Transaction {

    private String date;
    private int id_account;
    private double amount;
    private String category;

    private String account_name;
    private String account_currency;


    public Transaction (String date, int id_account, double amount, String category) {
        this.date = date;
        this.id_account = id_account;
        this.amount = amount;
        this.category = category;
    }

    public Transaction (String date, double amount, String category, String account_name, String account_currency) {
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.account_name = account_name;
        this.account_currency = account_currency;
    }

    public String getDate() {return date;}

    public int getId_account() {return id_account;}

    public double getAmount() {return amount;}

    public String getCategory() {return category;}

    public String getAccount_name() {return account_name;}

    public String getAccount_currency() {return account_currency;}
}
