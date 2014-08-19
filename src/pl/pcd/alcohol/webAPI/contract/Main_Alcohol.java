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

package pl.pcd.alcohol.webapi.contract;

public class Main_Alcohol {
    private long _id;
    private String _name;
    private float _price;
    private int _volume;
    private float _percent;
    private int _type;
    private int _subtype;
    private int _deposit;

    public Main_Alcohol(long id, String name, float price, int type, int subtype, int volume, float percent, int deposit) {
        this._id = id;
        this._name = name;
        this._price = price;
        this._type = type;
        this._subtype = subtype;
        this._volume = volume;
        this._percent = percent;
        this._deposit = deposit;
    }

    public int get_deposit() {
        return _deposit;
    }

    public void set_deposit(int _deposit) {
        this._deposit = _deposit;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public int get_subtype() {
        return _subtype;
    }

    public void set_subtype(int _subtype) {
        this._subtype = _subtype;
    }

    public int get_type() {
        return _type;
    }

    public void set_type(int _type) {
        this._type = _type;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public float get_percent() {
        return _percent;
    }

    public void set_percent(float _percent) {
        this._percent = _percent;
    }

    public int get_volume() {
        return _volume;
    }

    public void set_volume(int _volume) {
        this._volume = _volume;
    }

    public float get_price() {
        return _price;
    }

    public void set_price(float _price) {
        this._price = _price;
    }

    public static final class Type {
        public static final int NISKOPROCENTOWY = 0;
        public static final int SREDNIOPROCENTOWY = 1;
        public static final int WYSOKOPROCENTOWY = 2;
    }

}
