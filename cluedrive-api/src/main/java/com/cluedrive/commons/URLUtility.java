package com.cluedrive.commons;

/**
 * Url builder class for OneDrive.
 */
public class URLUtility {
    /**
     * Base URI. Every URI built will start with this String.
     */
    public final String URI_BASE;
    /**
     * String builder for faster string manipulation.
     */
    private StringBuilder urlBuilder;
    /**
     * Flag to check if option was added to url.
     */
    private boolean optionAdded;
    /**
     * Flage to check if path was added to url.
     */
    private boolean pathAdded;

    /**
     * Creates URLUtility with given URI_BASE
     * @param UriBase base uri to set.
     */
    public URLUtility(String UriBase) {
        this.URI_BASE = UriBase;
        this.optionAdded = false;
        this.pathAdded = false;
    }

    /**
     * Starts Uri building.
     * @return result ur = URI_BASE
     */
    public URLUtility base() {
        urlBuilder = new StringBuilder(URI_BASE);
        optionAdded = false;
        pathAdded = false;
        return this;
    }

    /**
     * Attaches route to uri.
     * @param route route to be attached.
     * @return result uri = uri(:)/route
     * Colon is added when previously segment was added.
     */
    public URLUtility route(String route) {
        if (optionAdded) {
            return null;
        }
        if (pathAdded) {
            urlBuilder.append(":");
        }
        urlBuilder.append("/").append(route);
        pathAdded = false;
        return this;
    }

    /**
     * Attaches a segment to uri. Paths are handled as segments.
     * @param path path to be attached.
     * @return result uri = uri:path
     */
    public URLUtility segment(CPath path) {
        if (optionAdded) {
            return null;
        }
        urlBuilder.append(":").append(path.toString());
        pathAdded = true;
        return this;
    }

    /**
     * Attaches segment to uri with parent path and resource name.
     * @param parentPath parent path of the segment
     * @param name name of resource
     * @return result uri = uri:parentPath/name
     */
    public URLUtility segment(CPath parentPath, String name) {
        if (optionAdded) {
            return null;
        }
        urlBuilder.append(":").append(parentPath.toString()).append("/").append(name);
        pathAdded = true;
        return this;
    }

    /**
     * Attaches query with parameter and value.
     * @param parameter parameter to set
     * @param value value to set.
     * @return uri result = uri(?/&)parameter=value
     * ? is added when no queries were added before.
     * & is added when queries were added previously.
     */
    public URLUtility query(String parameter, String value) {
        if (optionAdded) {
            urlBuilder.append("&");
        } else {
            urlBuilder.append("?");
            optionAdded = true;
        }
        urlBuilder.append(parameter).append("=").append(value);
        return this;
    }

    /**
     * Attaches filter with given value.
     * @param filters value of the filter.
     * @return uri result = uri(?/&)select=filters
     * ? is added when no queries were added before.
     * & is added when queries were added previously.
     */
    public URLUtility filter(String filters) {
        if (optionAdded) {
            urlBuilder.append("&select=");
        } else {
            urlBuilder.append("?select=");
            optionAdded = true;
        }
        urlBuilder.append(filters);
        return this;
    }

    /**
     * Returns string representation of the built uri.
     * @return uri in string.
     */
    public String toString() {
        return urlBuilder.toString();
    }
}
