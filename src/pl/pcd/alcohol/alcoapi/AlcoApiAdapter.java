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

package pl.pcd.alcohol.alcoapi;

import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.Utils;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Created by Tomasz on 2014-09-23.
 */
public class AlcoAPIAdapter {

    public static class AlcoJSONConverter implements Converter {

        public static String fromStream(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            //String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                // out.append(newLine);
            }
            return out.toString();
        }

        @Override
        public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
            JSONObject json = null;
            try {
                json = new JSONObject(Utils.substringBetween(fromStream(typedInput.in()), "<json>", "</json>"));
            } catch (IOException ignored) {/*NOP*/ } catch (JSONException jsonE) {
    /*NOP*/
                return null;
            }
            return json;
        }

        @Override
        public TypedOutput toBody(Object o) {
            return null;
        }
    }

    public static class TypedJsonString extends TypedString {
        public TypedJsonString(String body) {
            super(body);
        }

        @Override
        public String mimeType() {
            return "application/json";
        }
    }
}
