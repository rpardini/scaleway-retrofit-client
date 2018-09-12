package net.pardini.scaleway.depaginator;

import retrofit2.Response;

import java.util.Collection;

public interface DepaginatorRealItemExtractor<T, R> {
    Collection<R> extractListObject(Response<T> execute);
}
