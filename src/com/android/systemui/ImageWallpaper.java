package com.android.systemui;

import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.ImageWallpaper;
import com.android.systemui.glwallpaper.EglHelper;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.glwallpaper.ImageWallpaperRenderer;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public class ImageWallpaper extends WallpaperService {
    private static final String TAG = ImageWallpaper.class.getSimpleName();
    private HandlerThread mWorker;

    @Override // android.service.wallpaper.WallpaperService, android.app.Service
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread(TAG);
        this.mWorker = handlerThread;
        handlerThread.start();
    }

    @Override // android.service.wallpaper.WallpaperService
    public WallpaperService.Engine onCreateEngine() {
        return new GLEngine();
    }

    @Override // android.service.wallpaper.WallpaperService, android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.mWorker.quitSafely();
        this.mWorker = null;
    }

    public class GLEngine extends WallpaperService.Engine {
        @VisibleForTesting
        static final int MIN_SURFACE_HEIGHT = 64;
        @VisibleForTesting
        static final int MIN_SURFACE_WIDTH = 64;
        private EglHelper mEglHelper;
        private final Runnable mFinishRenderingTask = new Runnable() { // from class: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$4IwqG_0jMNtMT6yCqqj-KAFKSvE
            @Override // java.lang.Runnable
            public final void run() {
                ImageWallpaper.GLEngine.m7lambda$4IwqG_0jMNtMT6yCqqjKAFKSvE(ImageWallpaper.GLEngine.this);
            }
        };
        private GLWallpaperRenderer mRenderer;

        public boolean shouldZoomOutWallpaper() {
            return true;
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        GLEngine() {
            super(r1);
            ImageWallpaper.this = r1;
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        @VisibleForTesting
        GLEngine(Handler handler) {
            super(r2, $$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA.INSTANCE, handler);
            ImageWallpaper.this = r2;
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onCreate(SurfaceHolder surfaceHolder) {
            this.mEglHelper = getEglHelperInstance();
            this.mRenderer = getRendererInstance();
            setFixedSizeAllowed(true);
            setOffsetNotificationsEnabled(false);
            updateSurfaceSize();
        }

        public EglHelper getEglHelperInstance() {
            return new EglHelper();
        }

        public ImageWallpaperRenderer getRendererInstance() {
            return new ImageWallpaperRenderer(getDisplayContext());
        }

        private void updateSurfaceSize() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Size reportSurfaceSize = this.mRenderer.reportSurfaceSize();
            surfaceHolder.setFixedSize(Math.max(64, reportSurfaceSize.getWidth()), Math.max(64, reportSurfaceSize.getHeight()));
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onDestroy() {
            ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() { // from class: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$BobZgI4REJvgDbbrYxKQK2v8vCg
                @Override // java.lang.Runnable
                public final void run() {
                    ImageWallpaper.GLEngine.this.lambda$onDestroy$0$ImageWallpaper$GLEngine();
                }
            });
        }

        /* access modifiers changed from: public */
        /* renamed from: lambda$onDestroy$0 */
        private /* synthetic */ void lambda$onDestroy$0$ImageWallpaper$GLEngine() {
            this.mRenderer.finish();
            this.mRenderer = null;
            this.mEglHelper.finish();
            this.mEglHelper = null;
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(surfaceHolder) { // from class: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$iLRwANP3nahTog6rPMk87G_B1tQ
                    public final /* synthetic */ SurfaceHolder f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceCreated$1$ImageWallpaper$GLEngine(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: public */
        /* renamed from: lambda$onSurfaceCreated$1 */
        private /* synthetic */ void lambda$onSurfaceCreated$1$ImageWallpaper$GLEngine(SurfaceHolder surfaceHolder) {
            this.mEglHelper.init(surfaceHolder, needSupportWideColorGamut());
            this.mRenderer.onSurfaceCreated();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(i2, i3) { // from class: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$NZAB5XGFpHaOG6-R1l-DvpakCYM
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceChanged$2$ImageWallpaper$GLEngine(this.f$1, this.f$2);
                    }
                });
            }
        }

        /* access modifiers changed from: public */
        /* renamed from: lambda$onSurfaceChanged$2 */
        private /* synthetic */ void lambda$onSurfaceChanged$2$ImageWallpaper$GLEngine(int i, int i2) {
            this.mRenderer.onSurfaceChanged(i, i2);
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() { // from class: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$dwIVxRzjo8QTPBtgktS9kM6mj4o
                    @Override // java.lang.Runnable
                    public final void run() {
                        ImageWallpaper.GLEngine.lambda$dwIVxRzjo8QTPBtgktS9kM6mj4o(ImageWallpaper.GLEngine.this);
                    }
                });
            }
        }

        /* access modifiers changed from: public */
        private void drawFrame() {
            preRender();
            requestRender();
            postRender();
        }

        public void preRender() {
            Trace.beginSection("ImageWallpaper#preRender");
            preRenderInternal();
            Trace.endSection();
        }

        private void preRenderInternal() {
            boolean z;
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            cancelFinishRenderingTask();
            if (!this.mEglHelper.hasEglContext()) {
                this.mEglHelper.destroyEglSurface();
                if (!this.mEglHelper.createEglContext()) {
                    Log.w(ImageWallpaper.TAG, "recreate egl context failed!");
                } else {
                    z = true;
                    if (this.mEglHelper.hasEglContext() && !this.mEglHelper.hasEglSurface() && !this.mEglHelper.createEglSurface(getSurfaceHolder(), needSupportWideColorGamut())) {
                        Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
                    }
                    if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && z) {
                        this.mRenderer.onSurfaceCreated();
                        this.mRenderer.onSurfaceChanged(surfaceFrame.width(), surfaceFrame.height());
                        return;
                    }
                    return;
                }
            }
            z = false;
            Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
            if (this.mEglHelper.hasEglContext()) {
            }
        }

        public void requestRender() {
            Trace.beginSection("ImageWallpaper#requestRender");
            requestRenderInternal();
            Trace.endSection();
        }

        private void requestRenderInternal() {
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && surfaceFrame.width() > 0 && surfaceFrame.height() > 0) {
                this.mRenderer.onDrawFrame();
                if (!this.mEglHelper.swapBuffer()) {
                    Log.e(ImageWallpaper.TAG, "drawFrame failed!");
                    return;
                }
                return;
            }
            String str = ImageWallpaper.TAG;
            Log.e(str, "requestRender: not ready, has context=" + this.mEglHelper.hasEglContext() + ", has surface=" + this.mEglHelper.hasEglSurface() + ", frame=" + surfaceFrame);
        }

        public void postRender() {
            Trace.beginSection("ImageWallpaper#postRender");
            scheduleFinishRendering();
            Trace.endSection();
        }

        private void cancelFinishRenderingTask() {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().removeCallbacks(this.mFinishRenderingTask);
            }
        }

        private void scheduleFinishRendering() {
            if (ImageWallpaper.this.mWorker != null) {
                cancelFinishRenderingTask();
                ImageWallpaper.this.mWorker.getThreadHandler().postDelayed(this.mFinishRenderingTask, 1000);
            }
        }

        /* access modifiers changed from: public */
        private void finishRendering() {
            Trace.beginSection("ImageWallpaper#finishRendering");
            EglHelper eglHelper = this.mEglHelper;
            if (eglHelper != null) {
                eglHelper.destroyEglSurface();
                this.mEglHelper.destroyEglContext();
            }
            Trace.endSection();
        }

        private boolean needSupportWideColorGamut() {
            return this.mRenderer.isWcgContent();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            super.dump(str, fileDescriptor, printWriter, strArr);
            printWriter.print(str);
            printWriter.print("Engine=");
            printWriter.println(this);
            printWriter.print(str);
            printWriter.print("valid surface=");
            Object obj = "null";
            printWriter.println((getSurfaceHolder() == null || getSurfaceHolder().getSurface() == null) ? obj : Boolean.valueOf(getSurfaceHolder().getSurface().isValid()));
            printWriter.print(str);
            printWriter.print("surface frame=");
            if (getSurfaceHolder() != null) {
                obj = getSurfaceHolder().getSurfaceFrame();
            }
            printWriter.println(obj);
            this.mEglHelper.dump(str, fileDescriptor, printWriter, strArr);
            this.mRenderer.dump(str, fileDescriptor, printWriter, strArr);
        }
    }
}
