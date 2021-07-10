package androidx.leanback.widget;

import android.view.View;
import androidx.leanback.widget.ItemAlignmentFacet;
class ItemAlignment {
    public final Axis horizontal = new Axis(0);
    private int mOrientation = 0;
    public final Axis vertical = new Axis(1);

    ItemAlignment() {
    }

    static final class Axis extends ItemAlignmentFacet.ItemAlignmentDef {
        private int mOrientation;

        Axis(int i) {
            this.mOrientation = i;
        }

        public int getAlignmentPosition(View view) {
            return ItemAlignmentFacetHelper.getAlignmentPosition(view, this, this.mOrientation);
        }
    }

    public final void setOrientation(int i) {
        this.mOrientation = i;
    }
}
