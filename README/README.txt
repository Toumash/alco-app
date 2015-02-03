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

Do kompilacji tego repozytorium potrzebne są biblioteki:

 * Actionbar-Sherlock  dostępna w tym folderze, actionbarsherlock.zip - aby ją poprawnie dodać, spójrz na obrazek module_dependency.png)
 * android-async -     osadzona w folderze lib. Jeśli korzystasz z IntelliJ wystarczy ze w widoku projektu klikniesz PPM->Add as library.. i Voila


************************************
*****        ALCO API          *****
************************************

Aby polaczyc sie AlcoAPi, wystarczy ze otrzymasz od admina api_token i stworzysz taką klasę


package pl.pcd.alcohol.alcoapi;

public class ApiToken {
    public static final String TOKEN = "TWOJ_SESSION_TOKEN";
}
