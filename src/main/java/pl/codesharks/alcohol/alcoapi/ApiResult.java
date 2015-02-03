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

package pl.codesharks.alcohol.alcoapi;

public final class ApiResult {
    @Deprecated
    public static final String OK = "ok";
    @Deprecated
    public static final String LOGIN_PASSWORD = "login_password";
    @Deprecated
    public static final String ERROR = "error";
    @Deprecated
    public static final String ACTIVATION = "activation";

    public static final String KEY_RESULT = "result";
    public static final String KEY_ERROR_INFO = "error_info";


    public static final String RESULT_OK = "ok";
    public static final String RESULT_ERROR = "error";


    public static final String INVALID_LOGIN = "inv_login";
    public static final String ACCOUNT_NOT_ACTIVATED = "not_activated";
    public static final String BAD_REQUEST = "bad_request";
    public static final String NO_INPUT = "no_input";
    public static final String NO_METHOD = "no_method";


    public static final String VOID_SESSION = "void_session";
    public static final String NOT_FOUND = "not_found";
}
