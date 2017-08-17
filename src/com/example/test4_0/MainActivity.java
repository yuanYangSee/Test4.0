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
      
    private Button mBtnReset;  
    private Button mBtnGetMaxLnu;  
    private Button mBtnSendCommand;  
    private TextView mTvInfo;  
  
    private UsbManager mUsbManager;  
    private UsbDevice mUsbDevice;  
    private UsbEndpoint mEndpointIn;  
    private UsbEndpoint mEndpointOut;  
    private UsbDeviceConnection mConnection = null;  
      
    private final int mVendorID = 0x2109;  
    private final int mProductID = 0x7638;  
      
    private boolean mDetachedRegistered = false;  

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 mBtnReset = (Button)findViewById(R.id.btn_reset);  
	     mBtnGetMaxLnu = (Button)findViewById(R.id.btn_get_max_lnu);  
	     mBtnSendCommand = (Button)findViewById(R.id.btn_send_command); 
	     
	        mTvInfo = (TextView)findViewById(R.id.ReturnData);  
	          
	        mBtnReset.setOnClickListener(this);  
	        mBtnGetMaxLnu.setOnClickListener(this);  
	        mBtnSendCommand.setOnClickListener(this);  
	          
	        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);  
	}
	
	   private IntentFilter usbDetachedFilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);  
	   //弹出设备时关闭程序
	   private BroadcastReceiver usbDetachedReceiver = new BroadcastReceiver() {  
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);  
	        if(device != null) {  
	            // 确保弹出的设备为指定的  设备ID
	            if(mVendorID == device.getVendorId() && mProductID == device.getProductId()) {  
	                mUsbDevice = null;  
	                finish();  
	            }  
	        }  
	    }  
	   };  

	//找到相应的设备并建立连接
	protected void onResume() {  
	    super.onResume();  
	    // 获取启动Activity的USB设备  
	    Intent intent = getIntent();  
	    String action = intent.getAction();                   
	    mUsbDevice = null;  
	    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {  
	        mUsbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);  
	        if(mVendorID != mUsbDevice.getVendorId() || mProductID != mUsbDevice.getProductId()) {  
	        	Log.d(TAG, "ID核验不正确");
	            mUsbDevice = null;  
	        }  
	    }   
	      
	    if(mUsbDevice == null) {  
	        refreshDevice();  
	       }  
	      
	    if(mUsbDevice == null) {    // 插入设备自动启动应用程序,自动获取获取permission  
	        Log.d(TAG, "Please insert USB flash disk!");            // 手机请使用Toast  
	        Toast.makeText(this, "Please insert USB flash disk!", Toast.LENGTH_SHORT).show();  
	        finish();  
	        return;  
	    }   
	      
	    // 判断是否拥有权限  
	    if(!mUsbManager.hasPermission(mUsbDevice)) {  
	        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);  
	 //       IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);  
	 //     registerReceiver(mPermissionReceiver, filter);  
	        mUsbManager.requestPermission(mUsbDevice, permissionIntent);              
	       } else {  
	        Log.d(TAG, "Correct device!");  
	           Toast.makeText(MainActivity.this, "Correct device!", Toast.LENGTH_SHORT).show();           
	        makeConnection();  
	       }  	                      
	    registerReceiver(usbDetachedReceiver, usbDetachedFilter);   // 注册弹出通知       
	    mDetachedRegistered = true;  
	}  
	
	//获取设备
	private void refreshDevice() {  
	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();  
	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();  
	    while(deviceIterator.hasNext()){  
	        mUsbDevice = deviceIterator.next();  
	        if(mVendorID == mUsbDevice.getVendorId() && mProductID == mUsbDevice.getProductId()) {  
	            break;  
	        } else {  
	            mUsbDevice = null;  
	        }  
	    }     
	}  
	
	//建立连接
	private void makeConnection() {  
	    if(mUsbDevice == null) {  
	        Log.d(TAG, "Please insert USB flash disk!");  
	        Toast.makeText(this, "Please insert USB flash disk!", Toast.LENGTH_SHORT).show();  
	        finish();  
	        return;  
	    }  
	    // U盘接口个数为1  
	    if(mUsbDevice.getInterfaceCount() != 1) {  
	        Log.d(TAG, "Not a USB flash disk!");  
	        Toast.makeText(this, "Not a USB flash disk!", Toast.LENGTH_SHORT).show();  
	        finish();  
	        return;  
	    }  
	      
	    UsbInterface intf = mUsbDevice.getInterface(0);  
	      
	    // U盘接口0可获取的端点数为2  
	    if(intf.getEndpointCount() != 2) {  
	        Log.d(TAG, "Not a USB flash disk!");  
	        Toast.makeText(this, "Not a USB flash disk!", Toast.LENGTH_SHORT).show();  
	        finish();  
	        return;  
	    } else {  
	        mEndpointIn = intf.getEndpoint(0);   // Bulk-In端点  
	        mEndpointOut = intf.getEndpoint(1);  // Bulk_Out端点  
	        Log.d(TAG, "设备非空，获取到In以及OUT端点。");
	    }  
	                      
	    if (mUsbDevice != null) {  
	           UsbDeviceConnection connection = mUsbManager.openDevice(mUsbDevice);  
	           if (connection != null && connection.claimInterface(intf, true)) {  
	            Log.d(TAG, "连接非空，Make connection succeeded!");  
	            Toast.makeText(this, "Make connection succeeded!", Toast.LENGTH_SHORT).show();  
	               mConnection = connection;  
	           } else {  
	            Log.d(TAG, "Make connection failed!");  
	            Toast.makeText(this, "Make connection failed!", Toast.LENGTH_SHORT).show();  
	               mConnection = null;  
	               finish();  
	           }  
	        }  
	}  
	
	
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch(v.getId()) 	{  
        case R.id.btn_reset :  
            reset();  
            break;  
        case R.id.btn_get_max_lnu :  
            getMaxLnu();  
            break;  
        case R.id.btn_send_command :  
       //     sendCommand();  
        	Command_Down(19);
            break;  
        default :  
            break;  
							}  
	}
	
	private void reset() 
	{  
	    synchronized (this) {  
	           if (mConnection != null) {  
	            String str = mTvInfo.getText().toString();  
	              
	            // 复位命令的设置有USB Mass Storage的定义文档给出  
	            int result = mConnection.controlTransfer(0x21, 0xFF, 0x00, 0x00, null, 0, 1000);  
	               if(result < 0) {                      // result<0说明发送失败  
	                Log.d(TAG, "Send reset command failed!");  
	                str += "Send reset command failed!\n";  
	            } else {   
	                Log.d(TAG, "Send reset command succeeded!");  
	                str += "Send reset command succeeded!\n";  
	            }         
	               mTvInfo.setText(str);  
	           }  
	       }  
	}
	
	private void getMaxLnu() {  
	    synchronized (this) {  
	           if (mConnection != null) {  
	            String str = mTvInfo.getText().toString();  
	              
	            // 接收的数据只有1个字节  
	            byte[] message = new byte[1];  
	            // 获取最大LUN命令的设置由USB Mass Storage的定义文档给出  
	               int result = mConnection.controlTransfer(0xA1, 0xFE, 0x00, 0x00, message, 1, 1000);  
	               if(result < 0) {  
	                Log.d(TAG,  "Get max lnu failed!");  
	                str += "Get max lnu failed!\n";  
	            } else {  
	                Log.d(TAG, "Get max lnu succeeded!");                     
	                str += "Get max lnu succeeded!\nMax LNU : ";  
	                for(int i=0; i<message.length; i++) {  
	                    str += Integer.toString(message[i]&0x00FF);  
	                }  
	            }  
	               str += "\n";  
	               mTvInfo.setText(str);  
	           }  
	       }  
	}  
	
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
	      
	/*    byte[] message = new byte[10];      //  需要足够的长度接收数据  
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
	    }  */
	      
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
	
	private void Command_Down(int j) {  
	    String str = mTvInfo.getText().toString();  
	      
	    byte[] cmd = new byte[] {  
	        (byte) 0x55, (byte) 0x53, (byte) 0x42, (byte) 0x43, // 固定值 0~3 
	        (byte) 0x28, (byte) 0xe8, (byte) 0x3e, (byte) 0xfe, // 自定义,与返回的CSW中的值是一样的  4~7
	  //      (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节  
	          (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 传输数据长度为512字节  8~11
	        (byte) 0x00, //  (12th/out)
	        (byte) 0x00, // LNU为0,则设为0  
	        (byte) 0x02, // 命令长度为1  command length
	        (byte) j, (byte) 0x00, (byte) 0x00, (byte) 0x00, // READ FORMAT CAPACITIES,后面的0x00皆被忽略  
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
	      
	/*    byte[] message = new byte[10];      //  需要足够的长度接收数据  
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
	    }  */
	      
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


	

}
