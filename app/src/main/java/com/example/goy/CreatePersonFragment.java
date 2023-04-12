package com.example.goy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CreatePersonFragment extends DialogFragment {

    private EditText nameEdit, surnameEdit, ibanEdit, bicEdit, bankEdit;

    private OnPersonCreateClickedListener onPersonCreateClickedListener;

    public interface OnPersonCreateClickedListener{
        void onPersonCreateClicked(String name, String surname, String iban, String bic, String bank);
    }

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


        btnCancel.setOnClickListener(view1 -> {
            dismiss();
        });

        btnSave.setOnClickListener(view1 -> {
            if(validateInput()){
                String name = nameEdit.getText().toString();
                String surname = nameEdit.getText().toString();
                String iban = ibanEdit.getText().toString().isEmpty() ? null : ibanEdit.getText().toString();
                String bic = bicEdit.getText().toString().isEmpty() ? null : bicEdit.getText().toString();
                String bank = bankEdit.getText().toString().isEmpty() ? null : bankEdit.getText().toString();
                onPersonCreateClickedListener.onPersonCreateClicked(name, surname, iban, bic, bank);
                dismiss();
            }
        });

        return view;
    }

    private boolean validateInput(){
        if(nameEdit.getText().toString().isEmpty() || surnameEdit.getText().toString().isEmpty())
            return false;

        if(ibanEdit.getText().length() != 22)
            return false;

        if(!bicEdit.getText().toString().isEmpty() && (bicEdit.getText().length() != 8 && bicEdit.getText().length() != 11)){
            return false;
        }

        return true;
    }

    public void setOnPersonCreateClickedListener(OnPersonCreateClickedListener onPersonCreateClickedListener){
        this.onPersonCreateClickedListener = onPersonCreateClickedListener;
    }
}
