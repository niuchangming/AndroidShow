package com.army.gifdemo;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.lang.ref.SoftReference;

public class GifHandler {
	public long gifInfoPro;
	static {
		System.loadLibrary("GIFEngine");
		System.loadLibrary("gifmain");
	}
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				play();
				return;
			}
			if (msg.what == 1) {
				playOnce();
			}
		}
	};
	private ImageView img;
	private SoftReference<ImageView> imgReference;
	private Bitmap bitmap;
	private String path;
	private OnGifPlayFinishListener finishListener;

	public GifHandler(String path, ImageView img) {
		this.path = path;
		resetGif(path);
		imgReference = new SoftReference<>(img);
	}

	public void resetGif(String path) {
		gifInfoPro = openGif(path);
	}

	private void play() {
		try {
			if (bitmap == null) {
				bitmap = Bitmap.createBitmap(getWidth(gifInfoPro), getHeight(gifInfoPro), Bitmap.Config.ARGB_8888);
			}
			long nextTime = renderFrame(gifInfoPro, bitmap);
			if (nextTime == -1) {
				resetGif(path);
				nextTime = renderFrame(gifInfoPro, bitmap);
			}
			if (imgReference != null && imgReference.get() != null) {
				imgReference.get().setImageBitmap(bitmap);
				handler.sendEmptyMessageDelayed(0, nextTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void playOnce() {
		try {
			if (bitmap == null) {
				bitmap = Bitmap.createBitmap(getWidth(gifInfoPro), getHeight(gifInfoPro), Bitmap.Config.ARGB_8888);
			}
			long nextTime = renderFrame(gifInfoPro, bitmap);
            Logger.d("nextTime = " + nextTime);
			if (nextTime == -1) {
				if (finishListener != null) {
					finishListener.onFinish();
				}
				return;
			}
			if (imgReference != null && imgReference.get() != null) {
				imgReference.get().setImageBitmap(bitmap);
				handler.sendEmptyMessageDelayed(1, nextTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startPlayGif() {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(getWidth(gifInfoPro), getHeight(gifInfoPro), Bitmap.Config.ARGB_8888);
		}
		play();
	}

	public void startPlayGifOnce() {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(getWidth(gifInfoPro), getHeight(gifInfoPro), Bitmap.Config.ARGB_8888);
		}
		playOnce();
	}

	public void stop() {
		if (handler != null) {
			handler.removeMessages(0);
			handler.removeMessages(1);
		}
//		if (bitmap != null && !bitmap.isRecycled()) {
//			bitmap.recycle();
//			bitmap = null;
//		}
	}

	public void setFinishListener(OnGifPlayFinishListener finishListener) {
		this.finishListener = finishListener;
	}

	private native long openGif(String path);

	private native int getWidth(long gifInfoPro);

	private native int getHeight(long gifInfoPro);

	private native long renderFrame(long gifInfoPro, Bitmap frameBuffer);

	public interface OnGifPlayFinishListener {
		void onFinish();
	}
}
