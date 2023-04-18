package com.example.goy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

public class CreatePersonFragment extends DialogFragment {

    private EditText nameEdit, surnameEdit, ibanEdit, bicEdit, bankEdit;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.get_personal_data_view, container, false);
        nameEdit = view.findViewById(R.id.create_name);
        surnameEdit = view.findViewById(R.id.create_surname);
        ibanEdit = view.findViewById(R.id.create_iban);
        bicEdit = view.findViewById(R.id.create_bic);
        bankEdit = view.findViewById(R.id.create_bank);
        Button btnSave = view.findViewById(R.id.create_save);
        Button btnCancel = view.findViewById(R.id.create_cancel);

        Person currentData = getCurrentData();
        if(!currentData.getName().isEmpty())nameEdit.setText(decryptString(currentData.getName()));
        if(!currentData.getSurname().isEmpty())surnameEdit.setText(decryptString(currentData.getSurname()));
        if(!currentData.getIban().isEmpty()) ibanEdit.setText(decryptString(currentData.getIban()));
        if(!currentData.getBic().isEmpty())bicEdit.setText(decryptString(currentData.getBic()));
        if(!currentData.getBank().isEmpty())bankEdit.setText(decryptString(currentData.getBank()));

        btnCancel.setOnClickListener(view1 -> dismiss());

        btnSave.setOnClickListener(view1 -> {
            if(validateInput()){
                String name = nameEdit.getText().toString();
                String surname = surnameEdit.getText().toString();
                String iban = ibanEdit.getText().toString().isEmpty() ? null : ibanEdit.getText().toString();
                String bic = bicEdit.getText().toString().isEmpty() ? null : bicEdit.getText().toString();
                String bank = bankEdit.getText().toString().isEmpty() ? null : bankEdit.getText().toString();
                try {
                    saveData(name, surname, iban, bic, bank);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                dismiss();
            }
        });

        return view;
    }

    private boolean validateInput(){
        if(nameEdit.getText().toString().isEmpty() || surnameEdit.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Vor- und Nachnamen eintragen", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(ibanEdit.getText().length() != 22) {
            Toast.makeText(requireContext(), "IBAN überprüfen", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!bicEdit.getText().toString().isEmpty() && bicEdit.getText().length() != 8 && bicEdit.getText().length() != 11){
            Toast.makeText(requireContext(), "BIC überprüfen (kann auch leer sein)", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Person getCurrentData(){
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String surname = sharedPreferences.getString("surname", "");
        String iban = sharedPreferences.getString("iban", "");
        String bic = sharedPreferences.getString("bic", "");
        String bank = sharedPreferences.getString("bank", "");

        return new Person(name, surname, iban, bic, bank);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveData(String name, String surname, String iban, String bic, String bank) throws Exception {
        SharedPreferences sharedPreferences  = requireContext().getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", Utilities.encryptString(name));
        editor.putString("surname", Utilities.encryptString(surname));
        if(iban != null) {editor.putString("iban", Utilities.encryptString(iban));}
        if(bic != null) editor.putString("bic", Utilities.encryptString(bic));
        if(bank != null) editor.putString("bank", Utilities.encryptString(bank));
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String decryptString(String toDecrypt){
        try {
            return Utilities.decryptToString(toDecrypt);
        } catch (Exception e) {
            Log.e("Create Person", String.valueOf(e));
            return toDecrypt;
        }
    }
}
