package com.sharad.epocket.accounts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sharad.epocket.R;
import com.sharad.epocket.utils.Constant;
import com.sharad.epocket.utils.Utils;

import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

public class AddAccountActivity extends AppCompatActivity {
    EditText editTextCurrency;
    private long accountId = Constant.INVALID_ID;
    private Currency selectedCurrency = Currency.getInstance(Locale.getDefault());
    private IAccount iAccount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        Utils.setTaskDescription(this);

        Bundle extras = getIntent().getExtras();
        accountId = extras.getLong(Constant.ARG_ACCOUNT_NUMBER_LONG, Constant.INVALID_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextCurrency = (EditText) findViewById(R.id.account_currency);

        final ToggleButton btnCash = (ToggleButton) findViewById(R.id.account_button_cash);
        final ToggleButton btnCard = (ToggleButton) findViewById(R.id.account_button_card);

        btnCash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if(!isChecked && !btnCard.isChecked()) {
                    buttonView.setChecked(true);
                }
            }
        });

        btnCard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked && !btnCash.isChecked()) {
                    buttonView.setChecked(true);
                }
            }
        });

        final Button addField = (Button) findViewById(R.id.account_add_field);
        final View layoutDescription = findViewById(R.id.layout_account_description);
        final View layoutContact = findViewById(R.id.layout_account_contact);
        final View layoutLogin = findViewById(R.id.layout_account_login);
        final View layoutPassword = findViewById(R.id.layout_account_password);
        final View layoutAccountNumber = findViewById(R.id.layout_account_number);
        layoutDescription.setVisibility(View.GONE);
        layoutContact.setVisibility(View.GONE);
        layoutLogin.setVisibility(View.GONE);
        layoutPassword.setVisibility(View.GONE);
        layoutAccountNumber.setVisibility(View.GONE);
        addField.setVisibility(View.VISIBLE);

        final CharSequence[] fields = {
                "Description", "Account Number", "Login Id", "Password", "Contact Number" };
        final boolean[] checkedFields = {
                false, false, false, false, false };
        final View[] layoutFields = {
                layoutDescription, layoutAccountNumber, layoutLogin, layoutPassword, layoutContact };

        addField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AddAccountActivity.this)
                        .setTitle("Select Fields")
                        .setMultiChoiceItems(fields, checkedFields, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checkedFields[which] = isChecked;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i=0; i<layoutFields.length; i++) {
                                    layoutFields[i].setVisibility((checkedFields[i] ? View.VISIBLE : View.GONE));
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        editTextCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrencyPicker();
            }
        });

        EditText accountName = (EditText)findViewById(R.id.account_name);
        EditText accountNote = (EditText)findViewById(R.id.account_note);
        EditText accountLogin = (EditText)findViewById(R.id.account_login);
        EditText accountPassword = (EditText)findViewById(R.id.account_password);
        EditText accountNumber = (EditText)findViewById(R.id.account_number);
        EditText accountContact = (EditText)findViewById(R.id.account_contact);
        if(accountId != Constant.INVALID_ID) {
            DataSourceAccount source = new DataSourceAccount(this);
            iAccount = source.getAccount(accountId);
            if(iAccount != null) {
                btnCash.setChecked(iAccount.hasCashAccount());
                btnCard.setChecked(iAccount.hasCardAccount());

                accountName.setText(iAccount.getTitle());
                if(iAccount.getNote().length() > 0) {
                    accountNote.setText(iAccount.getNote());
                    layoutDescription.setVisibility(View.VISIBLE);
                    checkedFields[0] = true;
                }
                if(iAccount.getAccountNumber().length() > 0) {
                    accountNumber.setText(iAccount.getAccountNumber());
                    layoutAccountNumber.setVisibility(View.VISIBLE);
                    checkedFields[1] = true;
                }
                if(iAccount.getLoginId().length() > 0) {
                    accountLogin.setText(iAccount.getLoginId());
                    layoutLogin.setVisibility(View.VISIBLE);
                    checkedFields[2] = true;
                }
                if(iAccount.getPassword().length() > 0) {
                    accountPassword.setText(iAccount.getPassword());
                    layoutPassword.setVisibility(View.VISIBLE);
                    checkedFields[3] = true;
                }
                if(iAccount.getContact().length() > 0) {
                    accountContact.setText(iAccount.getContact());
                    layoutContact.setVisibility(View.VISIBLE);
                    checkedFields[4] = true;
                }
                selectedCurrency = Currency.getInstance(iAccount.getIsoCurrency());
            } else {
                iAccount = new IAccount();
            }
        } else {
            iAccount = new IAccount();
            accountName.setText("");
            accountNote.setText("");
            accountLogin.setText("");
            accountPassword.setText("");
            accountNumber.setText("");
            accountContact.setText("");
        }
        editTextCurrency.setText(selectedCurrency.getSymbol() + " - " + selectedCurrency.getDisplayName());
    }

    private void showCurrencyPicker(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(Constant.DLG_CURRENCY_PICKER);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        CurrencyPickerDialog newFragment = CurrencyPickerDialog.newInstance();
        newFragment.show(ft, Constant.DLG_CURRENCY_PICKER);
        newFragment.setOnCurrencySelectedListener(new CurrencyPickerDialog.OnCurrencySelectedListener() {
            @Override
            public void onCurrencySelected(Currency currency) {
                selectedCurrency = currency;
                editTextCurrency.setText(currency.getSymbol() + " - " + currency.getDisplayName());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_account, menu);
        Utils.tintMenuIcon(this, menu.findItem(R.id.action_save), android.R.color.white);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_save) {
            save();
            return true;
        } else if(id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        EditText accountName = (EditText)findViewById(R.id.account_name);
        EditText accountNote = (EditText)findViewById(R.id.account_note);
        EditText accountLogin = (EditText)findViewById(R.id.account_login);
        EditText accountPassword = (EditText)findViewById(R.id.account_password);
        EditText accountNumber = (EditText)findViewById(R.id.account_number);
        EditText accountContact = (EditText)findViewById(R.id.account_contact);
        ToggleButton btnCash = (ToggleButton) findViewById(R.id.account_button_cash);
        ToggleButton btnCard = (ToggleButton) findViewById(R.id.account_button_card);

        if(accountName.getText().length() > 0) {
            int accountType = IAccount.ACCOUNT_TYPE_CASH_CARD;

            if((btnCard.isChecked()) && (btnCash.isChecked())) {
                accountType = IAccount.ACCOUNT_TYPE_CASH_CARD;
            } else if(btnCard.isChecked()) {
                accountType = IAccount.ACCOUNT_TYPE_CARD_ONLY;
            } else if(btnCash.isChecked()) {
                accountType = IAccount.ACCOUNT_TYPE_CASH_ONLY;
            }

            iAccount.setIsoCurrency(selectedCurrency.getCurrencyCode());
            iAccount.setTitle(accountName.getText().toString());
            iAccount.setNote(accountNote.getText().toString());
            iAccount.setAccountNumber(accountNumber.getText().toString());
            iAccount.setLoginId(accountLogin.getText().toString());
            iAccount.setPassword(accountPassword.getText().toString());
            iAccount.setPassword(accountContact.getText().toString());
            iAccount.setLastUpdate(Calendar.getInstance().getTimeInMillis());
            iAccount.setAccountType(accountType);

            DataSourceAccount source = new DataSourceAccount(this);
            if(accountId == Constant.INVALID_ID) {
                accountId = source.insertAccount(iAccount);
                Toast.makeText(getApplicationContext(), "New Account Added", Toast.LENGTH_SHORT).show();
            } else {
                source.updateAccount(iAccount);
                Toast.makeText(getApplicationContext(), "Account Updated", Toast.LENGTH_SHORT).show();
            }

            Bundle activityResult = new Bundle();
            activityResult.putLong(Constant.ARG_ACCOUNT_NUMBER_LONG, accountId);
            Intent intent = new Intent();
            intent.putExtras(activityResult);
            setResult(RESULT_OK, intent);
            finish();
        }
        setResult(RESULT_CANCELED);
        finish();
    }
}
