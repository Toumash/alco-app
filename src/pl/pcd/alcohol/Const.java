package pl.pcd.alcohol;

public class Const {
    public static final int REQUEST_CODE_ADD = 1;
    public static final int REQUEST_CODE_UPDATE = 2;
    public static final int REQUEST_LOGIN = 3;

    public static final class ContextMenu {
        public static final int KEY_EDIT = 0;
        public static final int KEY_DELETE = 1;

        public static final class MainDB {
            public static final int KEY_FLAG = 0;
        }
    }

    public static final class EditIntentExtras {
        public static final String KEY_NAME = "name";
        public static final String KEY_PRICE = "price";
        public static final String KEY_TYPE = "type";
        public static final String KEY_SUBTYPE = "subtype";
        public static final String KEY_VOLUME = "volume";
        public static final String KEY_PERCENT = "percent";
        public static final String KEY_DEPOSIT = "deposit";
        public static final String KEY_ID = "id";
    }

    public static final class API {
        public static final String URL_ABOUT = "http://code-sharks.pl" + "/index.php/o-nas"; //About is on the main domain, not on the sub - "dev"
        public static final String URL_VERSION = Cfg.URL_BASE + "/version.json";
        public static final String URL_JSON = Cfg.URL_BASE + "/api?json=";
        public static final String URL_UPDATE = Cfg.URL_BASE + "/update?file=Alcohol.apk";//"/uploads/Alcohol.apk";
        public static final String URL_MAIN = Cfg.URL_BASE + "/api?db=main";
        public static final String URL_USER = Cfg.URL_BASE + "/api?db=user";

        public static final class LoginResult {
            public static final String OK = "ok";
            public static final String LOGIN_PASSWORD = "login_password";
            public static final String ERROR = "error";
            public static final String ACTIVATION = "activation";
        }

        public static final class Actions {
            public static final String LOGIN = "login";
            public static final String UPLOAD = "upload";
            public static final String REGISTER = "register";
            public static final String FLAG = "flag";
            public static final String ISSUE = "issue";
            public static final String PROFILE_DOWNLOAD = "profileDownload";
            public static final String FETCH_RATINGS = "fetchRatings";
            public static final String RATE = "rate";
            public static final String REGISTER_INSTALLATION = "reg_installation";
        }

    }

    public static final class Prefs {

        public static final class WEB_API {
            public static final String FILE = "webapi";
            public static final String PASSWORD = "p";
            public static final String LOGIN = "l";
            public static final String WEIGHT = "w";
            public static final String SEX = "sex";
            public static final String LOGGED = "lg";
            public static final String EMAIL = "email";
            public static final String RATINGS_COUNT = "ratings";
        }

        public static final class Main {
            public static final String FILE = "main";
            public static final String LAST_UPDATE_CHECK = "last_update";
            public static final String AUTO_UPDATE = "autoUpdate";
            public static final String COMMENTS_AUTO_REFRESH = "cm_auto_refresh";
            public static final String FIRST_RUN = "first_run";
            public static final String INSTALLATION_ID = "id";
            public static final String INSTALLATION_REGISTERED = "inst_reg";
        }

    }
}
