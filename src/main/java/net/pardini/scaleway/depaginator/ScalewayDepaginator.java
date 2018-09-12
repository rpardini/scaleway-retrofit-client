package net.pardini.scaleway.depaginator;

import lombok.SneakyThrows;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ScalewayDepaginator<T, R> {

    @SneakyThrows
    public List<R> depaginate(DepaginatorRealItemExtractor<T, R> extractor, DepaginatorScalewayHitter<T> hitter) {
        int currPage = 1;
        boolean areThereMorePages = true;
        ArrayList<R> allItems = new ArrayList<>();
        while (areThereMorePages) {
            Response<T> execute = hitter.hitScalewayForPage(currPage).execute();
            if (!execute.isSuccessful()) {
                throw new RuntimeException("Scaleway query in depaginator not successful: " + execute.message());
            }
            int totalItems = Integer.parseInt(Objects.requireNonNull(execute.headers().get("X-Total-Count")));
            Collection<R> thisPageImages = extractor.extractListObject(execute);
            allItems.addAll(thisPageImages);
            if (!(totalItems > allItems.size())) areThereMorePages = false;
            currPage++;
        }
        return allItems;
    }


}

