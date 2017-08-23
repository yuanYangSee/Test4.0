package com.example.test4_0;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.widget.Toast;

public class DeviceIO
{
	private static final String TAG = "DeviceIO";
	private static Toast mToast = null;
	private static final DeviceDatas Datas = new DeviceDatas();
	
	//打开设备
	public static UsbDevice OpenDevice(Context mContext, int VendorId, int ProductId)
	  {
	    UsbDevice mdevice = null;
	    mdevice = Datas.GetDevice(mContext, VendorId, ProductId);
	    boolean flag=Datas.makeConnection(mContext, mdevice);
	    if (!flag)
		{
			Log.d(TAG, "Sorry, GetUsbEndpoints failed!!!");
			return null;
		}
		Log.d(TAG, "GetUsbEndpoints Succeed!!!");
	    return mdevice;
	  }
	
	//关闭设备
	public static void CloseDevice(UsbDevice device)
	  {
		Log.d(TAG, "CloseDevice");
	    boolean isConnected = false;
	    isConnected = Datas.CheckConnection(device);
	    if (isConnected)
	    {
	      Datas.CloseConnection(device);
	      device = null;
	    }
	  }
	
	// 弹出通知
	public static void showToast(Context context, String msg, int duration)
	{
		if (mToast == null)
		{
			mToast = Toast.makeText(context, msg, duration);
		} else
		{
			mToast.setText(msg);
		}
		mToast.show();
	}
	
	//复位
	public static void DeviceReset(Context mContext)
	{
		try
		{
			Datas.reset(mContext);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
}
