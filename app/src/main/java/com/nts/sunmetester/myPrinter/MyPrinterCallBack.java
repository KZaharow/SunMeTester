/*
 * Author Konstantin Zaharov
 * Copyright (c) 2020. Simple sunMe implementation. Important! -device  system version should be non less then 1.5.3
 * If you need to change something - do it!
 */

package com.nts.sunmetester.myPrinter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.nts.sunmetester.R;
import com.sunmi.peripheral.printer.InnerResultCallbcak;

public class MyPrinterCallBack extends InnerResultCallbcak {

    private final int MSG;
    private Handler handler;

    // define printer callBack data strings
    private final String RESULT_OK;
    private final String RESULT_ERROR;
    private final String RESPONSE_DESCRIPTION;
    private final String ERROR_CODE;
    private final String ERROR_DESCRIPTION;
    private final String RESULT_CODE;
    private final String RESULT_DESCRIPTION;

    MyPrinterCallBack(Handler handler, Context context) {
        this.handler = handler;
        this.RESULT_OK = context.getResources().getString(R.string.callBackOK);
        this.RESULT_ERROR = context.getResources().getString(R.string.callBackError);
        this.RESPONSE_DESCRIPTION = context.getResources().getString(R.string.callBackResponseDescription);
        this.ERROR_CODE = context.getResources().getString(R.string.callBackErrorCode);
        this.ERROR_DESCRIPTION = context.getResources().getString(R.string.callBackErrorDescription);
        this.RESULT_CODE = context.getResources().getString(R.string.callBackResultCode);
        this.RESULT_DESCRIPTION = context.getResources().getString(R.string.callBackResultDescription);
        this.MSG = Integer.parseInt(context.getResources().getString(R.string.msg));
    }

    @Override
    public void onRunResult(boolean b) {
        sendMsg((b) ? RESULT_OK : RESULT_ERROR);
    }

    @Override
    public void onReturnString(String s) {
        sendMsg(RESPONSE_DESCRIPTION + ' ' + s);
    }

    @Override
    public void onRaiseException(int i, String s) {
        sendMsg(ERROR_CODE + ' ' + i + ERROR_DESCRIPTION + ' ' + s);
    }

    @Override
    public void onPrintResult(int i, String s) {
        sendMsg(RESULT_CODE + ' ' + i + RESULT_DESCRIPTION + ' ' + s);
    }

    private void sendMsg(String s) {
        Message message = handler.obtainMessage(MSG, s);
        handler.sendMessage(message);
    }
}
