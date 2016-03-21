package com.laojy.gestureview;

import java.util.ArrayList;
import java.util.List;

import com.polites.android.GestureImageView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity {
	GridView gridview;
	List<String> PicPathList=new ArrayList<String>();
	GridViewAdapter adapter;
	//int mScreenHeight,mScreenWidth;
	LruCache<String, Bitmap> mMemoryCache;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getAllPic();
		/* Display display = getWindowManager().getDefaultDisplay();  
		       mScreenHeight= display.getHeight();  
		        mScreenWidth = display.getWidth();*/ 
		
		
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);   
		   // ʹ���������ڴ�ֵ��1/8��Ϊ����Ĵ�С��   
		   int cacheSize = maxMemory / 4;   
		    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {   
		        @Override   
		        protected int sizeOf(String key, Bitmap bitmap) {   
		            // ��д�˷���������ÿ��ͼƬ�Ĵ�С��Ĭ�Ϸ���ͼƬ������   
		            return bitmap.getByteCount() / 1024;   
		        }   
		    };   


		gridview=(GridView) findViewById(R.id.gridView);
		adapter=new GridViewAdapter(this,PicPathList);
		gridview.setAdapter(adapter);
	}
	
	public void getAllPic(){
		ContentResolver contentResolver = getContentResolver();  
        String[] projection = new String[] { MediaStore.Images.Media.DATA };  
       Cursor cursor = contentResolver.query(  
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,  
                 null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);  
       cursor.moveToFirst();
       int fileNum = cursor.getCount();  
       for (int counter = 0; counter < fileNum; counter++) {  
            PicPathList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            cursor.moveToNext();  
        }
       cursor.close(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class GridViewAdapter extends BaseAdapter{
		private Context context;
	    private List<String> PicPathList;
	    
	    
		public GridViewAdapter(Context context, List<String> picPathList) {
			super();
			this.context = context;
			PicPathList = picPathList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return PicPathList.size();
		}

		@Override
		public Object getItem(int pos) {
			// TODO Auto-generated method stub
			return PicPathList.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			// TODO Auto-generated method stub
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View view =convertView;
			if(view==null){
				view=LayoutInflater.from(context).inflate(R.layout.view_item, null);
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		   

			ImageView img=(ImageView) view.findViewById(R.id.imageview);
			if(PicPathList.get(pos)!=null){
				Bitmap bmp=getBitmapFromMemCache(PicPathList.get(pos));
				if(bmp!=null){
					img.setImageBitmap(bmp);
				}else{
					BitmapWorkerTask bitmaptask=new BitmapWorkerTask(PicPathList.get(pos),img);
					bitmaptask.execute(1000);
				}
			}
			 /*LayoutParams para = img.getLayoutParams();  
	           para.height = mScreenHeight/8;//һ��Ļ��ʾ8��  
	           para.width = (mScreenWidth-20)/2;//һ����ʾ����
	           img.setLayoutParams(para);
			Bitmap bmp=decodeSampledBitmapFromResource(PicPathList.get(pos),100,100);
			img.setImageBitmap(bmp);*/
			return view;
		}
		
		

			
		public  Bitmap decodeSampledBitmapFromResource(String path,   //����ͼƬ����ͼ
				       int reqWidth, int reqHeight) {   
				   // ��һ�ν�����inJustDecodeBounds����Ϊtrue������ȡͼƬ��С   
				   final BitmapFactory.Options options = new BitmapFactory.Options();   
				   options.inJustDecodeBounds = true;   
				   //BitmapFactory.decodeResource(res, resId, options);
				   BitmapFactory.decodeFile(path, options);
				   // �������涨��ķ�������inSampleSizeֵ   
				   options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);   
				   // ʹ�û�ȡ����inSampleSizeֵ�ٴν���ͼƬ   
				    options.inJustDecodeBounds = false;   
				   // return BitmapFactory.decodeResource(res, resId, options);  
				    return BitmapFactory.decodeFile(path, options);
				}

		
		public  int calculateInSampleSize(BitmapFactory.Options options,   //����ͼƬ����ͼ��С
				       int reqWidth, int reqHeight) {   
				   // ԴͼƬ�ĸ߶ȺͿ��   
				   final int height = options.outHeight;   
				   final int width = options.outWidth;   
				   int inSampleSize = 1;   
				   if (height > reqHeight || width > reqWidth) {   
				       // �����ʵ�ʿ�ߺ�Ŀ���ߵı���   
				       final int heightRatio = Math.round((float) height / (float) reqHeight);   
				        final int widthRatio = Math.round((float) width / (float) reqWidth);   
				        // ѡ���͸�����С�ı�����ΪinSampleSize��ֵ���������Ա�֤����ͼƬ�Ŀ�͸�   
				        // һ��������ڵ���Ŀ��Ŀ�͸ߡ�   
				        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;   
				    }   
				    return inSampleSize;   
				} 
		
		
		class BitmapWorkerTask extends AsyncTask<Integer, Bitmap, Bitmap> {  
			String str;
			ImageView imageview;
			   public BitmapWorkerTask(String str, ImageView imageview) {
				super();
				this.str = str;
				this.imageview = imageview;
			}
			// �ں�̨����ͼƬ��   
			   @Override   
			   protected Bitmap doInBackground(Integer... params) {   
			       final Bitmap bitmap = decodeSampledBitmapFromResource(   
			               str, 100, 100);   
			       addBitmapToMemoryCache(str, bitmap);   
			       publishProgress(bitmap);
			       return bitmap;   
			   }
			@Override
			protected void onProgressUpdate(Bitmap... values) {
				// TODO Auto-generated method stub
				imageview.setImageBitmap(values[0]);;
			} 
			
			} 
		
		public void addBitmapToMemoryCache(String key, Bitmap bitmap) {   
		    if (getBitmapFromMemCache(key) == null&&bitmap!=null) {   
		        mMemoryCache.put(key, bitmap);   
		    }   
		}   
		 
		public Bitmap getBitmapFromMemCache(String key) {   
		    return mMemoryCache.get(key);   
		} 
	}
}
