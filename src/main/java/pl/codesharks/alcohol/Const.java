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

package pl.codesharks.alcohol;

import pl.codesharks.alcohol.alcoapi.APICfg;

public class Const {
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

    }

}
