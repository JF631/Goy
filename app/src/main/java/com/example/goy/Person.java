package com.example.goy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Person {
    private final String name, surname, iban, bic, bank;

    public Person(@NonNull String name, @NonNull String surname, @Nullable String iban, @Nullable String bic, @Nullable String bank){
        this.name = name;
        this.surname = surname;
        this.iban = iban;
        this.bic = bic;
        this.bank = bank;
    }

    public String getName(){return name;}
    public String getSurname(){return surname;}
    public String getIban(){return iban;}
    public String getBic(){return bic;}
    public String getBank(){return bank;}


}
