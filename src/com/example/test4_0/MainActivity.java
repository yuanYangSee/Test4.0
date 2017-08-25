package com.example.test4_0;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener
{
	private static String TAG = "Debug";
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private Button btn_reset;
	private Button btn_get_max_lnu;
	private Button btn_open_device;
	private Button btn_verify;
	private Button btn_data_up;
	private TextView mTvInfo;
	private Button btn_test2;

	private UsbManager mUsbManager;
	private UsbDevice mUsbDevice;

	private PendingIntent mPermissionIntent;

	private boolean mSensorInited = false;

	//	private final int mVendorID = 0x2109; // 190
	//	private final int mProductID = 0x7638;

	
		private final int mVendorID = 8210; //big 
		private final int mProductID =8209;
	 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setViewContent();
		registerUSBpermisson(this.getApplicationContext()); /* ע�����USB */
	}

	// ���ؽ���
	private void setViewContent()
	{
		btn_reset = (Button) findViewById(R.id.btn_reset);
		btn_get_max_lnu = (Button) findViewById(R.id.btn_get_max_lnu);
		btn_open_device = (Button) findViewById(R.id.btn_open_device);
		btn_verify = (Button) findViewById(R.id.btn_verify);
		btn_data_up=(Button)findViewById(R.id.btn_data_up);
		btn_test2=(Button)findViewById(R.id.test2);

		mTvInfo = (TextView) findViewById(R.id.ReturnData);
		mTvInfo.setMovementMethod(ScrollingMovementMethod.getInstance());

		btn_reset.setOnClickListener(this);
		btn_get_max_lnu.setOnClickListener(this);
		btn_open_device.setOnClickListener(this);
		btn_verify.setOnClickListener(this);
		btn_data_up.setOnClickListener(this);
		btn_test2.setOnClickListener(this);

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
	}

	// ע�����USB
	public void registerUSBpermisson(Context context)
	{
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);// �γ�USB
		context.registerReceiver(mUsbReceiverPermission, filter);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.btn_open_device:
			Log.d(TAG, "R.id.OpenDevice");
			mSensorInited = InitUsbDevice(MainActivity.this, mVendorID, mProductID);
			break;

		case R.id.btn_verify:
			if (mSensorInited)
			{
				Log.d(TAG, "R.id.btn_verify");
				int nRet = DeviceIO.HS_Verfiy(mUsbDevice);
				if (0 == nRet)
				{
					DeviceIO.showToast(MainActivity.this, "����ɹ���", Toast.LENGTH_SHORT);
				} else
				{
					DeviceIO.showToast(MainActivity.this, "���顣", Toast.LENGTH_SHORT);
				}
			} else
			{
				DeviceIO.showToast(MainActivity.this, "�豸δ�򿪣�����豸��", Toast.LENGTH_SHORT);
			}
			break;

		case R.id.btn_get_max_lnu:
			if (mSensorInited)
			{
				int number = DeviceIO.getMaxLnu();
				String str1 = mTvInfo.getText().toString();
				str1 += "\nget_max_lun:" + Integer.toString(number);
				mTvInfo.setText(str1);
			} else
			{
				DeviceIO.showToast(MainActivity.this, "�豸δ�򿪣�����豸��", Toast.LENGTH_SHORT);
			}
			break;

		case R.id.btn_reset:
			if (mSensorInited)
			{
				Log.d(TAG, "R.id.btn_reset");
				DeviceIO.DeviceReset(MainActivity.this);
			} else
			{
				DeviceIO.showToast(MainActivity.this, "�豸δ�򿪣�����豸��", Toast.LENGTH_SHORT);
			}
			break;
			
		case R.id.btn_data_up:
			Log.d(TAG, " R.id.btn_data_up");
			
			if (mSensorInited)
			{
				String str = mTvInfo.getText().toString();
				
				//byte[] FpArray = new byte[65536];
				byte[] FpArray = new byte[65536];
				
				int result = DeviceIO.UpImage(mUsbDevice, FpArray, FpArray.length);
				if ( 0==result)
                {
					Log.d(TAG, "DeviceIO UpImage OK."); 
                }else 
				{
                	Log.d(TAG, "DeviceIO UpImage failed."); 
                	break;
				}

				/*for(int i=0; i<FpArray.length; i++)
				{ str += Integer.toHexString(FpArray[i]&0x00FF) + " "; }    
				str += "\n"; 
				mTvInfo.setText(str);*/
				Log.d(TAG, "FpArray="+Arrays.toString(FpArray));

			} else
			{
				DeviceIO.showToast(MainActivity.this, "�豸δ�򿪣�����豸��", Toast.LENGTH_SHORT);
			}
			
			break;
			
		case R.id.test2:
			Log.d(TAG, "R.id.test2"); 
			ByteBuffer Buffer = ByteBuffer.allocate(36);
			byte[] FpArray = new byte[65536];
			System.arraycopy(Buffer.array(), 0, FpArray, 0, 36);
			break;
			
		default:
			break;
		}
	}

	private boolean InitUsbDevice(Context mContext, int vid, int pid)
	{
		boolean isSucceed = false;
		mUsbDevice = DeviceIO.OpenDevice(mContext, vid, pid);
		if (mUsbDevice == null)
		{
			return false;
		}
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// �ж��Ƿ�ӵ��Ȩ��
		if (mUsbDevice != null && !mUsbManager.hasPermission(mUsbDevice))
		{
			Log.d(TAG, "����Ȩ��");
			mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
			try
			{
				Thread.sleep(5000);// ��ʱ5s���ȴ��û�ѡ��
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		} else
		{
			isSucceed = true;
			Log.d(TAG, "����Ȩ�ޡ���ʼ���ɹ�!");
			Toast.makeText(MainActivity.this, "Correct device!", Toast.LENGTH_SHORT).show();
		}
		return isSucceed;
	}

	// �㲥
	private final BroadcastReceiver mUsbReceiverPermission = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context arg0, Intent intent)
		{
			// TODO Auto-generated method stub
			// ��ȡ����Activity��USB�豸
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action))
			{
				// ����USBȨ��
				synchronized (this)
				{
					UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (usbDevice != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
						// �û�ͬ�� ;
						mSensorInited = true;
					} else// �û��ܾ�
					{
						mSensorInited = false;
						Log.e(TAG, "�û��ܾ���Ȩ.");
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
			{
				mSensorInited = false;
				// �ر��豸���ͷ���Դ
				DeviceIO.CloseDevice(mUsbDevice);
				DeviceIO.showToast(MainActivity.this, "USB�ɼ��豸�Ѱγ���", Toast.LENGTH_SHORT);
			}
		}
	};

}
