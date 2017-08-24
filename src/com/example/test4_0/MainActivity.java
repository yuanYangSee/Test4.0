package com.example.test4_0;

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
	private static String TAG="Debug";
	private static final String ACTION_USB_PERMISSION =  
            "com.android.example.USB_PERMISSION";  
      
    private Button btn_reset;  
    private Button btn_get_max_lnu;  
    private Button btn_open_device;  
    private Button btn_verify;
    private Button btn_data_up;
    private TextView mTvInfo;  
  
    private UsbManager mUsbManager;  
    private UsbDevice mUsbDevice;  

    private PendingIntent mPermissionIntent;
    
    private boolean mSensorInited =false;
      
    private final int mVendorID = 0x2109;  //190
    private final int mProductID = 0x7638;  
    
   /* 	private final int mVendorID = 8210;  //big
    	private final int mProductID = 8209;  */
      

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setViewContent(); 
		registerUSBpermisson(this.getApplicationContext()); /*注册监听USB*/
	}
	
	//加载界面
		private void setViewContent()
		{
			 btn_reset = (Button)findViewById(R.id.btn_reset);  
		     btn_get_max_lnu = (Button)findViewById(R.id.btn_get_max_lnu);  
		     btn_open_device = (Button)findViewById(R.id.btn_open_device); 
		     btn_verify=(Button)findViewById(R.id.btn_verify);
		     
		     mTvInfo = (TextView)findViewById(R.id.ReturnData);  
		     mTvInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
		          
		     btn_reset.setOnClickListener(this);  
		     btn_get_max_lnu.setOnClickListener(this);  
		     btn_open_device.setOnClickListener(this);  
		     btn_verify.setOnClickListener(this);
		          
		     mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);  
		}
		
		//	注册监听USB
		public void registerUSBpermisson(Context context) 
		{
			  mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
			  IntentFilter filter = new IntentFilter();
			  filter.addAction(ACTION_USB_PERMISSION);
	          filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);//拔出USB
	          context.registerReceiver(mUsbReceiverPermission, filter);
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
					DeviceIO.showToast(MainActivity.this, "USB采集设备已拔出。",Toast.LENGTH_SHORT);
				}
			}
		};
	
	/*
	
	
	
	
	
	
	
	
	
	private void sendCommand() {  
	    String str = mTvInfo.getText().toString();  
	      
	    byte[] cmd = new byte[] {  
	        (byte) 0x55, (byte) 0x53, (byte) 0x42, (byte) 0x43, // 固定值 0~3 
	        (byte) 0x28, (byte) 0xe8, (byte) 0x3e, (byte) 0xfe, // 自定义,与返回的CSW中的值是一样的  4~7
	  //      (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节  
	          (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节  8~11
	        (byte) 0x00, // 传入数据  (12)
	        (byte) 0x00, // LNU为0,则设为0  
	        (byte) 0x02, // 命令长度为1  command length
	        (byte) 0x13, (byte) 0x00, (byte) 0x00, (byte) 0x00, // READ FORMAT CAPACITIES,后面的0x00皆被忽略  
	        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  
	        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  
	        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00  
	    };  
	    int result = mConnection.bulkTransfer(mEndpointOut, cmd, cmd.length, 1000);  
	    if(result < 0) {  
	        Log.d(TAG,  "Send command failed!");  
	        str += "Send command failed!\n";  
	    } else {  
	        Log.d(TAG, "Send command succeeded!");  
	        str += "Send command succeeded!\n";  
	    }  
	      
	    byte[] message = new byte[10];      //  需要足够的长度接收数据  
	    result = mConnection.bulkTransfer(mEndpointIn, message, message.length, 1000);  
	    if(result < 0) {  
	        Log.d(TAG,  "Receive message failed!");  
	        str += "Receive message failed!\n";  
	    } else {  
	        Log.d(TAG, "Receive message succeeded!");  
	        str += "Receive message succeeded!\nFormat capacities : \n";  
	        for(int i=0; i<message.length; i++) {  
	            str += Integer.toHexString(message[i]&0x00FF) + " ";  
	        }                 
	    }  
	      
	    byte[] csw = new byte[13];  
	    result = mConnection.bulkTransfer(mEndpointIn, csw, csw.length, 1000);  
	    if(result < 0) {  
	        Log.d(TAG,  "Receive CSW failed!");  
	        str += "\nReceive CSW failed!";  
	    } else {  
	        Log.d(TAG, "Receive CSW succeeded!");  
	        str += "\nReceive CSW succeeded!\nReceived CSW : ";  
	        for(int i=0; i<csw.length; i++) {  
	            str += Integer.toHexString(csw[i]&0x00FF) + " ";  
	        }                 
	    }  
	    str += "\n";  
	    mTvInfo.setText(str);  
	}  
	
	

	private void Verify() {  
	    String str = mTvInfo.getText().toString();  
	      
	    byte[] cmd = new byte[] {  
	        (byte) 0x55, (byte) 0x53, (byte) 0x42, (byte) 0x43, // 固定值 0~3 
	        (byte) 0x28, (byte) 0xe8, (byte) 0x3e, (byte) 0xfe, // 自定义,与返回的CSW中的值是一样的  4~7
	  //      (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节  
	          (byte) 0x24, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节  8~11
	        (byte) 0x80, //  (12th/in)
	        (byte) 0x00, // LNU为0,则设为0  
	        (byte) 0x06, // 命令长度为1  command length
	        (byte)0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, // READ FORMAT CAPACITIES,后面的0x00皆被忽略  
	        (byte) 0x24, (byte) 0x00, (byte) 0x00, (byte) 0x00,  
	        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  
	        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00  
	    };  
	    int result = mConnection.bulkTransfer(mEndpointOut, cmd, cmd.length, 1000);  
	    if(result < 0) {  
	        Log.d(TAG,  "Send command failed!");  
	        str += "Send command failed!\n";  
	    } else {  
	        Log.d(TAG, "Send UP command succeeded!");  
	        str += "\nSend Up command succeeded!\n";  
	    }  
	      
	    byte[] message = new byte[36];      //  需要足够的长度接收数据  
	    result = mConnection.bulkTransfer(mEndpointIn, message, message.length, 1000);  
	    if(result < 0) {  
	        Log.d(TAG,  "Receive message failed!");  
	        str += "Receive message failed!\n";  
	    } else {  
	        Log.d(TAG, "Receive message succeeded!");  
	        str += "Receive message succeeded!\nFormat capacities : \n";  
	        for(int i=0; i<message.length; i++) {  
	            str += Integer.toHexString(message[i]&0x00FF) + " ";  
	        }                 
	    }  
	      
	    byte[] csw = new byte[13];  
	    result = mConnection.bulkTransfer(mEndpointIn, csw, csw.length, 1000);  
	    if(result < 0) {  
	        Log.d(TAG,  "Receive CSW failed!");  
	        str += "\nReceive CSW failed!";  
	    } else {  
	        Log.d(TAG, "Receive CSW succeeded!");  
	        str += "\nReceive CSW succeeded!\nReceived CSW : ";  
	        for(int i=0; i<csw.length; i++) {  
	            str += Integer.toHexString(csw[i]&0x00FF) + " ";  
	        }                 
	    }  
	    str += "\n";  
	    mTvInfo.setText(str);  
	}  

	*/
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.btn_open_device:
			Log.d(TAG, "R.id.OpenDevice");
			mSensorInited = InitUsbDevice(MainActivity.this,mVendorID, mProductID);
			break;
			
		case R.id.btn_verify:
			Log.d(TAG, "R.id.btn_verify");
			int nRet = DeviceIO.HS_Verfiy(mUsbDevice);
			if (0 == nRet)
			{
				DeviceIO.showToast(MainActivity.this, "核验成功。", Toast.LENGTH_SHORT);
			}
			else 
			{
				DeviceIO.showToast(MainActivity.this, "核验失败。", Toast.LENGTH_SHORT);
			}
			break;
		
			
		case R.id.btn_get_max_lnu:
			int number=DeviceIO.getMaxLnu();
			String str1 = mTvInfo.getText().toString();
			str1 += "\nget_max_lun:"+Integer.toString(number);
			mTvInfo.setText(str1);
			break;	
			
		case R.id.btn_reset:
			Log.d(TAG, "R.id.btn_reset");
			DeviceIO.DeviceReset(MainActivity.this);
			break;
		default:
			break;
		}
	}
	
	
	
	private boolean InitUsbDevice (Context mContext,int vid, int pid)
	{
		boolean isSucceed = false;
		mUsbDevice=DeviceIO.OpenDevice(mContext, vid, pid);
		if(mUsbDevice==null)
		{
			return false;
		}
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// 判断是否拥有权限
		if (mUsbDevice!=null && !mUsbManager.hasPermission(mUsbDevice))
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

	
	
	
	/*private void Command_Down(int j)
	{
		String str = mTvInfo.getText().toString();

		byte[] cmd = new byte[]
		{ (byte) 0x55, (byte) 0x53, (byte) 0x42, (byte) 0x43, // 固定值 0~3
				(byte) 0x28, (byte) 0xe8, (byte) 0x3e, (byte) 0xfe, // 自定义,与返回的CSW中的值是一样的
																	// 4~7
				// (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, //
				// 传输数据长度为512字节
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节
																	// 8~11
				(byte) 0x00, // (12th/out)
				(byte) 0x00, // LNU为0,则设为0
				(byte) 0x02, // 命令长度为1 command length
				(byte) j, (byte) 0x00, (byte) 0x00, (byte) 0x00, // READ FORMAT
																	// CAPACITIES,后面的0x00皆被忽略
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		int result = mConnection.bulkTransfer(mEndpointOut, cmd, cmd.length, 1000);
		if (result < 0)
		{
			Log.d(TAG, "Send command failed!");
			str += "Send command failed!\n";
		} else
		{
			Log.d(TAG, "Send command succeeded!");
			str += "Send DOWN command succeeded!\n";
		}

		byte[] csw = new byte[13];
		result = mConnection.bulkTransfer(mEndpointIn, csw, csw.length, 1000);
		if (result < 0)
		{
			Log.d(TAG, "Receive CSW failed!");
			str += "\nReceive CSW failed!";
		} else
		{
			Log.d(TAG, "Receive CSW succeeded!");
			str += "Receive CSW succeeded!\nReceived CSW : ";
			for (int i = 0; i < csw.length; i++)
			{
				str += Integer.toHexString(csw[i] & 0x00FF) + " ";
			}
		}
		str += "\n";
		mTvInfo.setText(str);
	}*/
}
	
	