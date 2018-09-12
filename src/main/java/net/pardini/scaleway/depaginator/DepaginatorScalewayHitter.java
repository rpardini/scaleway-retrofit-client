package net.pardini.scaleway.depaginator;

import retrofit2.Call;

public interface DepaginatorScalewayHitter<T> {
    Call<T> hitScalewayForPage(int currPage);
}
