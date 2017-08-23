package com.example.test4_0;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

public class DeviceIO
{
	private static final String TAG = "DeviceIO";
	private static final DeviceDatas Datas = new DeviceDatas();
	
	
	public static UsbDevice OpenDevice(Context mContext, int VendorId, int ProductId)
	  {
	    UsbDevice mdevice = null;
	    mdevice = Datas.GetDevice(mContext, VendorId, ProductId);
	    boolean flag=Datas.makeConnection(mContext, mdevice);
	    if (!flag)
		{
			Log.d("TAG", "Sorry, GetUsbEndpoints failed!!!");
			return null;
		}
		Log.d("TAG", "GetUsbEndpoints Succeed!!!");
	    return mdevice;
	  }
	
	public static void CloseDevice(UsbDevice device)
	  {
	    boolean isConnected = false;
	    isConnected = Datas.CheckConnection(device);
	    if (isConnected)
	    {
	      Datas.CloseConnection(device);
	      device = null;
	    }
	  }
	
	
}
