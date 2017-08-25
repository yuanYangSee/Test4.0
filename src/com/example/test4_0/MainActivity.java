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
		registerUSBpermisson(this.getApplicationContext()); /* 注册监听USB */
	}

	// 加载界面
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

	// 注册监听USB
	public void registerUSBpermisson(Context context)
	{
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);// 拔出USB
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
					DeviceIO.showToast(MainActivity.this, "核验成功。", Toast.LENGTH_SHORT);
				} else
				{
					DeviceIO.showToast(MainActivity.this, "核验。", Toast.LENGTH_SHORT);
				}
			} else
			{
				DeviceIO.showToast(MainActivity.this, "设备未打开，请打开设备。", Toast.LENGTH_SHORT);
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
				DeviceIO.showToast(MainActivity.this, "设备未打开，请打开设备。", Toast.LENGTH_SHORT);
			}
			break;

		case R.id.btn_reset:
			if (mSensorInited)
			{
				Log.d(TAG, "R.id.btn_reset");
				DeviceIO.DeviceReset(MainActivity.this);
			} else
			{
				DeviceIO.showToast(MainActivity.this, "设备未打开，请打开设备。", Toast.LENGTH_SHORT);
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
				DeviceIO.showToast(MainActivity.this, "设备未打开，请打开设备。", Toast.LENGTH_SHORT);
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

		// 判断是否拥有权限
		if (mUsbDevice != null && !mUsbManager.hasPermission(mUsbDevice))
		{
			Log.d(TAG, "申请权限");
			mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
			try
			{
				Thread.sleep(5000);// 延时5s，等待用户选择
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		} else
		{
			isSucceed = true;
			Log.d(TAG, "已有权限。初始化成功!");
			Toast.makeText(MainActivity.this, "Correct device!", Toast.LENGTH_SHORT).show();
		}
		return isSucceed;
	}

	// 广播
	private final BroadcastReceiver mUsbReceiverPermission = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context arg0, Intent intent)
		{
			// TODO Auto-generated method stub
			// 获取启动Activity的USB设备
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action))
			{
				// 申请USB权限
				synchronized (this)
				{
					UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (usbDevice != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
						// 用户同意 ;
						mSensorInited = true;
					} else// 用户拒绝
					{
						mSensorInited = false;
						Log.e(TAG, "用户拒绝授权.");
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
			{
				mSensorInited = false;
				// 关闭设备，释放资源
				DeviceIO.CloseDevice(mUsbDevice);
				DeviceIO.showToast(MainActivity.this, "USB采集设备已拔出。", Toast.LENGTH_SHORT);
			}
		}
	};

}
