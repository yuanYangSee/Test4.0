package com.example.test4_0;

import java.nio.ByteBuffer;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.widget.Toast;

public class DeviceIO
{
	private static final String TAG = "DeviceIO";
	private static Toast mToast = null;
	private static final DeviceDatas Datas = new DeviceDatas();
	
	//���豸
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
	
	//�ر��豸
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
	
	// ����֪ͨ
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
	
	//��λ
	public static void DeviceReset(Context mContext)
	{
		try
		{
			Log.d(TAG, "DeviceReset");
			Datas.reset(mContext);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//��ȡ�߼���Ԫ��
	public static int getMaxLnu()
	{
		int number=Datas.getMaxLnu();
		return number;
	}
	
	//�����豸
	public static int HS_Verfiy(UsbDevice device)
	{
		int nRet = -1;
		boolean isConnected = false;
		isConnected = Datas.CheckConnection(device);
		if (isConnected)
		{
			nRet = Datas.UDiskVerfiy();
			Log.d(TAG, "UDiskVerfiy return="+nRet);
		}
		return nRet;
	}
	
	// �ϴ�ͼ��
	public static int UpImage(UsbDevice device, byte[] pImageData, int iImageLength)
	{
		int nRet = -1;
		boolean isConnected = false;
		isConnected = Datas.CheckConnection(device);
		if (isConnected)
		{
			// ByteBuffer Buffer = ByteBuffer.allocate(92160);
			
			//ʵ������
			ByteBuffer Buffer = ByteBuffer.allocate(16384);//����65536����	64K
			nRet = Datas.UdiskUpImage(Buffer, Buffer.capacity());
			if (nRet == 0)//�ص�
			{	
				//���Ƹ�65536
				System.arraycopy(Buffer.array(), 0, pImageData, 0, 36);
			} else
			{
				Log.e("AS60xIO", "UdiskUpImage error�� nRet=" + nRet);
			}
			Buffer.clear();

		} else
		{
			nRet = -3;
			Log.e(TAG, "CheckConnection error�� nRet=" + nRet);
		}
		return nRet;
	}
	
	
}
