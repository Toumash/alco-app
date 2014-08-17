package pl.pcd.alcohol.webapi.contract;

public class User_Alcohol {
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
