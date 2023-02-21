package com.example.monobox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;

public class CustomGalleryActivity extends AppCompatActivity {

    ArrayList<String> f = new ArrayList<>();
    File[] listFile;
    private String folderName = "MyPhotoDir";

    ViewPager mViewPager;

    ViewPagerAdapter mViewPagerAdapter;

    Button selectBtn;

    boolean dateAble = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getFromSdcard();
        mViewPager = findViewById(R.id.viewPagerMain);
        mViewPagerAdapter = new ViewPagerAdapter(this, f);
        mViewPager.setAdapter(mViewPagerAdapter);

        selectBtn = findViewById(R.id.select);
        
        Intent get = getIntent();
        dateAble = get.getBooleanExtra("dateAble", false);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = mViewPager.getCurrentItem();
                String path = f.get(currentItem);
                Intent intent = new Intent(CustomGalleryActivity.this, MainActivity.class);
                intent.putExtra("path", path);
                intent.putExtra("logoAble", true);
                intent.putExtra("dateAble", dateAble);
                startActivity(intent);
            }
        });
    }

    public void getFromSdcard() {
        File file = new File(getExternalFilesDir(folderName + "icon"), "/");
        File file1 = new File(getExternalFilesDir(folderName + "iconsmall"), "/");
        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                f.add(listFile[i].getAbsolutePath());
            }
        }
        if (file1.isDirectory()) {
            listFile = file1.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                f.add(listFile[i].getAbsolutePath());
            }
        }
    }
}
