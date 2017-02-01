package com.androidcollider.easyfin.common.repository.database;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * @author Ihor Bilous
 */

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {
}