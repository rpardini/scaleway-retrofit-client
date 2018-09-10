package net.pardini.scaleway;

import retrofit2.Call;

public interface DepaginatorScalewayHitter<T> {
    public Call<T> hitScalewayForPage(int currPage);
}
