package com.example.test4_0;

import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

public class DeviceDatas
{
	private static final String TAG = "DeviceDatas";
	
	private UsbDeviceConnection mConnection = null;
	private UsbManager mUsbManager = null;
	private UsbEndpoint mEndpointIn = null;
	private UsbEndpoint mEndpointOut = null;

	// �ر����ӣ��ͷ���Դ
	void CloseConnection(UsbDevice device)
	{
		if ((this.mConnection != null) && (device != null))
		{
			this.mConnection.releaseInterface(device.getInterface(0));
			this.mConnection.close();
			this.mConnection = null;
			Log.d(TAG, "�ر����ӣ��ͷ���Դ");
		}

	}

	boolean CheckConnection(UsbDevice device)
	{
		boolean ret = false;
		try
		{
			if ((this.mConnection == null) && (device != null))
			{
				this.mConnection = GetConnection(device);
				if (this.mConnection == null)
				{
					Log.d(TAG, "mConnection==null");
					return false;
				}
				Log.d(TAG, "mConnection��Ϊnull");
				this.mConnection.claimInterface(device.getInterface(0), false);
				ret = true;
			}
		} catch (SecurityException e)
		{
			Log.e(TAG, "java.lang.SecurityException e: CheckConnection!!!");
		}

		return ret;
	}
	
	private UsbDeviceConnection GetConnection(UsbDevice device)
	{
		UsbDeviceConnection connection = null;
		if (device == null)
		{
			Log.d(TAG, "Please insert USB flash disk!");
			return null;
		}
		if ((device != null) && (this.mUsbManager != null) && (this.mUsbManager.hasPermission(device)))
		{
			UsbInterface intf = device.getInterface(0);
			connection = this.mUsbManager.openDevice(device);
			if ((connection == null) || (!connection.claimInterface(intf, true)))
			{
				Log.e(TAG, "connection == null,open device failed!!");
				connection = null;
			}
		} else
		{
			Log.e("TAG", "usb has not Permission !");
		}
		return connection;
	}


	
	

	// ���豸����ȡ�˵㡣�������ӡ�
	UsbDevice GetDevice(Context mContext, int VendorId, int ProductId)
	{
		UsbDevice mdevice = null;
		Log.d(TAG, "getSystemService(Context.USB_SERVICE)");
		this.mUsbManager = ((UsbManager)mContext.getSystemService("usb"));
		if (this.mUsbManager == null)
		{
			Log.d(TAG, "mUsbManager == null return!!!");
			return null;
		}
		Log.d(TAG, "mUsbManager=" + this.mUsbManager);
		HashMap<String, UsbDevice> deviceList = this.mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while (deviceIterator.hasNext())
		{
			UsbDevice device = null;
			device = deviceIterator.next();
			String devInfo = device.getDeviceName() + "(" + device.getVendorId() + ":" + device.getProductId() + ")";
			Log.e(TAG, devInfo);
			if (VendorId == device.getVendorId() && ProductId == device.getProductId())
			{
				mdevice = device;
				Log.d(TAG, "mdevice found!");
				break;
			}
		}
		if(mdevice==null)
		{
			Log.d(TAG, "mdevice==null");
		}
		return mdevice;
	}

	// ��������
	public boolean makeConnection(Context mContext,UsbDevice device)
	{
		boolean isConnected = false;
		if (device == null)
		{
			Log.d(TAG, "Please insert USB flash disk!");
			Toast.makeText(mContext, "Please insert USB flash disk!", Toast.LENGTH_SHORT).show();
			return false;
		}
		// U�̽ӿڸ���Ϊ1
		if (device.getInterfaceCount() != 1)
		{
			Log.d(TAG, "Not a USB flash disk!");
			Toast.makeText(mContext, "Not a USB flash disk!", Toast.LENGTH_SHORT).show();
			return false;
		}

		UsbInterface intf = device.getInterface(0);

		// U�̽ӿ�0�ɻ�ȡ�Ķ˵���Ϊ2
		if (intf.getEndpointCount() != 2)
		{
			Log.d(TAG, "Not a USB flash disk!");
			Toast.makeText(mContext, "Not a USB flash disk!", Toast.LENGTH_SHORT).show();
			return false;
		} else
		{
			mEndpointIn = intf.getEndpoint(0); // Bulk-In�˵�
			mEndpointOut = intf.getEndpoint(1); // Bulk_Out�˵�
			Log.d(TAG, "�豸�ǿգ���ȡ��In�Լ�OUT�˵㡣");
		}

		if (device != null)
		{
			UsbDeviceConnection connection = mUsbManager.openDevice(device);
			if (connection != null && connection.claimInterface(intf, true))
			{
				Log.d(TAG, "���ӷǿգ�Make connection succeeded!");
				Toast.makeText(mContext, "Make connection succeeded!", Toast.LENGTH_SHORT).show();
				mConnection = connection;
				isConnected = true;
			} else
			{
				Log.d(TAG, "Make connection failed!");
				Toast.makeText(mContext, "Make connection failed!", Toast.LENGTH_SHORT).show();
				mConnection = null;
				return false;
			}
		}
		return isConnected;
	}
}
