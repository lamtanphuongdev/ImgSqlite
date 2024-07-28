package com.example.imgsqlite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class IUDActivity extends AppCompatActivity {

    //KHAI BAO
    Button btnSave, btnChooseImg, btnDelete, btnCancel;
    EditText edtName, edtStadium, edtCapacity;
    ImageView imgView;
    Bitmap bitmap;
    int RQC_CHOOSE_IMG = 111;
    SQLiteDatabase DB;
    String ACTION = null;
    int ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iud);

        Init();
        MakeOrOpenDB();
        Act();
        getReqAndData();
    }

    private void getReqAndData() {
        Intent intent = this.getIntent();
        ACTION = intent.getStringExtra("ACTION");
        switch (ACTION){
            case "ADD":
                btnDelete.setEnabled(false);
                break;
            case "UPDATE":
                btnDelete.setEnabled(true);
                ID = intent.getIntExtra("ID",-1);
                if (ID==-1){
                    finish();
                }else {
                    Cursor cursor = DB.rawQuery("SELECT * FROM fcteam WHERE ID=?", new String[] {ID +""});
                    cursor.moveToFirst();
                    edtName.setText(cursor.getString(1));
                    imgView.setImageBitmap(BitmapUtility.getImage(cursor.getBlob(2)));
                    bitmap = BitmapUtility.getImage(cursor.getBlob(2));
                    edtStadium.setText(cursor.getString(3));
                    edtCapacity.setText(cursor.getString(4));
                }
                break;
        }
    }

    private void Act() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImg();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteTeam();
            }
        });
    }

    private void DeleteTeam() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_delete);
        builder.setTitle("Xoá đội bóng");
        builder.setMessage("Bạn có muốn xoá?");

        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int i = DB.delete("fcteam","ID="+ID+"",null);
                if (i<1){
                    Toast.makeText(IUDActivity.this, "Xoá thất bại", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(IUDActivity.this, "Xoá thành công", Toast.LENGTH_SHORT).show();
                }
                BackToMainActivity();
            }
        });

        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void BackToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void SaveData() {
        String msg="";
        String NAME = edtName.getText().toString().trim();
        String STADIUM = edtStadium.getText().toString().trim();
        String CAPACITY = edtCapacity.getText().toString().trim();

        switch (ACTION){
            case "ADD":
                msg = "ADD";
                if(!isEmptyData()){
                    msg = "Dũ liệu chưa hợp lệ;";
                }else {
                    byte[] LOGO = BitmapUtility.getBytes(bitmap);
                    ContentValues values = new ContentValues();
                    values.put("NAME",NAME);
                    values.put("STADIUM",STADIUM);
                    values.put("CAPACITY",CAPACITY);
                    values.put("LOGO",LOGO);

                    long r = DB.insert("fcteam","_ID",values);

                    if(r==-1){
                        msg = "Thêm thất bại!";
                    }else {
                        msg = "Thêm thành công";
                        ClearControl();
                    }

                }
                break;
            case "UPDATE":

                byte[] LOGO = BitmapUtility.getBytes(bitmap);
                ContentValues values = new ContentValues();
                values.put("NAME",NAME);
                values.put("STADIUM",STADIUM);
                values.put("CAPACITY",CAPACITY);
                values.put("LOGO",LOGO);
                int u = DB.update("fcteam", values, "ID = '"+ID+"'",null );
                if (u>0){
                    msg="Cập nhật thành công";
                }else{
                    msg="Cập nhật thất bại";
                }
                break;
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void ClearControl() {
        edtName.getText().clear();
        edtCapacity.getText().clear();
        edtStadium.getText().clear();
        bitmap = null;
        imgView.setImageBitmap(null);
    }

    private boolean isEmptyData() {
        String NAME = edtName.getText().toString().trim();
        String STADIUM = edtStadium.getText().toString().trim();
        String CAPACITY = edtCapacity.getText().toString().trim();
        if(NAME.isEmpty())
            return false;
        if(STADIUM.isEmpty())
            return false;
        if(CAPACITY.isEmpty())
            return false;
        if (bitmap == null)
            return false;
        return true;
    }

    private void chooseImg() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,RQC_CHOOSE_IMG);
    }

    private void Init() {
        btnCancel = findViewById(R.id.btnCancel);
        btnChooseImg = findViewById(R.id.btnChooseImg);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        edtCapacity = findViewById(R.id.edtCapacity);
        edtName = findViewById(R.id.edtName);
        edtStadium = findViewById(R.id.edtStadium);

        imgView = findViewById(R.id.imgView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RQC_CHOOSE_IMG && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imgView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void MakeOrOpenDB(){
        DB = openOrCreateDatabase("fcdb.sqlite",MODE_PRIVATE,null);
        String sql = "CREATE TABLE IF NOT EXISTS fcteam" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "NAME VARCHAR(100)," +
                "LOGO BLOB," +
                "STADIUM VARCHAR(30)," +
                "CAPACITY VARCHAR(30)" +
                ")";
        DB.execSQL(sql);

    }


}