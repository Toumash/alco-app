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

public class Rating {
    public static final String KEY_LOGIN = "a";
    public static final String KEY_DATE = "a";
    public static final String KEY_COMMENT = "a";
    public static final String KEY_RATE = "a";

    public String author;
    public String content;
    public String date;
    public int rating;

    public Rating(String author, String content, String date, int rating) {
        this.author = author;
        this.content = content;
        this.date = date;
        this.rating = rating;
    }
}