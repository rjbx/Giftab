package com.github.rjbx.givetrack.data;

/**
 * Defines the request and response path and parameters for the data API service.
 */
final class DatasourceContract {

    static final String BASE_URL = "https://api.data.charitynavigator.org/v2";
    static final String API_PATH_ORGANIZATIONS = "Organizations";
    static final String DEFAULT_VALUE_STR = "";
    static final int DEFAULT_VALUE_INT = -1;

    // Query parameters
    static final String PARAM_APP_ID = "app_id";
    static final String PARAM_APP_KEY = "app_key";
    static final String PARAM_EIN = "ein";
    static final String PARAM_PAGE_NUM = "pageNum";
    static final String PARAM_PAGE_SIZE = "pageSize";
    static final String PARAM_SEARCH = "search";
    static final String PARAM_SEARCH_TYPE ="searchType";
    static final String PARAM_RATED = "rated";
    static final String PARAM_CATEGORY_ID = "categoryID";
    static final String PARAM_CAUSE_ID = "causeID";
    static final String PARAM_FILTER = "fundraisingOrgs";
    static final String PARAM_STATE = "state";
    static final String PARAM_CITY = "city";
    static final String PARAM_ZIP = "zip";
    static final String PARAM_MIN_RATING = "minRating";
    static final String PARAM_MAX_RATING = "maxRating";
    static final String PARAM_SIZE_RANGE = "sizeRange";
    static final String PARAM_DONOR_PRIVACY = "donorPrivacy";
    static final String PARAM_SCOPE_OF_WORK = "scopeOfWork";
    static final String PARAM_CFC_CHARITIES = "cfcCharities";
    static final String PARAM_NO_GOV_SUPPORT = "noGovSupport";
    static final String PARAM_SORT = "sort";

    // Response keys
    static final String KEY_EIN = "ein";
    static final String KEY_CHARITY_NAME = "charityName";
    static final String KEY_LOCATION = "mailingAddress";
    static final String KEY_STREET_ADDRESS = "streetAddress1";
    static final String KEY_ADDRESS_DETAIL = "streetAddress2";
    static final String KEY_CITY = "city";
    static final String KEY_STATE = "stateOrProvince";
    static final String KEY_POSTAL_CODE = "postalCode";
    static final String KEY_WEBSITE_URL = "websiteURL";
    static final String KEY_PHONE_NUMBER = "phoneNumber";
    static final String KEY_EMAIL_ADDRESS = "generalEmail";
    static final String KEY_CURRENT_RATING = "currentRating";
    static final String KEY_RATING = "rating";
    static final String KEY_ADVISORIES = "advisories";
    static final String KEY_SEVERITY = "severity";
    static final String KEY_CHARITY_NAVIGATOR_URL = "charityNavigatorURL";
    static final String KEY_ERROR_MESSAGE = "errorMessage";

    static final String[] OPTIONAL_PARAMS = {
            PARAM_PAGE_NUM,
            PARAM_PAGE_SIZE,
            PARAM_SEARCH,
            PARAM_SEARCH_TYPE,
            PARAM_RATED,
            PARAM_CATEGORY_ID,
            PARAM_CAUSE_ID,
            PARAM_FILTER,
            PARAM_STATE,
            PARAM_CITY,
            PARAM_ZIP,
            PARAM_MIN_RATING,
            PARAM_MAX_RATING,
            PARAM_SIZE_RANGE,
            PARAM_DONOR_PRIVACY,
            PARAM_SCOPE_OF_WORK,
            PARAM_CFC_CHARITIES,
            PARAM_NO_GOV_SUPPORT,
            PARAM_SORT
    };
}
