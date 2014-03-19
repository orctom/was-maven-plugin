package com.orctom.mojo.was.model;

import org.codehaus.plexus.util.StringUtils;

/**
 * Build generic info
 * Created by CH on 3/19/14.
 */
public class Meta {

    private String locale;
    private String cdap;
    private String brand;

    public Meta() {
    }

    public String getLocale() {
        return locale;
    }

    public Meta setLocale(String locale) {
        if (StringUtils.isNotEmpty(locale)) {
            this.locale = locale;
        }
        return this;
    }

    public String getCdap() {
        return cdap;
    }

    public Meta setCdap(String cdap) {
        if (StringUtils.isNotEmpty(cdap)) {
            this.cdap = cdap;
        }
        return this;
    }

    public String getBrand() {
        return brand;
    }

    public Meta setBrand(String brand) {
        if (StringUtils.isNotEmpty(brand)) {
            this.brand = brand;
        }
        return this;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "locale='" + locale + '\'' +
                ", cdap='" + cdap + '\'' +
                ", brand='" + brand + '\'' +
                '}';
    }
}
