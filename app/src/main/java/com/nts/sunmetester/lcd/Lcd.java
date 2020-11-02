/*
 * Author Konstantin Zaharov
 * Copyright (c) 2020. Simple sunMe implementation. Important! -device  system version should be non less then 1.5.3
 * If you need to change something - do it!
 */

package com.nts.sunmetester.lcd;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.nts.sunmetester.R;
import com.sunmi.peripheral.printer.InnerLcdCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class Lcd implements Runnable {
    private SunmiPrinterService sunmiPrinterService;
    private Handler handler;
    private final String TEXT;
    // uses for init display
    private final byte[] LCD;
    private final int MSG;
    private final Context context;
    private final String LCD_OK;
    private final String LCD_ERROR;


    public Lcd(String text, SunmiPrinterService sunmiPrinterService, Handler handler, Context context) {
        this.TEXT = text;
        this.sunmiPrinterService = sunmiPrinterService;
        this.handler = handler;
        this.context = context;
        this.MSG = Integer.parseInt(context.getResources().getString(R.string.msg));
        this.LCD_OK = context.getResources().getString(R.string.lcdOk);
        this.LCD_ERROR = context.getResources().getString(R.string.lcdError);
        byte init = (byte) Integer.parseInt(context.getResources().getString(R.string.lcdInit));
        byte clean = (byte) Integer.parseInt(context.getResources().getString(R.string.lcdClean));
        byte ready = (byte) Integer.parseInt(context.getResources().getString(R.string.lcdReady));
        this.LCD = new byte[]{init, clean, ready};
    }

    @Override
    public void run() {

        if (sunmiPrinterService != null) {
            try {

                // init LCD firstly
                for (byte b : LCD) {
                    sunmiPrinterService.sendLCDCommand(b);
                }

                // send LCD data, and prepare lcd strings to the 3*20 chars format
                LcdUtils lcdUtils = new LcdUtils(TEXT, context);
                sunmiPrinterService.sendLCDMultiString(lcdUtils.getLCD_LINES(), lcdUtils.getLCD_FONT_SIZE(), new InnerLcdCallback() {

                    // use call back for read lcd answer
                    @Override
                    public void onRunResult(boolean b) throws RemoteException {
                        sendMsg((b) ? LCD_OK : LCD_ERROR);
                    }
                });
            } catch (RemoteException e) {
                sendMsg(e.toString());
            }
        }
    }

    private void sendMsg(String s) {
        Message message = handler.obtainMessage(MSG, s);
        handler.sendMessage(message);
    }
}
