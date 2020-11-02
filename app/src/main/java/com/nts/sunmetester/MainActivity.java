/*
 * Author Konstantin Zaharov
 * Copyright (c) 2020. Simple sunMe implementation. Important! -device  system version should be non less then 1.5.3
 * If you need to change something - do it!
 */

package com.nts.sunmetester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nts.sunmetester.lcd.Lcd;
import com.nts.sunmetester.myPrinter.MyPrinter;
import com.nts.sunmetester.myPrinter.Task;
import com.sunmi.peripheral.printer.ExceptionConst;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {


    private static int NoSunmiPrinter = 0x00000000;
    private static int CheckSunmiPrinter = 0x00000001;
    private static int FoundSunmiPrinter = 0x00000002;
    private static int LostSunmiPrinter = 0x00000003;



    /**
     * sunmiPrinter means checking the printer connection status
     */
    public int sunmiPrinter = CheckSunmiPrinter;
    private SunmiPrinterService sunmiPrinterService;


    public MainActivity() {

    }

    private InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            sunmiPrinterService = service;
            checkSunmiPrinterService(service);
        }

        @Override
        protected void onDisconnected() {
            sunmiPrinterService = null;
            sunmiPrinter = LostSunmiPrinter;
        }
    };

    /**
     * init sunmi print service
     */
    public void initSunmiPrinterService(Context context) {
        try {
            boolean ret = InnerPrinterManager.getInstance().bindService(context,
                    innerPrinterCallback);
            if (!ret) {
                sunmiPrinter = NoSunmiPrinter;
            }
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }


    /**
     * Check the printer connection,
     * like some devices do not have a printer but need to be connected to the cash drawer through a print service
     */
    private void checkSunmiPrinterService(SunmiPrinterService service) {
        boolean ret = false;
        try {
            ret = InnerPrinterManager.getInstance().hasPrinter(service);
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
        sunmiPrinter = ret ? FoundSunmiPrinter : NoSunmiPrinter;
    }

    /**
     * Some conditions can cause interface calls to fail
     * For example: the version is too low、device does not support
     * You can see {@link ExceptionConst}
     * So you have to handle these exceptions
     */
    private void handleRemoteException(RemoteException e) {
        //TODO process when get one exception
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSunmiPrinterService(this);

        EditText userText = findViewById(R.id.userText);
        Button btnOk = findViewById(R.id.btnOk);

        TextView txtLog = findViewById(R.id.txtLog);

        @SuppressLint("HandlerLeak")
        Handler handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                try {
                    DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
                    txtLog.append('\n' + timeFormat.format(new Date()) + " " + msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        RadioGroup rbGroup = findViewById(R.id.rbGroupe);

        btnOk.setOnClickListener(v -> {
                    if (sunmiPrinterService != null) {
                        Context context = getApplicationContext();
                        RadioButton radioButton = findViewById(rbGroup.getCheckedRadioButtonId());
                        switch (radioButton.getId()) {
                            case R.id.rbPrintText: {
                                List<Task> taskList = new ArrayList<>();
                                taskList.add(Task.PRINT);
                                taskList.add(Task.QR);
                                taskList.add(Task.CUT);
                                Runnable task = new MyPrinter(userText.getText().toString(), sunmiPrinterService, handler, taskList, context);
                                new Thread(task).start();
                                break;
                            }

                            case R.id.rbShowDisplay: {
                                Runnable task = new Lcd(userText.getText().toString(), sunmiPrinterService, handler, context);
                                new Thread(task).start();
                                break;
                            }

                            case R.id.rbPrintQrCode: {
                                List<Task> taskList = new ArrayList<>();
                                taskList.add(Task.QR);
                                taskList.add(Task.CUT);
                                Runnable task = new MyPrinter(userText.getText().toString(), sunmiPrinterService, handler, taskList, context);
                                new Thread(task).start();
                                break;
                            }

                            case R.id.rbCutPaper: {
                                List<Task> taskList = new ArrayList<>();
                                taskList.add(Task.CUT);
                                Runnable task = new MyPrinter(userText.getText().toString(), sunmiPrinterService, handler, taskList, context);
                                new Thread(task).start();
                                break;
                            }

                            case R.id.rbOpenCashDraw: {
                                List<Task> taskList = new ArrayList<>();
                                taskList.add(Task.CD);
                                Runnable task = new MyPrinter(userText.getText().toString(), sunmiPrinterService, handler, taskList, context);
                                new Thread(task).start();
                                break;
                            }

                            case R.id.rbPrintSystemInfo: {
                                List<Task> taskList = new ArrayList<>();
                                taskList.add(Task.INFO);
                                taskList.add(Task.CUT);
                                Runnable task = new MyPrinter(userText.getText().toString(), sunmiPrinterService, handler, taskList, context);
                                new Thread(task).start();
                                break;
                            }

                            case R.id.rbPrintTestReport: {
                                List<Task> taskList = new ArrayList<>();
                                taskList.add(Task.REPORT);
                                taskList.add(Task.QR);
                                taskList.add(Task.CUT);
                                Runnable task = new MyPrinter(userText.getText().toString(), sunmiPrinterService, handler, taskList, context);
                                new Thread(task).start();
                                break;
                            }
                        }
                    }
                }
        );
    }
}




