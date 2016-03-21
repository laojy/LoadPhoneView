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
		   // 使用最大可用内存值的1/8作为缓存的大小。   
		   int cacheSize = maxMemory / 4;   
		    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {   
		        @Override   
		        protected int sizeOf(String key, Bitmap bitmap) {   
		            // 重写此方法来衡量每张图片的大小，默认返回图片数量。   
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
	           para.height = mScreenHeight/8;//一屏幕显示8行  
	           para.width = (mScreenWidth-20)/2;//一屏显示两列
	           img.setLayoutParams(para);
			Bitmap bmp=decodeSampledBitmapFromResource(PicPathList.get(pos),100,100);
			img.setImageBitmap(bmp);*/
			return view;
		}
		
		

			
		public  Bitmap decodeSampledBitmapFromResource(String path,   //生成图片缩略图
				       int reqWidth, int reqHeight) {   
				   // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小   
				   final BitmapFactory.Options options = new BitmapFactory.Options();   
				   options.inJustDecodeBounds = true;   
				   //BitmapFactory.decodeResource(res, resId, options);
				   BitmapFactory.decodeFile(path, options);
				   // 调用上面定义的方法计算inSampleSize值   
				   options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);   
				   // 使用获取到的inSampleSize值再次解析图片   
				    options.inJustDecodeBounds = false;   
				   // return BitmapFactory.decodeResource(res, resId, options);  
				    return BitmapFactory.decodeFile(path, options);
				}

		
		public  int calculateInSampleSize(BitmapFactory.Options options,   //计算图片缩略图大小
				       int reqWidth, int reqHeight) {   
				   // 源图片的高度和宽度   
				   final int height = options.outHeight;   
				   final int width = options.outWidth;   
				   int inSampleSize = 1;   
				   if (height > reqHeight || width > reqWidth) {   
				       // 计算出实际宽高和目标宽高的比率   
				       final int heightRatio = Math.round((float) height / (float) reqHeight);   
				        final int widthRatio = Math.round((float) width / (float) reqWidth);   
				        // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高   
				        // 一定都会大于等于目标的宽和高。   
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
			// 在后台加载图片。   
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
