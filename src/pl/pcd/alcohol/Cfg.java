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

@SuppressWarnings("SpellCheckingInspection")
public class Cfg {
    public static final boolean DEBUG = false;
    public static final boolean localTesting = false;
    private static final String localHost = "http://192.168.0.111";
    private static final String externalHost = "http://dev.code-sharks.pl";
    public static final String URL_BASE = localTesting ? localHost : externalHost;
}
