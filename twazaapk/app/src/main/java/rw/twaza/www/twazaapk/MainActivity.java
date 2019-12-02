package com.twaza.wwww.twazaapk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import com.twaza.wwww.twazaapk.mode.User;

public class MainActivity extends AppCompatActivity {

    //    button and text we will use in loin and submit
    private Button Signin;
    private Button Register;
    // end of button decarlaring

    //    style decallaring
    private Typeface tf1,tf2,tf3;
    private TextView heading;
    private  TextView twazaheading;
    //     end of decalarig sty

//    decallaring fire base requers

    private FirebaseDatabase db;
    private FirebaseAuth Auth;
    private DatabaseReference users;
    //    end of decalaring firebase
    RelativeLayout rootLayoutl;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        heading =(TextView)findViewById(R.id.txt_rid_app);
        Signin = (Button)findViewById(R.id.btnSignIn);
        Register =(Button)findViewById(R.id.btnRegister);
        twazaheading =(TextView)findViewById(R.id.twazaheading);


//        rootLayoutl =(RelativeLayout)findViewById(R.id.rootlayout);


//        calling styling of words
        tf2 = Typeface.createFromAsset(getAssets(),"101!kimmy'skowboyhat.ttf");
        tf1 = Typeface.createFromAsset(getAssets(),"Tyrannothesaurus.otf");
        heading.setTypeface(tf1);
        Signin.setTypeface(tf1);
        Register.setTypeface(tf1);
        twazaheading.setTypeface(tf2);
//        end of calling styling phse

        Auth = FirebaseAuth.getInstance();
        db =FirebaseDatabase.getInstance();
        users = db.getReference("userse");

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegistarationDialog();
            }
        });
        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowLoginDialog();
            }
        });


    }

    private void ShowLoginDialog(){
    final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGIN IN");
            dialog.setMessage("please wailt");
    LayoutInflater inflater = LayoutInflater.from(this);

    final View sigin_in = inflater.inflate(R.layout.layout_login,null);
    final MaterialEditText email = sigin_in.findViewById(R.id.txtlEmaila);
    final MaterialEditText password = sigin_in.findViewById(R.id.txtlPassword1);

        dialog.setView(sigin_in);
        dialog.setPositiveButton("SIGIN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//                Signin.setEnabled(false);
                if (TextUtils.isEmpty((email.getText().toString()))) {
                    Toast.makeText(MainActivity.this, "please enter your email", Toast.LENGTH_SHORT).show();

                    return;
                }
                if (TextUtils.isEmpty((password.getText().toString()))) {
                    Toast.makeText(MainActivity.this, "please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.getText().toString().length() < 6) {
                    Toast.makeText(MainActivity.this, "please enter your password more than six number", Toast.LENGTH_SHORT).show();
                    return;
                }
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

//                sigin_in
                Auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult)
                            {
                                waitingDialog.dismiss();
                                Intent main = new Intent(MainActivity.this, test.class);
                                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(main);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                        waitingDialog.dismiss();
                        Toast.makeText(MainActivity.this,"FaiLed",Toast.LENGTH_SHORT)
                                .show();
                        Register.setEnabled(false);


                    }
                });
            }
        });


        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
            dialogInterface.dismiss();

        }
    });
        dialog.show();

}

    private void showRegistarationDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTARATION");
//        dialog.setMessage("please wailt");
        LayoutInflater inflater = LayoutInflater.from(this);

        View login_Layout = inflater.inflate(R.layout.registeration,null);
        final MaterialEditText email = login_Layout.findViewById(R.id.txtEmail);
        final MaterialEditText password = login_Layout.findViewById(R.id.txtPassword);
        final MaterialEditText fname = login_Layout.findViewById(R.id.txtFname);
        final MaterialEditText lname = login_Layout.findViewById(R.id.txtLname);
        final MaterialEditText telephone = login_Layout.findViewById(R.id.txtPhone);
        final MaterialEditText gender = login_Layout.findViewById(R.id.txtGender);
        dialog.setView(login_Layout);
        dialog.setPositiveButton("Registaration", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//                Register.setEnabled(false);
                if (TextUtils.isEmpty((email.getText().toString())))
                {
                    Toast.makeText(MainActivity.this,"please enter your email",Toast.LENGTH_SHORT).show();

                    return;
                }
                if (TextUtils.isEmpty((password.getText().toString())))
                {
                    Toast.makeText(MainActivity.this,"please enter your password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.getText().toString().length()<6)
                {
                    Toast.makeText(MainActivity.this,"please enter your password more than six number",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty((fname.getText().toString())))
                {
                    Toast.makeText(MainActivity.this,"please enter your first name",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty((lname.getText().toString())))
                {
                    Toast.makeText(MainActivity.this,"please enter your last name",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty((telephone.getText().toString())))
                {
                    Toast.makeText(MainActivity.this,"please enter your telephone",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty((gender.getText().toString())))
                {
                    Toast.makeText(MainActivity.this,"please enter your gender",Toast.LENGTH_SHORT).show();
                    return;
                }
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();
                Auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setPassword(password.getText().toString());
                                user.setFname(fname.getText().toString());
                                user.setLname(lname.getText().toString());
                                user.setPhone(telephone.getText().toString());
                                user.setGender(gender.getText().toString());


                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                waitingDialog.dismiss();
                                                   Intent main = new Intent(MainActivity.this, test.class);
                                                   main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                   startActivity(main);
                                                   finish();
                                                Toast.makeText(MainActivity.this,"Regisataration successful",Toast.LENGTH_SHORT)
                                                        .show();
                                                Register.setEnabled(true);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                waitingDialog.dismiss();
                                                Toast.makeText(MainActivity.this,"FaiLed",Toast.LENGTH_SHORT)
                                                        .show();
                                                Register.setEnabled(false);

                                            }
                                        });


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e)

                            {
                                waitingDialog.dismiss();
                                Intent main = new Intent(MainActivity.this, test.class);
                                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(main);
                                finish();
//                                Register.setEnabled(false);
                            }
                        });

            }


        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                dialogInterface.dismiss();

            }
        });
        dialog.show();

    }

}