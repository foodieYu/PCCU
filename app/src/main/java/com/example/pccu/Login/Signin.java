package com.example.pccu.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.pccu.LoginSuccess.LandlordSigninSuccess;
import com.example.pccu.LoginSuccess.StudentSigninSuccess;
import com.example.pccu.R;
import com.example.pccu.Registered.Registered;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class Signin extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String account;
    private String password;
    private TextInputLayout accoutLayout;
    private TextInputLayout passwordLayout;
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button signinBtn,registeredBtn;
    private FirebaseUser user;
    private RadioGroup radiogroup;
    private RadioButton landlord,student;
    private String landlordId,studentId;
    private int number=0;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initView();

        //取消ActionBar
        getSupportActionBar().hide();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        accountEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        accoutLayout = (TextInputLayout) findViewById(R.id.account_layout);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);
        accoutLayout.setErrorEnabled(true);
        signinBtn = (Button) findViewById(R.id.signin_btn);
        registeredBtn=(Button) findViewById(R.id.registered_btn);
        landlord=(RadioButton) findViewById(R.id.student_radiobtn);
        student=(RadioButton) findViewById(R.id.student_radiobtn);
        radiogroup=(RadioGroup) findViewById(R.id.radiogroup);

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(accountEdit.getText().toString().matches("")) {
                    Toast.makeText(Signin.this,"請輸入帳號",Toast.LENGTH_SHORT).show();
                    if(checkedId==R.id.landlord_radiobtn){
                        number=1;
                    }else if(checkedId==R.id.student_radiobtn){
                        number=2;
                    }
                }else if(!accountEdit.getText().toString().matches("")){
                    if(checkedId==R.id.landlord_radiobtn){
                        number=1;
                        Log.i("選擇房東", String.valueOf(number));
                    }else if(checkedId==R.id.student_radiobtn){
                        number=2;
                        Log.i("選擇學生", String.valueOf(number));
                    }
                }
            }
        });

        //登入驗證
        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String account = accountEdit.getText().toString();
                final String password = passwordEdit.getText().toString();
                if (TextUtils.isEmpty(account)) {
                    accoutLayout.setError(getString(R.string.plz_input_accout));
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordLayout.setError(getString(R.string.plz_input_pw));
                    return;
                }
                accoutLayout.setError("");
                passwordLayout.setError("");

                mAuth.signInWithEmailAndPassword(account, password)
                        .addOnCompleteListener(Signin.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    user = FirebaseAuth.getInstance().getCurrentUser();
                                    boolean emailVerified = user.isEmailVerified();
                                    Log.i("選擇對象", String.valueOf(number));
                                    if (emailVerified == true) {//確認是否驗證信箱
                                        if(number==1){//房東登入
                                            Query query=db.collection("studentinfo").whereEqualTo("Account",account);
                                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    QuerySnapshot querySnapshot = task.isSuccessful() ? task.getResult() : null;
                                                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                                        if (documentSnapshot.getId().equals(account)){
                                                            Toast.makeText(Signin.this, "找不到此帳號，請重新確認帳號密碼是否有輸入錯誤或者身分選擇錯誤", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });

                                            db.collection("landlordinfo").whereEqualTo("Account",account)
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                                        Log.i("第一個",documentSnapshot.getId());
                                                        Log.i("第二個",account);
                                                        if (documentSnapshot.getId().equals(account)){
                                                            Toast.makeText(Signin.this, R.string.sign_success, Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent();
                                                            intent.setClass(Signin.this, LandlordSigninSuccess.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                }
                                            });
                                        } else if(number==2) {//學生登入
                                            Query query=db.collection("landlordinfo").whereEqualTo("Account",account);
                                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    QuerySnapshot querySnapshot = task.isSuccessful() ? task.getResult() : null;
                                                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                                        if (documentSnapshot.getId().equals(account)){
                                                            Toast.makeText(Signin.this, "找不到此帳號，請重新確認帳號密碼是否有輸入錯誤或者身分選擇錯誤", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });

                                            db.collection("studentinfo").whereEqualTo("Account",account)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    QuerySnapshot querySnapshot = task.isSuccessful() ? task.getResult() : null;
                                                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                                        Log.i("第一個",documentSnapshot.getId());
                                                        Log.i("第二個",account);
                                                        if (documentSnapshot.getId().equals(account)){
                                                            Toast.makeText(Signin.this, R.string.sign_success, Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent();
                                                            intent.setClass(Signin.this, StudentSigninSuccess.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }else {
                                        Toast.makeText(Signin.this, R.string.emailckeck, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Signin.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        registeredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Signin.this, Registered.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(Signin.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                Signin.this.finish();//關閉activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//顯示對話框
    }
}



/*
Toast.makeText(Signin.this, "找不到此帳號，請重新確認帳號密碼是否有輸入錯誤或者身分選擇錯誤", Toast.LENGTH_SHORT).show();
 */