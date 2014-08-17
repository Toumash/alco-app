package pl.pcd.alcohol.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.jetbrains.annotations.NotNull;
import pl.pcd.alcohol.Cfg;
import pl.pcd.alcohol.R;
import pl.pcd.alcohol.activity.base.ThemeActivity;
import pl.pcd.alcohol.database.UserDB;
import pl.pcd.alcohol.webapi.contract.User_Alcohol;

import java.util.ArrayList;
import java.util.List;


public class EditorActivity extends ThemeActivity {

    public static final String TAG = "EditorActivity";
    public int EDIT_MODE = 1;
    UserDB db;
    @NotNull
    Context context = this;
    EditText et_name, et_price, et_volume, et_percent;
    TextView tv_error, tv_deposit;
    CheckBox cb_deposit;
    Spinner sr_type, sr_subtype;
    Button bt_commit;
    long Alcohol_ID_fromIntent;

    public static Intent createIntent(Context context, long alcoholID, String name, double price, double percent, int type, int subtype, int volume, boolean deposit) {
        //TODO: db fetching done in EditorActivity not in Calling Activity
        Intent intent = new Intent(context, EditorActivity.class);
        Bundle extras = new Bundle();
        extras.putLong("id", alcoholID);
        extras.putString(EditActivityIntents.EditIntentExtras.KEY_NAME, name);
        extras.putDouble(EditActivityIntents.EditIntentExtras.KEY_PRICE, price);
        extras.putInt(EditActivityIntents.EditIntentExtras.KEY_TYPE, type);
        extras.putInt(EditActivityIntents.EditIntentExtras.KEY_SUBTYPE, subtype);
        extras.putInt(EditActivityIntents.EditIntentExtras.KEY_VOLUME, volume);
        extras.putDouble(EditActivityIntents.EditIntentExtras.KEY_PERCENT, percent);
        extras.putInt(EditActivityIntents.EditIntentExtras.KEY_DEPOSIT, deposit ? 1 : 0);


        intent.putExtras(extras);

        return intent;
    }

    /**
     * @return empty list if there is no error
     */
    @SuppressWarnings("ConstantConditions")
    @NotNull
    private List<String> validateUserInput() {
        List<String> ret = new ArrayList<String>();
        if (et_name.getText().toString().trim().length() <= 3) ret.add("Nazwie");
        try {
            if (Float.parseFloat(et_price.getText().toString().trim()) <= 0) throw new Exception();
        } catch (Exception e) {
            ret.add("Cenie");
        }
        try {
            if (Integer.valueOf(et_volume.getText().toString().trim()) <= 0) throw new Exception();
        } catch (Exception e) {
            ret.add("Objętości");
        }
        try {
            if (Float.valueOf(et_percent.getText().toString().trim()) <= 0 || Float.valueOf(et_percent.getText().toString().trim()) >= 100)
                throw new Exception();
        } catch (Exception e) {
            ret.add("Procentach");
        }

        return ret;
    }

    private void openDB() {
        db = new UserDB(this);
        db.open();
    }

    private void closeDB() {
        if (db != null)
            db.close();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentViewWithTitle(context, R.layout.activ_editor, R.string.editor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                //noinspection ConstantConditions
                getActionBar().setDisplayShowHomeEnabled(true);
            } catch (NullPointerException e) {
                Log.d(TAG, "Warning: No ActionBar detected");
            }
        }
        bt_commit = (Button) findViewById(R.id.bt_commit);
        et_name = (EditText) findViewById(R.id.editor_et_name);
        et_price = (EditText) findViewById(R.id.editor_et_price);
        et_volume = (EditText) findViewById(R.id.editor_et_volume);
        et_percent = (EditText) findViewById(R.id.editor_et_percent);

        tv_error = (TextView) findViewById(R.id.editor_error);

        cb_deposit = (CheckBox) findViewById(R.id.editor_cb_deposit);
        tv_deposit = (TextView) findViewById(R.id.editor_tv_deposit);
        sr_type = (Spinner) findViewById(R.id.editor_sr_type);
        sr_subtype = (Spinner) findViewById(R.id.editor_sr_subtype);

        bt_commit.setOnClickListener(new onClick_Commit());


