package com.xtc.lint;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button btnClick;
    private TextView tvShow;
    private TextView tvTest;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    public enum EnumTest {
        MON, TUE, WED, THU, FRI, SAT, SUN;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1_main);
        initViews();
        init();
        setTitle("欧阳鹏1");

        HashMap<Integer, String> map1 = new HashMap<Integer, String>();
        map1.put(1, "name");
        HashMap<Integer, String> map2 = new HashMap<>();
        map2.put(1, "name");
        Map<Integer, String> map3 = new HashMap<>();
        map3.put(1, "name");

        LongSparseArray longSparseArray;
        SparseBooleanArray sparseBooleanArray;
        SparseIntArray sparseIntArray;
        SparseLongArray  sparseLongArray;
        SparseArray sparseArray;

        EnumTest test = EnumTest.MON;
        switch(test){
            case FRI:
                break;
            case MON:
                break;
            case SAT:
                break;
            case TUE:
                break;
            case WED:
                break;
            case THU:
                break;
            case SUN:
                break;
        }

    }

    private void init() {
        System.out.println("test_println");
        System.err.println("error_println");
        System.err.print("error_print");
        System.out.print("out_print");

        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();

        ToastUtils.showToast(this,"Test ToastUtil");

        android.widget.Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        Log.d("test lint", "msg");

        new Message();
        Message.obtain();
        handler.obtainMessage();
        handler.sendEmptyMessage(1);
        getLayoutInflater().inflate(R.layout.time, (ViewGroup) findViewById(R.id.activity_main));
    }

    private void initViews() {
        btnClick = (Button) findViewById(R.id.One);
        btnClick.setOnClickListener(this);
        tvShow = (TextView) findViewById(R.id.Show);
        tvShow.setText("欧阳鹏");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.One:
                showText();
                break;
        }
    }

    private void showText() {

        LintTestBean lintTestBean = new LintTestBean("xsf", "666");
        tvShow.setText(lintTestBean.name + "\n" +
                lintTestBean.age + "\n");

    }


    private void test() {
        // TODO自动生成的方法存根
        byte[] buffer = new byte[512];   //一次取出的字节数大小,缓冲区大小
        int numberRead = 0;
        FileInputStream input = null;
        FileOutputStream out = null;
        try {
            input = new FileInputStream("D:/tiger.jpg");
            out = new FileOutputStream("D:/tiger2.jpg"); //如果文件不存在会自动创建
            while ((numberRead = input.read(buffer)) != -1) {  //numberRead的目的在于防止最后一次读取的字节小于buffer长度，
                out.write(buffer, 0, numberRead);       //否则会自动被填充0
            }
        } catch (IOException e) {
            // TODO自动生成的 catch 块
            e.printStackTrace();
            Log.getStackTraceString(e);
            try {
                if (input != null)
                    input.close();
                if (out != null)
                    out.close();
            } catch (IOException ex) {
                // TODO自动生成的 catch 块
                ex.printStackTrace();
            }
        } finally {
            try {
                if (input != null)
                    input.close();
                if (out != null)
                    out.close();
            } catch (IOException ex) {
                // TODO自动生成的 catch 块
                ex.printStackTrace();
            }
        }
    }
}
