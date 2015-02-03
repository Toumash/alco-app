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
package pl.codesharks.alcohol.alcoapi.contract;

public class User_Alcohol {
    public static final String API_NAME = "n";
    public static final String API_ID = "id";
    public static final String API_VOLUME = "vol";
    public static final String API_TYPE = "t";
    public static final String API_SUBTYPE = "st";
    public static final String API_PRICE = "cost";
    public static final String API_DEPOSIT = "depo";
    public static final String API_PERCENT = "pct";

    private String _name;
    private double _price;
    private int _volume;
    private double _percent;
    private int _type;
    private int _subtype;
    private int _deposit;
    private int _alcID;

    public User_Alcohol(String name, double price, int volume, double percent, int type, int subtype, int deposit) {
        _name = name;
        _price = price;
        _volume = volume;
        _percent = percent;
        _type = type;
        _subtype = subtype;
        _deposit = deposit;

    }

    public int get_alcID() {
        return _alcID;
    }

    public void set_alcID(int _alcID) {
        this._alcID = _alcID;
    }

    public int get_deposit() {
        return _deposit;
    }

    public void set_deposit(int _deposit) {
        this._deposit = _deposit;
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

    public double get_percent() {
        return _percent;
    }

    public void set_percent(double _percent) {
        this._percent = _percent;
    }

    public int get_volume() {
        return _volume;
    }

    public void set_volume(int _volume) {
        this._volume = _volume;
    }

    public double get_price() {
        return _price;
    }

    public void set_price(double _price) {
        this._price = _price;
    }

    public static final class Type {
        public static final int NISKOPROCENTOWY = 0;
        public static final int SREDNIOPROCENTOWY = 1;
        public static final int WYSOKOPROCENTOWY = 2;
    }

}
