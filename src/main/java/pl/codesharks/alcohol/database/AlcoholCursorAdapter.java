/******************************************************************************
 * Copyright 2014 CodeSharks                                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package pl.codesharks.alcohol.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.codesharks.alcohol.R;


public abstract class AlcoholCursorAdapter extends CursorAdapter {
    protected ViewHolder mHolder;
    protected Resources mRes = this.mContext.getResources();
    protected String[] mTypes;
    protected String[] mSubtypesLow;
    protected String[] mSubtypesMedium;
    protected String[] mSubtypesHigh;
    protected CurIndex mIndexes;

    public AlcoholCursorAdapter(Context context, @NotNull Cursor c, int flags) {
        super(context, c, flags);
        mTypes = this.mRes.getStringArray(R.array.typy);
        mSubtypesLow = this.mRes.getStringArray(R.array.niskoprocentowe);
        mSubtypesMedium = this.mRes.getStringArray(R.array.srednioprocentowe);
        mSubtypesHigh = this.mRes.getStringArray(R.array.wysokoprocentowe);
        mIndexes = new CurIndex(c);
    }

    @Override
    abstract public void bindView(View view, Context context, Cursor cursor);

    /**
     * This method needs implementing to enable more code reuse see example implementation:
     * mHolder = (ViewHolder) view.getTag();
     * mHolder.name.setText(cursor.getString(mIndexes.name));
     * mHolder.percent.setText(cursor.getString(mIndexes.percent));
     * mHolder.volume.setText(cursor.getString(mIndexes.volume));
     * mHolder.price.setText(cursor.getString(mIndexes.price));
     * int type = cursor.getInt(mIndexes.type);
     * int subtype = cursor.getInt(mIndexes.subtype);
     * mHolder.type.setText(mTypes[type]);
     * String subtypeString = "";
     * switch (type) {
     * case 0:
     * subtypeString = mSubtypesLow[subtype];
     * break;
     * case 1:
     * subtypeString = mSubtypesMedium[subtype];
     * break;
     * case 2:
     * subtypeString = mSubtypesHigh[subtype];
     * break;
     * }
     * mHolder.subtype.setText(subtypeString);
     * mHolder.id.setText(cursor.getString(mIndexes.id));
     */
    @Override
    public View newView(@NotNull Context context, Cursor cursor, ViewGroup parent) {
        View v;
        LayoutInflater vi =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.alcohol_item, null);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) v.findViewById(R.id.alcoholitem_Name);
        holder.percent = (TextView) v.findViewById(R.id.alcoholitem_Percentage);
        holder.volume = (TextView) v.findViewById(R.id.alcoholitem_Volume);
        holder.price = (TextView) v.findViewById(R.id.alcoholitem_Price);
        holder.type = (TextView) v.findViewById(R.id.alcoholitem_type);
        holder.subtype = (TextView) v.findViewById(R.id.alcoholitem_subtype);
        holder.id = (TextView) v.findViewById(R.id.alcoholitem_Number);
        v.setTag(holder);
        return v;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }

    protected static class ViewHolder {
        public TextView name;
        public TextView percent;
        public TextView volume;
        public TextView price;
        public TextView type;
        public TextView subtype;
        public TextView id;
    }

    protected class CurIndex {
        public int name, price, volume, type, subtype, id, percent;

        public CurIndex(@NotNull Cursor c) {
            this.name = c.getColumnIndexOrThrow(MainDB.KEY_NAME);
            this.price = c.getColumnIndexOrThrow(pl.codesharks.alcohol.database.MainDB.KEY_PRICE);
            this.volume = c.getColumnIndexOrThrow(MainDB.KEY_VOLUME);
            this.type = c.getColumnIndexOrThrow(MainDB.KEY_TYPE);
            this.subtype = c.getColumnIndexOrThrow(MainDB.KEY_SUBTYPE);
            this.id = c.getColumnIndexOrThrow(MainDB.KEY_ID_ALC);
            this.percent = c.getColumnIndexOrThrow(MainDB.KEY_PERCENT);
        }

        public int getName() {
            return name;
        }

        public void setName(int name) {
            this.name = name;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getSubtype() {
            return subtype;
        }

        public void setSubtype(int subtype) {
            this.subtype = subtype;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }
    }
}
