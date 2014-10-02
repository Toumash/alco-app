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

package pl.pcd.alcohol;

import pl.pcd.alcohol.alcoapi.APICfg;

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

    public static final class API {
        public static final String URL_ABOUT = "http://code-sharks.pl" + "/index.php/o-nas"; //About is on the main domain, not on the sub - "dev"
        public static final String URL_VERSION = APICfg.URL_BASE + "/version.json";
        public static final String URL_JSON = APICfg.URL_BASE + "/api?json=";
        public static final String URL_UPDATE = APICfg.URL_BASE + "/update?file=Alcohol.apk";//"/uploads/Alcohol.apk";
        public static final String URL_MAIN = APICfg.URL_BASE + "/api?db=main";
        public static final String URL_USER = APICfg.URL_BASE + "/api?db=user";

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
            public static final String UPDATE = "update";
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
