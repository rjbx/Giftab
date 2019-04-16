package com.github.rjbx.givetrack.data;

import android.net.Uri;

import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Spawn;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

final class DataUtilities {

    static <T extends Entry> Uri getContentUri(Class<T> entryType) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case DatabaseContract.CompanyEntry.TABLE_NAME_TARGET: return DatabaseContract.CompanyEntry.CONTENT_URI_TARGET;
            case DatabaseContract.CompanyEntry.TABLE_NAME_RECORD: return DatabaseContract.CompanyEntry.CONTENT_URI_RECORD;
            case DatabaseContract.CompanyEntry.TABLE_NAME_SPAWN: return DatabaseContract.CompanyEntry.CONTENT_URI_SPAWN;
            case DatabaseContract.UserEntry.TABLE_NAME_USER: return DatabaseContract.UserEntry.CONTENT_URI_USER;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    static <T extends Entry> String getTimeTableColumn(Class<T> entryType) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case DatabaseContract.CompanyEntry.TABLE_NAME_TARGET: return DatabaseContract.UserEntry.COLUMN_TARGET_STAMP;
            case DatabaseContract.CompanyEntry.TABLE_NAME_RECORD: return DatabaseContract.UserEntry.COLUMN_RECORD_STAMP;
            case DatabaseContract.CompanyEntry.TABLE_NAME_SPAWN: return "";
            case DatabaseContract.UserEntry.TABLE_NAME_USER: return DatabaseContract.UserEntry.COLUMN_USER_STAMP;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    static <T extends Entry> long getTableTime(Class<T> entryType, User user) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case DatabaseContract.CompanyEntry.TABLE_NAME_SPAWN: return 0;
            case DatabaseContract.CompanyEntry.TABLE_NAME_TARGET: return user.getTargetStamp();
            case DatabaseContract.CompanyEntry.TABLE_NAME_RECORD: return user.getRecordStamp();
            case DatabaseContract.UserEntry.TABLE_NAME_USER: return user.getUserStamp();
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    static <T extends Entry> void setTableTime(Class<T> entryType, User user, long time) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case DatabaseContract.CompanyEntry.TABLE_NAME_SPAWN: break;
            case DatabaseContract.CompanyEntry.TABLE_NAME_TARGET: user.setTargetStamp(time); break;
            case DatabaseContract.CompanyEntry.TABLE_NAME_RECORD: user.setRecordStamp(time); break;
            case DatabaseContract.UserEntry.TABLE_NAME_USER: user.setUserStamp(time); break;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    /**
     * Builds the proper {@link Uri} for requesting movie data.
     * Users must register and reference a unique API key.
     * API keys are available at http://api.charitynavigator.org/
     * @return {@link Uri} for requesting data from the API service.
     */
    static URL getUrl(Uri uri) {
        URL url = null;
        try {
            String urlStr = URLDecoder.decode(uri.toString(), "UTF-8");
            url = new URL(urlStr);
            Timber.v("Fetch URL: %s", url.toString());
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            Timber.e("Unable to convert Uri of %s to URL:", e.getMessage());
        }
        return url;
    }

