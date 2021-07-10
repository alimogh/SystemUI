package androidx.slice.builders.impl;

import androidx.slice.builders.ListBuilder;
public interface ListBuilder {
    void addRow(ListBuilder.RowBuilder rowBuilder);

    void setHeader(ListBuilder.HeaderBuilder headerBuilder);

    void setTtl(long j);
}
