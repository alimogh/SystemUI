package androidx.leanback.widget;
public final class ItemAlignmentFacet {
    private ItemAlignmentDef[] mAlignmentDefs = {new ItemAlignmentDef()};

    public static class ItemAlignmentDef {
        private boolean mAlignToBaseline;
        int mFocusViewId = -1;
        int mOffset = 0;
        float mOffsetPercent = 50.0f;
        boolean mOffsetWithPadding = false;
        int mViewId = -1;

        public final int getItemAlignmentFocusViewId() {
            int i = this.mFocusViewId;
            return i != -1 ? i : this.mViewId;
        }

        public boolean isAlignedToTextViewBaseLine() {
            return this.mAlignToBaseline;
        }
    }

    public ItemAlignmentDef[] getAlignmentDefs() {
        return this.mAlignmentDefs;
    }
}
