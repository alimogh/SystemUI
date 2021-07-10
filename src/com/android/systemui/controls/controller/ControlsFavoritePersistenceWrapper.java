package com.android.systemui.controls.controller;

import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.systemui.backup.BackupHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import libcore.io.IoUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
/* compiled from: ControlsFavoritePersistenceWrapper.kt */
public final class ControlsFavoritePersistenceWrapper {
    private BackupManager backupManager;
    private final Executor executor;
    private File file;

    public ControlsFavoritePersistenceWrapper(@NotNull File file, @NotNull Executor executor, @Nullable BackupManager backupManager) {
        Intrinsics.checkParameterIsNotNull(file, "file");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.file = file;
        this.executor = executor;
        this.backupManager = backupManager;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ ControlsFavoritePersistenceWrapper(File file, Executor executor, BackupManager backupManager, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(file, executor, (i & 4) != 0 ? null : backupManager);
    }

    public final void changeFileAndBackupManager(@NotNull File file, @Nullable BackupManager backupManager) {
        Intrinsics.checkParameterIsNotNull(file, "fileName");
        this.file = file;
        this.backupManager = backupManager;
    }

    public final boolean getFileExists() {
        return this.file.exists();
    }

    public final void deleteFile() {
        this.file.delete();
    }

    public final void storeFavorites(@NotNull List<StructureInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "structures");
        this.executor.execute(new Runnable(this, list) { // from class: com.android.systemui.controls.controller.ControlsFavoritePersistenceWrapper$storeFavorites$1
            final /* synthetic */ List $structures;
            final /* synthetic */ ControlsFavoritePersistenceWrapper this$0;

            {
                this.this$0 = r1;
                this.$structures = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                boolean z;
                BackupManager backupManager;
                Log.d("ControlsFavoritePersistenceWrapper", "Saving data to file: " + this.this$0.file);
                AtomicFile atomicFile = new AtomicFile(this.this$0.file);
                synchronized (BackupHelper.Companion.getControlsDataLock()) {
                    try {
                        FileOutputStream startWrite = atomicFile.startWrite();
                        z = true;
                        try {
                            XmlSerializer newSerializer = Xml.newSerializer();
                            newSerializer.setOutput(startWrite, "utf-8");
                            newSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                            newSerializer.startDocument(null, Boolean.TRUE);
                            newSerializer.startTag(null, "version");
                            newSerializer.text("1");
                            newSerializer.endTag(null, "version");
                            newSerializer.startTag(null, "structures");
                            for (StructureInfo structureInfo : this.$structures) {
                                newSerializer.startTag(null, "structure");
                                newSerializer.attribute(null, "component", structureInfo.getComponentName().flattenToString());
                                newSerializer.attribute(null, "structure", structureInfo.getStructure().toString());
                                newSerializer.startTag(null, "controls");
                                for (ControlInfo controlInfo : structureInfo.getControls()) {
                                    newSerializer.startTag(null, "control");
                                    newSerializer.attribute(null, "id", controlInfo.getControlId());
                                    newSerializer.attribute(null, "title", controlInfo.getControlTitle().toString());
                                    newSerializer.attribute(null, "subtitle", controlInfo.getControlSubtitle().toString());
                                    newSerializer.attribute(null, "type", String.valueOf(controlInfo.getDeviceType()));
                                    newSerializer.endTag(null, "control");
                                }
                                newSerializer.endTag(null, "controls");
                                newSerializer.endTag(null, "structure");
                            }
                            newSerializer.endTag(null, "structures");
                            newSerializer.endDocument();
                            atomicFile.finishWrite(startWrite);
                        } catch (Throwable th) {
                            IoUtils.closeQuietly(startWrite);
                            throw th;
                        }
                        IoUtils.closeQuietly(startWrite);
                    } catch (IOException e) {
                        Log.e("ControlsFavoritePersistenceWrapper", "Failed to start write file", e);
                        return;
                    }
                }
                if (z && (backupManager = this.this$0.backupManager) != null) {
                    backupManager.dataChanged();
                }
            }
        });
    }

    @NotNull
    public final List<StructureInfo> readFavorites() {
        List<StructureInfo> parseXml;
        if (!this.file.exists()) {
            Log.d("ControlsFavoritePersistenceWrapper", "No favorites, returning empty list");
            return CollectionsKt__CollectionsKt.emptyList();
        }
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(this.file));
            try {
                Log.d("ControlsFavoritePersistenceWrapper", "Reading data from file: " + this.file);
                synchronized (BackupHelper.Companion.getControlsDataLock()) {
                    XmlPullParser newPullParser = Xml.newPullParser();
                    newPullParser.setInput(bufferedInputStream, null);
                    Intrinsics.checkExpressionValueIsNotNull(newPullParser, "parser");
                    parseXml = parseXml(newPullParser);
                }
                IoUtils.closeQuietly(bufferedInputStream);
                return parseXml;
            } catch (XmlPullParserException e) {
                throw new IllegalStateException("Failed parsing favorites file: " + this.file, e);
            } catch (IOException e2) {
                throw new IllegalStateException("Failed parsing favorites file: " + this.file, e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(bufferedInputStream);
                throw th;
            }
        } catch (FileNotFoundException unused) {
            Log.i("ControlsFavoritePersistenceWrapper", "No file found");
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }

    private final List<StructureInfo> parseXml(XmlPullParser xmlPullParser) {
        Ref$IntRef ref$IntRef = new Ref$IntRef();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ComponentName componentName = null;
        String str = null;
        while (true) {
            int next = xmlPullParser.next();
            ref$IntRef.element = next;
            if (next == 1) {
                return arrayList;
            }
            String name = xmlPullParser.getName();
            String str2 = "";
            if (name == null) {
                name = str2;
            }
            if (ref$IntRef.element == 2 && Intrinsics.areEqual(name, "structure")) {
                componentName = ComponentName.unflattenFromString(xmlPullParser.getAttributeValue(null, "component"));
                str = xmlPullParser.getAttributeValue(null, "structure");
                if (str == null) {
                    str = str2;
                }
            } else if (ref$IntRef.element == 2 && Intrinsics.areEqual(name, "control")) {
                String attributeValue = xmlPullParser.getAttributeValue(null, "id");
                String attributeValue2 = xmlPullParser.getAttributeValue(null, "title");
                String attributeValue3 = xmlPullParser.getAttributeValue(null, "subtitle");
                if (attributeValue3 != null) {
                    str2 = attributeValue3;
                }
                String attributeValue4 = xmlPullParser.getAttributeValue(null, "type");
                Integer valueOf = attributeValue4 != null ? Integer.valueOf(Integer.parseInt(attributeValue4)) : null;
                if (!(attributeValue == null || attributeValue2 == null || valueOf == null)) {
                    arrayList2.add(new ControlInfo(attributeValue, attributeValue2, str2, valueOf.intValue()));
                }
            } else if (ref$IntRef.element == 3 && Intrinsics.areEqual(name, "structure")) {
                if (componentName == null) {
                    Intrinsics.throwNpe();
                    throw null;
                } else if (str != null) {
                    arrayList.add(new StructureInfo(componentName, str, CollectionsKt___CollectionsKt.toList(arrayList2)));
                    arrayList2.clear();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }
    }
}
