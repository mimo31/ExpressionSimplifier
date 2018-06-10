package com.github.mimo31.expressionsimplifier;

import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.view.Menu;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

public class MainActivity extends AppCompatActivity {

    EditText inputEdit;
    TextView resultText;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setTitle("Expression Simplifier");
        this.inputEdit = (EditText) this.findViewById(R.id.inputEdit);
        this.resultText = (TextView) this.findViewById(R.id.resultText);
        this.toolbar = toolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        menu.findItem(R.id.menuExpand).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return goToHelp();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuExpand) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean goToHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        this.startActivity(intent);
        return true;
    }

    public void simplify(View view) {
        if (view.getId() == R.id.simplifyButton) {
            String expressionText = this.inputEdit.getText().toString();
            String result;
            try {
                MathExpression expression = MathExpression.getMathExpression(expressionText);
                expression = expression.simplify();
                result = expression.toString();
            } catch (FormatException e) {
                result = e.getMessage();
            }
            this.resultText.setText(result);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void clear(View view) {
        if (view.getId() == R.id.clearButton) {
            EditText inputEdit = (EditText)findViewById(R.id.inputEdit);
            inputEdit.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input", inputEdit.getText().toString());
        outState.putString("output", resultText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        inputEdit.setText(savedInstanceState.getString("input"));
        resultText.setText(savedInstanceState.getString("output"));
    }
}
