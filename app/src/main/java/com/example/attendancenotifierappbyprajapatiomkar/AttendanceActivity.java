package com.example.attendancenotifierappbyprajapatiomkar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AttendanceActivity extends AppCompatActivity {
    private String subject_txt, message_txt, raw_data_from_csv, strings_emails,string_name;
    private Button selectFileBtn, sendEmailBtn;
    private EditText subject_EditTxt, message_EditTxt, email_EditTxt, password_EditTxt;
    private ListView attendance_listView;
    private ProgressBar pb;
    private StudentAdapter studentAdapter;

    private List<String> EMAIL_ARRAY, NAME_ARRAY, PERCENTAGE_ARRAY, RESULT_EMAIL_ARRAY;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);


//        Objects.requireNonNull(getSupportActionBar()).hide();

        selectFileBtn = findViewById(R.id.selectFileBtn);
        sendEmailBtn = findViewById(R.id.sendEmailBtn);
        subject_EditTxt = findViewById(R.id.subject_EditTxt);
        message_EditTxt = findViewById(R.id.message_EditTxt);
        attendance_listView = findViewById(R.id.attendance_listView);
        email_EditTxt = findViewById(R.id.email_EditTxt);
        password_EditTxt = findViewById(R.id.password_EditTxt);
        pb = findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);

        studentAdapter = new StudentAdapter(getApplicationContext(), new ArrayList<Students>());
        attendance_listView.setAdapter(studentAdapter);
        setUserCredential();
        selectFileBtn.setText("+ SELECT");

        selectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                data.setType("text/comma-separated-values");
                intent_Activity_Result_Launcher.launch(data);

            }
        });

        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(email_EditTxt.getText()) && TextUtils.isEmpty(password_EditTxt.getText()) && TextUtils.isEmpty(subject_EditTxt.getText()) && TextUtils.isEmpty(message_EditTxt.getText())) {
                    email_EditTxt.setError("Email Id is required!");
                    password_EditTxt.setError("Password is required!");
                    subject_EditTxt.setError("Subject Field is required!");
                    message_EditTxt.setError("Message Field is required!");
                }
                if (raw_data_from_csv != null) {
                    extractingRawData();
                    run();
                    pb.setVisibility(View.VISIBLE);

                }
                if(selectFileBtn.getText() == "+ SELECT"){
                    Toast.makeText(getApplicationContext(),"Please Select the file",Toast.LENGTH_SHORT).show();
                }
                dataPersistence();
                closeKeyBoard();
            }
        });


    }



    private void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if (view !=null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void run() {
        AttendanceAsyncTask  attendanceAsyncTask = new AttendanceAsyncTask();
        attendanceAsyncTask.execute();
    }

    private void setUserCredential() {
        SharedPreferences sharedPreferences = getSharedPreferences("userCredential", MODE_PRIVATE);
        email_EditTxt.setText((sharedPreferences.getString("email", "")));
        password_EditTxt.setText((sharedPreferences.getString("pass", "")));
    }


    private void dataPersistence() {
        SharedPreferences sharedPreferences = getSharedPreferences("userCredential", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email_EditTxt.getText().toString());
        editor.putString("pass", password_EditTxt.getText().toString());
        editor.apply();
    }

    private void extractingRawData() {
        NAME_ARRAY = new ArrayList<>();
        EMAIL_ARRAY = new ArrayList<>();
        PERCENTAGE_ARRAY = new ArrayList<>();
        RESULT_EMAIL_ARRAY = new ArrayList<>();
        patternMatching();
    }

    private void patternMatching() {
        RegexPattern regexPattern = new RegexPattern();
        Pattern name_pattern = Pattern.compile(regexPattern.getNAME_REGEX());
        Pattern email_pattern = Pattern.compile(regexPattern.getEMAIL_REGEX());
        Pattern percentage_pattern = Pattern.compile(regexPattern.getPERCENTAGE_REGEX());

        Matcher name_matcher = name_pattern.matcher(raw_data_from_csv);
        Matcher email_matcher = email_pattern.matcher(raw_data_from_csv);
        Matcher percentage_matcher = percentage_pattern.matcher(raw_data_from_csv);

        while (name_matcher.find()) {
            NAME_ARRAY.add(name_matcher.group());
        }
        while (email_matcher.find()) {
            EMAIL_ARRAY.add(email_matcher.group());
        }
        while (percentage_matcher.find()) {
            PERCENTAGE_ARRAY.add(String.valueOf(Math.round(Float.parseFloat(percentage_matcher.group()))));
        }
//        Small test case to check final email
        for (int i = 0; i < NAME_ARRAY.size(); i++) {
            if (NAME_ARRAY.get(i) != null && EMAIL_ARRAY.get(i) != null && PERCENTAGE_ARRAY.get(i) != null && Integer.parseInt(PERCENTAGE_ARRAY.get(i + 1)) < 75) {
                RESULT_EMAIL_ARRAY.add(EMAIL_ARRAY.get(i));
            }
        }
//       Converting RESULT_EMAIL_ARRAY into string for further process
        for (int j = 0; j < RESULT_EMAIL_ARRAY.size(); j++) {
            strings_emails = strings_emails + RESULT_EMAIL_ARRAY.get(j) + ",";
        }

        try {
            subject_txt = subject_EditTxt.getText().toString();
            message_txt = message_EditTxt.getText().toString();
            String email = email_EditTxt.getText().toString();
            String password = password_EditTxt.getText().toString();
            String stringReceiverEmail = strings_emails;

            String[] recipientList = stringReceiverEmail.split(",");
            InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            int counter = 0;
            for (String recipient : recipientList) {
                recipientAddress[counter] = new InternetAddress(recipient.trim());
                counter++;
            }

            message.setRecipients(Message.RecipientType.TO, recipientAddress);
            message.setSubject(subject_txt);
            message.setText(message_txt);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(message);
                        pb.setVisibility(View.INVISIBLE);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();


        } catch (Exception e) {

        }
    }

//  Creating ActivityResultLauncher To Get Data After Clicking On Button In String Format From Data Source like .CSV

    ActivityResultLauncher<Intent> intent_Activity_Result_Launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        selectFileBtn.setText("FILE SELECTED SUCCESSFULLY");
                        Intent data = result.getData();
                        assert data != null;
                        Uri uri = data.getData();
                        byte[] character_bytes = getByteFromUri(getApplicationContext(), uri);
                        raw_data_from_csv = new String(character_bytes);
                    }
                }

                private byte[] getByteFromUri(Context context, Uri uri) {
                    InputStream inputStream = null;
                    ByteArrayOutputStream byteArrayOutputStream = null;

                    try {
                        inputStream = context.getContentResolver().openInputStream(uri);
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];          // Inside [] <- We pass BufferSize
                        int length = 0;
                        while ((length = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert byteArrayOutputStream != null;
                    return byteArrayOutputStream.toByteArray();
                }
            }

    );

    public class AttendanceAsyncTask extends AsyncTask<Void, Void, ArrayList<Students>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected ArrayList<Students> doInBackground(Void... voids) {
            ArrayList<Students> result = new ArrayList<>();

            ArrayList<String> names = new ArrayList<>();
            for (int j = 0; j < NAME_ARRAY.size(); j++) {
                string_name = string_name + NAME_ARRAY.get(j) + ",";
            }
            Pattern pattern = Pattern.compile("([A-Z a-z]+)");
            Matcher matcher = pattern.matcher(string_name);
            while (matcher.find()){
                names.add(matcher.group());
            }
            for (int i=0;i<NAME_ARRAY.size();i++){
                Students students = new Students(names.get(i+1),PERCENTAGE_ARRAY.get(i+1)+" %");
                result.add(students);
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Students> students) {
            studentAdapter.clear();
            if (students != null && !students.isEmpty()) {
                studentAdapter.addAll(students);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}