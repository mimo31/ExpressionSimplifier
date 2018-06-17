package com.github.mimo31.expressionsimplifier;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.view.Menu;
import android.widget.ListView;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText inputEdit;
    ListView outputList;
    Toolbar toolbar;
    ArrayList<String> outputListItems = new ArrayList<String>();
    ArrayAdapter<String> outputAdapter;
    // RecyclerView.Adapter recyclerAdapter;
    // ArrayList<Recycler> // TO BE CONTINUED FROM HERE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setTitle("Expression Simplifier");
        this.inputEdit = this.findViewById(R.id.inputEdit);
        this.outputList = this.findViewById(R.id.outputList);
        this.outputAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.outputListItems);
        this.outputList.setAdapter(this.outputAdapter);
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

    public void process(View view) {
        if (view.getId() == R.id.simplifyButton) {
            String expressionText = this.inputEdit.getText().toString();
            this.outputListItems.clear();
            Logic.processInput(expressionText, this::pushOutput);
            if (this.outputListItems.size() == 0)
            {
                this.pushOutput("no results");
            }

            /*String result;
            try {
                MathExpression expression = MathExpression.getMathExpression(expressionText);
                expression = expression.simplify();
                result = expression.toString();
            } catch (FormatException e) {
                result = e.getMessage();
            }
            this.resultText.setText(result);*/

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void pushOutput(String output)
    {
        this.outputListItems.add(output);
    }

    public void clear(View view) {
        if (view.getId() == R.id.clearButton) {
            inputEdit.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input", inputEdit.getText().toString());
        // outState.putString("output", resultText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        inputEdit.setText(savedInstanceState.getString("input"));
        // resultText.setText(savedInstanceState.getString("output"));
    }
}