        //Loads the info to be edited by the user,
        // as it is in EDIT_MODE
        Bundle xtr = getIntent().getExtras();
        if (xtr != null) {
            Alcohol_ID_fromIntent = xtr.getLong(EditActivityIntents.EditIntentExtras.KEY_ID);
            et_name.setText(xtr.getString(EditActivityIntents.EditIntentExtras.KEY_NAME));
            et_price.setText(Float.toString(xtr.getFloat(EditActivityIntents.EditIntentExtras.KEY_PRICE)));
            sr_type.setSelection(xtr.getInt(EditActivityIntents.EditIntentExtras.KEY_TYPE));
            int arrayRId = -1;
            switch ((int) sr_type.getSelectedItemId()) {
                case User_Alcohol.Type.NISKOPROCENTOWY:
                    arrayRId = R.array.niskoprocentowe;
                    break;
                case User_Alcohol.Type.SREDNIOPROCENTOWY:
                    arrayRId = R.array.srednioprocentowe;
                    break;

                case User_Alcohol.Type.WYSOKOPROCENTOWY:
                    arrayRId = R.array.wysokoprocentowe;
                    cb_deposit.setEnabled(false);
                    tv_deposit.setEnabled(false);
                    break;
            }
            if (arrayRId != -1) buildNSetAdapterAlcSubType(arrayRId);
            // buildNSetAdapterAlcSubType();
            // sr_subtype.setSelection(xtr.getInt(EditIntentExtras.KEY_SUBTYPE));
            et_volume.setText(Integer.toString(xtr.getInt(EditActivityIntents.EditIntentExtras.KEY_VOLUME)));
            et_percent.setText(Integer.toString(xtr.getInt(EditActivityIntents.EditIntentExtras.KEY_PERCENT)));
            bt_commit.setText(getResources().getString(android.R.string.ok));
            cb_deposit.setChecked(xtr.getInt(EditActivityIntents.EditIntentExtras.KEY_DEPOSIT) == 1);
            EDIT_MODE = EDIT_MODES.UPDATE;

        } else {
            EDIT_MODE = EDIT_MODES.ADD;
        }
        initSpinner();
        openDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void initSpinner() {
        sr_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView == sr_type) {
                    int arrayRId = -1;
                    cb_deposit.setEnabled(true);
                    tv_deposit.setEnabled(true);
                    switch (position) {
                        case User_Alcohol.Type.NISKOPROCENTOWY:
                            arrayRId = R.array.niskoprocentowe;
                            break;
                        case User_Alcohol.Type.SREDNIOPROCENTOWY:
                            arrayRId = R.array.srednioprocentowe;
                            break;

                        case User_Alcohol.Type.WYSOKOPROCENTOWY:
                            arrayRId = R.array.wysokoprocentowe;
                            cb_deposit.setEnabled(false);
                            tv_deposit.setEnabled(false);
                            break;
                    }
                    if (arrayRId != -1) buildNSetAdapterAlcSubType(arrayRId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void buildNSetAdapterAlcSubType(int arrayRId) {
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                arrayRId, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sr_subtype.setAdapter(arrayAdapter);
        sr_subtype.requestFocus();
    }

    static class EDIT_MODES {
        public static final int ADD = 1;
        public static final int UPDATE = 2;
    }

    public static final class EditActivityIntents {

        public static final class EditIntentExtras {
            public static final String KEY_NAME = "name";
            public static final String KEY_PRICE = "price";
            public static final String KEY_TYPE = "type";
            public static final String KEY_SUBTYPE = "subtype";
            public static final String KEY_VOLUME = "volume";
            public static final String KEY_PERCENT = "percent";
            public static final String KEY_DEPOSIT = "deposit";
            public static final String KEY_ID = "id";
        }
    }

    private class onClick_Commit implements View.OnClickListener {
        @Override
        public void onClick(@NotNull View view) {

            List<String> validator = validateUserInput();

            if (validator.isEmpty()) {

                if (tv_error.getVisibility() == View.VISIBLE) tv_error.setVisibility(View.INVISIBLE);
                int deposit = cb_deposit.isChecked() ? 1 : 0;

                //noinspection ConstantConditions
                User_Alcohol newAlcohol = new User_Alcohol(
                        et_name.getText().toString().trim(),
                        Double.valueOf(et_price.getText().toString().trim()),
                        Integer.valueOf(et_volume.getText().toString().trim()),
                        Double.valueOf(et_percent.getText().toString().trim()), (int) sr_type.getSelectedItemId(), (int) sr_subtype.getSelectedItemId(), deposit);
                switch (EDIT_MODE) {
                    case EDIT_MODES.ADD: {
                        db.insertRow(newAlcohol);
                        Toast.makeText(context, R.string.alcohol_added, Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Alcohol added to database");
                        if (Cfg.DEBUG) Log.d(TAG, et_name.getText().toString() + " "
                                + Float.valueOf(et_price.getText().toString()) + "zł "
                                + Integer.valueOf(et_volume.getText().toString()) + "ml "
                                + Float.valueOf(et_percent.getText().toString()) + "%");
                        et_name.setText("");
                        et_price.setText("");
                        et_volume.setText("");
                        et_percent.setText("");
                    }
                    break;
                    case EDIT_MODES.UPDATE: {
                        db.updateRow(Alcohol_ID_fromIntent, newAlcohol);
                        Log.d(TAG, "Alcohol Updated");
                        finish();
                    }
                    break;
                }
            } else {
                StringBuilder error_text = new StringBuilder("Błąd w ");
                for (String x : validator) {
                    error_text.append(x);
                    error_text.append(' ');
                }
                tv_error.setText(error_text);
                tv_error.setVisibility(View.VISIBLE);
            }
        }
    }
}