    /**
     * Returns the result of the HTTP request.
     * @param url address from which to fetch the HTTP response.
     * @return the result of the HTTP request; null if none received.
     */
    static String requestResponseFromUrl(URL url, @Nullable String password) {

        HttpURLConnection urlConnection = null;
        String response = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
//            TODO: Replace network API with Retrofit
//
//            if (password != null) {
//                urlConnection.setRequestProperty("Content-Type", "application/json");
//                urlConnection.setDoOutput(true);
//                urlConnection.setRequestMethod("PUT");
//
//                String credential = "Basic " + new String(Base64.encode(password.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
//                urlConnection.setRequestProperty("Authorization", credential);
//
//                String format = "{\"format\":\"json\",\"pattern\":\"#\"}";
//                OutputStreamWriter oStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
//                oStreamWriter.write(format);
//                oStreamWriter.close();
//            }
            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) response = scanner.next();
            scanner.close();
            Timber.v("Fetched Response: %s", response);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return response;
    }

    /**
     * This method parses JSON String of data API response and returns array of {@link Spawn}.
     */
    static Spawn[] parseSpawns(@NonNull String jsonResponse, String uid, boolean single) {

        Spawn[] spawns = null;
        try {
            if (single) {
                spawns = new Spawn[1];
                spawns[0] = parseSpawn(new JSONObject(jsonResponse),uid);
                Timber.v("Parsed Response: %s", spawns[0].toString());
            } else {
                JSONArray charityArray = new JSONArray(jsonResponse);
                spawns = new Spawn[charityArray.length()];
                for (int i = 0; i < charityArray.length(); i++) {
                    JSONObject charityObject = charityArray.getJSONObject(i);
                    Spawn spawn = parseSpawn(charityObject, uid);
                    spawns[i] = spawn;
                    Timber.v("Parsed Response: %s", spawn.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e);
        }
        return spawns;
    }

    /**
     * This method parses JSONObject of JSONArray and returns {@link Spawn}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    static Spawn parseSpawn(JSONObject charityObject, String uid) throws JSONException {

        JSONObject locationObject = charityObject.getJSONObject(DatasourceContract.KEY_LOCATION);
        String ein = charityObject.getString(DatasourceContract.KEY_EIN);
        String name = charityObject.getString(DatasourceContract.KEY_CHARITY_NAME);
        String street = locationObject.getString(DatasourceContract.KEY_STREET_ADDRESS);
        String detail = locationObject.getString(DatasourceContract.KEY_ADDRESS_DETAIL);
        String city = locationObject.getString(DatasourceContract.KEY_CITY);
        String state = locationObject.getString(DatasourceContract.KEY_STATE);
        String zip = locationObject.getString(DatasourceContract.KEY_POSTAL_CODE);
        String homepageUrl = charityObject.getString(DatasourceContract.KEY_WEBSITE_URL);
        String navigatorUrl = charityObject.getString(DatasourceContract.KEY_CHARITY_NAVIGATOR_URL);

        return new Spawn(uid, ein, System.currentTimeMillis(), name, street, detail, city, state, zip, homepageUrl, navigatorUrl, "", "", "", "0", 0);
    }

    /**
     * Converts a null value returned from API response to default value.
     */
    static String nullToDefaultStr(String str) {
        return (str.equals("null")) ? DatasourceContract.DEFAULT_VALUE_STR : str;
    }

    // TODO Retrieve from Clearbit Enrichment API
    static String urlToSocialHandle(Target target) {
        String socialHandle = DatasourceContract.DEFAULT_VALUE_STR;
        String url = target.getHomepageUrl();
        if (url == null || url.isEmpty()) return socialHandle;
        try {
            List<String> socialHandles = urlToElementContent(url, "a", "twitter.com/", null, null, " ");
//            if (socialHandles.isEmpty()) {
//                String thirdPartyEngineUrl  = String.format(
//                        "https://site.org/profile/%s-%s",
//                        give.getEin().substring(0, 2),
//                        give.getEin().substring(2));
//                socialHandles = urlToElementContent(thirdPartyEngineUrl, "a", "/twitter.com/", null, null, " ");
//            }
//           if (socialHandles.isEmpty())) {
//                String spawnEngineUrl  = String.format(
//                        "https://webcache.googleusercontent.com/spawn?q=cache:%s",
//                        url);
//                socialHandles = urlToElementContent(spawnEngineUrl, "twitter.com/", null, null, null);
//            }
            if (!socialHandles.isEmpty()) {
                for (String handle : socialHandles) Timber.v("Social: @%s", handle);
                socialHandle = socialHandles.get(0);
            }
        } catch (IOException e) { Timber.e(e); }
        return socialHandle;
    }

    static String urlToEmailAddress(Target target) {
        String emailAddress = DatasourceContract.DEFAULT_VALUE_STR;
        String url = target.getHomepageUrl();
        if (url == null || url.isEmpty()) return DatasourceContract.DEFAULT_VALUE_STR;
        try {
            List<String> emailAddresses = urlToElementContent(url, "a", "mailto:", new String[] { "Donate", "Contact" }, null, " ");
//            if (emailAddresses.isEmpty()) {
//                String thirdPartyUrl = "";
//                if (!url.equals(thirdPartyUrl)) emailAddress = urlToElementContent();
//            }
//            if (emailAddress.equals(DEFAULT_VALUE_STR)) {
//                url.replace("http://", "").replace("https://", "").replace("www.", "");
//                String spawnEngineUrl  = String.format(
//                        "https://www.google.com/spawn?q=site%%3A%s+contact+OR+support+\"*%%40%s\"",
//                        url,
//                        url);
//                if (!url.equals(spawnEngineUrl)) emailAddress = (spawnEngineUrl, "mailto:", null, null, " ");
//            }
            if (!emailAddresses.isEmpty()) {
                for (String address : emailAddresses) Timber.v("Email: %s", address);
                emailAddress = emailAddresses.get(0);
            }
        } catch (IOException e) { Timber.e(e); }
        return emailAddress;
    }

    static String urlToPhoneNumber(Target target) {
        String phoneNumber = DatasourceContract.DEFAULT_VALUE_STR;
        String url = target.getNavigatorUrl();
        if (url == null || url.isEmpty()) return phoneNumber;
        try {
            List<String> phoneNumbers = urlToElementContent(url, "div[class=cn-appear]", "tel:", null, 15, "[^0-9]");
            if (!phoneNumbers.isEmpty()) {
                for (String number : phoneNumbers) Timber.v("Phone: %s", number);
                phoneNumber = phoneNumbers.get(0);
            }
        } catch (IOException e) { Timber.e(e);
        } return phoneNumber;
    }

    static List<String> urlToElementContent(@NonNull String url, String cssQuery, String key, @Nullable String[] pageNames, @Nullable Integer endIndex, @Nullable String removeRegex) throws IOException {

        Elements homeInfo = parseElements(url, cssQuery);
        List<String> infoList = new ArrayList<>();
        List<String> visitedLinks = new ArrayList<>();
        if (pageNames != null) {
            for (String pageName : pageNames) {
                infoList.addAll(parseKeysFromPages(url, homeInfo, pageName, visitedLinks, key));
            }
        } infoList.addAll(parseKeys(homeInfo, key, endIndex, removeRegex));
        return infoList;
    }

    // TODO: Establish whether URL is valid before attempting to connect
    static List<String> parseKeysFromPages(String homeUrl, Elements anchors, String pageName, List<String> visitedLinks, String key) throws IOException {
        List<String> emails = new ArrayList<>();
        for (int i = 0; i < anchors.size(); i++) {
            Element anchor = anchors.get(i);
            if (anchor.text().contains(pageName)) {
                if (!anchor.hasAttr("href")) continue;
                String pageLink = anchors.get(i).attr("href");
                if (pageLink.startsWith("/")) pageLink = homeUrl + pageLink.substring(1);
                if (visitedLinks.contains(pageLink)) continue;
                else visitedLinks.add(pageLink);
                Document page = Jsoup.connect(pageLink).get();
                Elements pageAnchors = page.select("a");

                emails.addAll(parseKeys(pageAnchors, key, null, " "));
            }
        }
        return emails;
    }

    static List<String> parseKeys(Elements anchors, String key, @Nullable Integer endIndex, @Nullable String removeRegex) {
        List<String> values = new ArrayList<>();
        for (int j = 0; j < anchors.size(); j++) {
            Element anchor = anchors.get(j);
            if (anchor.hasAttr("href")) {
                if (anchor.attr("href").contains(key))
                    values.add(anchor.attr("href").split(key)[1].trim());
            } else if (anchor.text().contains(key)) {
                String text = anchor.text();
                String value = text.split(key)[1].trim();
                if (endIndex != null) value = value.substring(0, endIndex);
                if (removeRegex != null) value = value.replaceAll(removeRegex, "");
                values.add(value);
            }
        }
        return values;
    }

    static Elements parseElements(String url, String cssQuery) throws IOException {
        Document homepage = Jsoup.connect(url).get();
        return homepage.select(cssQuery);
    }

//    private static String urlToCompanyData(Context context, String homepageUrlStr) {
//        String formattedUrl = parsedResponse[0].getHomepageUrl().split("www.")[1].replace("/", "");
//        String clearbitUrlStr = "https://api.com/v1/enrichment/domain=" + homepageUrlStr
//        URL clearbitURL = getUrl(new Uri.Builder().path(clearbitUrlStr).build());
//        return requestResponseFromUrl(clearbitURL, context.getString(R.string.cb_api_key));
//    }
}
