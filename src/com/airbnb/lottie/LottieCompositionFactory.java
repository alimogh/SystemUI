package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.airbnb.lottie.model.LottieCompositionCache;
import com.airbnb.lottie.network.NetworkFetcher;
import com.airbnb.lottie.parser.LottieCompositionMoshiParser;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import okio.Okio;
public class LottieCompositionFactory {
    private static final Map<String, LottieTask<LottieComposition>> taskCache = new HashMap();

    public static LottieTask<LottieComposition> fromUrl(Context context, String str) {
        return fromUrl(context, str, "url_" + str);
    }

    public static LottieTask<LottieComposition> fromUrl(final Context context, final String str, final String str2) {
        return cache(str2, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.1
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return NetworkFetcher.fetchSync(context, str, str2);
            }
        });
    }

    public static LottieTask<LottieComposition> fromAsset(Context context, String str) {
        return fromAsset(context, str, "asset_" + str);
    }

    public static LottieTask<LottieComposition> fromAsset(Context context, final String str, final String str2) {
        final Context applicationContext = context.getApplicationContext();
        return cache(str2, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.2
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromAssetSync(applicationContext, str, str2);
            }
        });
    }

    public static LottieResult<LottieComposition> fromAssetSync(Context context, String str) {
        return fromAssetSync(context, str, "asset_" + str);
    }

    public static LottieResult<LottieComposition> fromAssetSync(Context context, String str, String str2) {
        try {
            if (str.endsWith(".zip")) {
                return fromZipStreamSync(new ZipInputStream(context.getAssets().open(str)), str2);
            }
            return fromJsonInputStreamSync(context.getAssets().open(str), str2);
        } catch (IOException e) {
            return new LottieResult<>(e);
        }
    }

    public static LottieTask<LottieComposition> fromRawRes(Context context, int i) {
        return fromRawRes(context, i, rawResCacheKey(context, i));
    }

    public static LottieTask<LottieComposition> fromRawRes(Context context, final int i, final String str) {
        final WeakReference weakReference = new WeakReference(context);
        final Context applicationContext = context.getApplicationContext();
        return cache(str, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.3
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                Context context2 = (Context) weakReference.get();
                if (context2 == null) {
                    context2 = applicationContext;
                }
                return LottieCompositionFactory.fromRawResSync(context2, i, str);
            }
        });
    }

    public static LottieResult<LottieComposition> fromRawResSync(Context context, int i) {
        return fromRawResSync(context, i, rawResCacheKey(context, i));
    }

    public static LottieResult<LottieComposition> fromRawResSync(Context context, int i, String str) {
        try {
            return fromJsonInputStreamSync(context.getResources().openRawResource(i), str);
        } catch (Resources.NotFoundException e) {
            return new LottieResult<>(e);
        }
    }

    private static String rawResCacheKey(Context context, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("rawRes");
        sb.append(isNightMode(context) ? "_night_" : "_day_");
        sb.append(i);
        return sb.toString();
    }

    private static boolean isNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    public static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream inputStream, String str) {
        return fromJsonInputStreamSync(inputStream, str, true);
    }

    private static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream inputStream, String str, boolean z) {
        try {
            return fromJsonReaderSync(JsonReader.of(Okio.buffer(Okio.source(inputStream))), str);
        } finally {
            if (z) {
                Utils.closeQuietly(inputStream);
            }
        }
    }

    public static LottieResult<LottieComposition> fromJsonReaderSync(JsonReader jsonReader, String str) {
        return fromJsonReaderSyncInternal(jsonReader, str, true);
    }

    private static LottieResult<LottieComposition> fromJsonReaderSyncInternal(JsonReader jsonReader, String str, boolean z) {
        try {
            LottieComposition parse = LottieCompositionMoshiParser.parse(jsonReader);
            if (str != null) {
                LottieCompositionCache.getInstance().put(str, parse);
            }
            LottieResult<LottieComposition> lottieResult = new LottieResult<>(parse);
            if (z) {
                Utils.closeQuietly(jsonReader);
            }
            return lottieResult;
        } catch (Exception e) {
            LottieResult<LottieComposition> lottieResult2 = new LottieResult<>(e);
            if (z) {
                Utils.closeQuietly(jsonReader);
            }
            return lottieResult2;
        } catch (Throwable th) {
            if (z) {
                Utils.closeQuietly(jsonReader);
            }
            throw th;
        }
    }

    public static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream zipInputStream, String str) {
        try {
            return fromZipStreamSyncInternal(zipInputStream, str);
        } finally {
            Utils.closeQuietly(zipInputStream);
        }
    }

    private static LottieResult<LottieComposition> fromZipStreamSyncInternal(ZipInputStream zipInputStream, String str) {
        HashMap hashMap = new HashMap();
        try {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            LottieComposition lottieComposition = null;
            while (nextEntry != null) {
                String name = nextEntry.getName();
                if (name.contains("__MACOSX")) {
                    zipInputStream.closeEntry();
                } else if (nextEntry.getName().contains(".json")) {
                    lottieComposition = fromJsonReaderSyncInternal(JsonReader.of(Okio.buffer(Okio.source(zipInputStream))), null, false).getValue();
                } else {
                    if (!name.contains(".png")) {
                        if (!name.contains(".webp")) {
                            zipInputStream.closeEntry();
                        }
                    }
                    String[] split = name.split("/");
                    hashMap.put(split[split.length - 1], BitmapFactory.decodeStream(zipInputStream));
                }
                nextEntry = zipInputStream.getNextEntry();
            }
            if (lottieComposition == null) {
                return new LottieResult<>(new IllegalArgumentException("Unable to parse composition"));
            }
            for (Map.Entry entry : hashMap.entrySet()) {
                LottieImageAsset findImageAssetForFileName = findImageAssetForFileName(lottieComposition, (String) entry.getKey());
                if (findImageAssetForFileName != null) {
                    findImageAssetForFileName.setBitmap(Utils.resizeBitmapIfNeeded((Bitmap) entry.getValue(), findImageAssetForFileName.getWidth(), findImageAssetForFileName.getHeight()));
                }
            }
            for (Map.Entry<String, LottieImageAsset> entry2 : lottieComposition.getImages().entrySet()) {
                if (entry2.getValue().getBitmap() == null) {
                    return new LottieResult<>(new IllegalStateException("There is no image for " + entry2.getValue().getFileName()));
                }
            }
            if (str != null) {
                LottieCompositionCache.getInstance().put(str, lottieComposition);
            }
            return new LottieResult<>(lottieComposition);
        } catch (IOException e) {
            return new LottieResult<>(e);
        }
    }

    private static LottieImageAsset findImageAssetForFileName(LottieComposition lottieComposition, String str) {
        for (LottieImageAsset lottieImageAsset : lottieComposition.getImages().values()) {
            if (lottieImageAsset.getFileName().equals(str)) {
                return lottieImageAsset;
            }
        }
        return null;
    }

    private static LottieTask<LottieComposition> cache(final String str, Callable<LottieResult<LottieComposition>> callable) {
        final LottieComposition lottieComposition = str == null ? null : LottieCompositionCache.getInstance().get(str);
        if (lottieComposition != null) {
            return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.9
                @Override // java.util.concurrent.Callable
                public LottieResult<LottieComposition> call() {
                    return new LottieResult<>(LottieComposition.this);
                }
            });
        }
        if (str != null && taskCache.containsKey(str)) {
            return taskCache.get(str);
        }
        LottieTask<LottieComposition> lottieTask = new LottieTask<>(callable);
        if (str != null) {
            lottieTask.addListener(new LottieListener<LottieComposition>() { // from class: com.airbnb.lottie.LottieCompositionFactory.10
                public void onResult(LottieComposition lottieComposition2) {
                    LottieCompositionFactory.taskCache.remove(str);
                }
            });
            lottieTask.addFailureListener(new LottieListener<Throwable>() { // from class: com.airbnb.lottie.LottieCompositionFactory.11
                public void onResult(Throwable th) {
                    LottieCompositionFactory.taskCache.remove(str);
                }
            });
            taskCache.put(str, lottieTask);
        }
        return lottieTask;
    }
}
