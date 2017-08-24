package com.example.test4_0;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
		Log.d(TAG, "CloseConnection");
		if ((this.mConnection != null) && (device != null))
		{
			this.mConnection.releaseInterface(device.getInterface(0));
			this.mConnection.close();
			this.mConnection = null;
			Log.d(TAG, "�ر����ӣ��ͷ���Դ");
		}

	}
	
	//������ӣ�Ҳ�Ǳ�֤�ѻ�ȡ���ӣ�
	boolean CheckConnection(UsbDevice device)
	{
		Log.d(TAG, "CheckConnection");
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
				return ret;
			}
			
		} catch (SecurityException e)
		{
			Log.e(TAG, "java.lang.SecurityException e: CheckConnection!!!");
		}
		ret=true;
		Log.d(TAG, "mConnection��Ϊnull");
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

	// ��ȡ�豸��
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
	
	//����
	public void reset(Context mContext)
	{
		synchronized (this)
		{
			if (mConnection != null)
			{
				// ��λ�����������USB Mass Storage�Ķ����ĵ�����
				int result = mConnection.controlTransfer(0x21, 0xFF, 0x00, 0x00, null, 0, 1000);
				if (result < 0)
				{ // result<0˵������ʧ��
					Log.d(TAG, "Send reset command failed!");
					DeviceIO.showToast(mContext, "��λʧ��", Toast.LENGTH_SHORT);
				} else
				{
					Log.d(TAG, "Send reset command succeeded!");
					DeviceIO.showToast(mContext, "��λ�ɹ�", Toast.LENGTH_SHORT);
				}
			}
			else 
			{
				Log.d(TAG, "mConnection");
			}
			
		}
	}
	
	// ��ȡ�߼���Ԫ��
	public byte getMaxLnu()
	{
		byte i=0;
		synchronized (this)
		{
			if (mConnection != null)
			{
				// ���յ�����ֻ��1���ֽ�
				byte[] message = new byte[1];
				// ��ȡ���LUN�����������USB Mass Storage�Ķ����ĵ�����
				int result = mConnection.controlTransfer(0xA1, 0xFE, 0x00, 0x00, message, 1, 1000);
				if (result < 0)
				{
					Log.d(TAG, "Get max lnu failed!");
				} else
				{
					Log.d(TAG, "Get max lnu succeeded!");
					i=message[0] ;
				}
			}
		}
		return i;
	}
	
	//����У��
	int UDiskVerfiy()
	{
		
		ByteBuffer buffer = ByteBuffer.allocate(65536);

		Byte cmd[] ={ 0x13, 0x00, 0x00, 0x00, 0x00, 0x00 };

		if (UDiskDownData(cmd, 1000)!=0)
		{
			return -101;
		}
		return 0;
	}

	private int UDiskDownData(Byte[] cmd , int nTimeOut)
	{
		int ret = -1;
		int i = 0;
		byte[] di_CSW = new byte[13];
		
		byte[] do_CBW = new byte[]
				{	    (byte) 0x55, (byte) 0x53, (byte) 0x42, (byte) 0x43, // �̶�ֵ 0~3
						(byte) 0x28, (byte) 0xe8, (byte) 0x3e, (byte) 0xfe, // 4~7�Զ���,�뷵�ص�CSW�е�ֵ��һ����
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 8~11�������ݳ���Ϊ512�ֽ�
						(byte) 0x00, 										// (12th 0x00-out)
						(byte) 0x00, 										// 13th LNUΪ0,����Ϊ0
						(byte) 0x06, 										// 14th	����� command length
						(byte) cmd[0], (byte) cmd[1], (byte) cmd[2], (byte)cmd[3], // READ	FORMAT CAPACITIES,�����0x00�Ա�����
						(byte) cmd[4], (byte) cmd[5], (byte) 0x00, (byte) 0x00,	//				
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		if ((this.mEndpointOut != null) && (this.mConnection != null))
		{
			ret = this.mConnection.bulkTransfer(this.mEndpointOut, do_CBW, 31, nTimeOut);
		}

		if (ret != 31)
		{
			Log.e(TAG, "1...UDiskDownData DO_CBW fail!\n");
			return -301;
		}

		if ((this.mEndpointIn != null) && (this.mConnection != null))
		{
			ret = this.mConnection.bulkTransfer(this.mEndpointIn, di_CSW, 13, nTimeOut);
		} 

		if ((di_CSW[3] != 83) || (di_CSW[12] != 0))
		{
			Log.e(TAG, "2...UDiskDownData DI_CSW fail!\n");
			return -303;
		}
		for (i = 4; i < 8; i++)
		{
			if (di_CSW[i] == do_CBW[i])
				continue;
			Log.e("AS60xDatas", "4...UDiskDownData DI_CSW fail!\n");
			return -303;
		}
		
		Log.d(TAG, "2 UDiskDownData="+Arrays.toString(di_CSW));
		return 0;
	}
	
	

	private int UDiskGetData(Byte[] cmd, int img_Len, ByteBuffer img,  int nTimeOut )
	{
		int ret = -1;
		int i = 0;
		byte[] recvbuffer = new byte[65536];//64K
		byte[] di_CSW = new byte[13];
		
		byte[] do_CBW = new byte[]
				{	    (byte) 0x55, (byte) 0x53, (byte) 0x42, (byte) 0x43, // �̶�ֵ 0~3
						(byte) 0x28, (byte) 0xe8, (byte) 0x3e, (byte) 0xfe, // 4~7�Զ���,�뷵�ص�CSW�е�ֵ��һ����
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 8~11�������ݳ���Ϊ512�ֽ�
						(byte) 0x80, 										// (12th 0x80-in)
						(byte) 0x00, 										// 13th LNUΪ0,����Ϊ0
						(byte) 0x06, 										// 14th	����� command length
						(byte) cmd[0], (byte) cmd[1], (byte) cmd[2], (byte)cmd[3], // READ	FORMAT CAPACITIES,�����0x00�Ա�����
						(byte) cmd[4], (byte) cmd[5], (byte) 0x00, (byte) 0x00,	//				
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		do_CBW[8] = (byte) (img_Len & 0xFF);
		do_CBW[9] = (byte) (img_Len >> 8 & 0xFF);
		do_CBW[10] = (byte) (img_Len >> 16 & 0xFF);
		do_CBW[11] = (byte) (img_Len >> 24 & 0xFF);
		ret = mConnection.bulkTransfer(mEndpointOut, do_CBW, do_CBW.length, nTimeOut);
		if (ret != 31)
	    {
	      Log.e(TAG, "1...UDiskGetData DO_CBW fail!\n");
	      return -311;
	    }
		
		if ((this.mEndpointIn != null) && (this.mConnection != null))
		{
			ret = this.mConnection.bulkTransfer(this.mEndpointIn, recvbuffer, recvbuffer.length, nTimeOut);
		}
		if (ret != recvbuffer.length)
		{
			Log.e(TAG, "2...UDiskGetData DI_DATA fail! ret=" + ret);
			return -312;
		}
		img.put(recvbuffer, 0, recvbuffer.length);
		
		if ((this.mEndpointIn != null) && (this.mConnection != null))
		{
			ret = this.mConnection.bulkTransfer(this.mEndpointIn, di_CSW, 13, nTimeOut);
		} 

		if ((di_CSW[3] != 83) || (di_CSW[12] != 0))
		{
			Log.e(TAG, "2...UDiskDownData DI_CSW fail!\n");
			return -303;
		}
		for (i = 4; i < 8; i++)
		{
			if (di_CSW[i] == do_CBW[i])
				continue;
			Log.e(TAG, "4...UDiskDownData DI_CSW fail!\n");
			return -303;
		}
		return 0;
	}
	
}
