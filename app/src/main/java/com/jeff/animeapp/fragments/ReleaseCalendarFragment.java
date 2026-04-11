package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.CalendarAdapter;
import com.jeff.animeapp.api.AniListClient;
import com.jeff.animeapp.models.ReleaseItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReleaseCalendarFragment extends Fragment {

    private RecyclerView recyclerCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_release_calendar, container, false);

        recyclerCalendar = v.findViewById(R.id.recyclerCalendar);
        recyclerCalendar.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Back button listener
        v.findViewById(R.id.btnBack).setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        fetchUpcomingAnime();

        return v;
    }

    private void fetchUpcomingAnime() {
        // GraphQL query
        String query = "query { Page(page:1, perPage:10) { media(season:SPRING, seasonYear:2026, type:ANIME) { title { romaji } startDate { year month day } coverImage { large } format genres } } }";

        JsonObject body = new JsonObject();
        body.addProperty("query", query);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReleaseItem> releaseList = new ArrayList<>();

                    JsonObject data = response.body().getAsJsonObject("data");
                    JsonObject page = data.getAsJsonObject("Page");
                    JsonArray mediaArray = page.getAsJsonArray("media");

                    for (int i = 0; i < mediaArray.size(); i++) {
                        JsonObject anime = mediaArray.get(i).getAsJsonObject();

                        String title = anime.getAsJsonObject("title").get("romaji").getAsString();
                        JsonObject startDate = anime.getAsJsonObject("startDate");
                        String date = startDate.get("year").getAsInt() + "-" +
                                startDate.get("month").getAsInt() + "-" +
                                startDate.get("day").getAsInt();

                        String format = anime.get("format").getAsString();
                        JsonArray genresArray = anime.getAsJsonArray("genres");
                        List<String> genres = new ArrayList<>();
                        for (int j = 0; j < genresArray.size(); j++) {
                            genres.add(genresArray.get(j).getAsString());
                        }

                        String imageUrl = anime.getAsJsonObject("coverImage").get("large").getAsString();

                        releaseList.add(new ReleaseItem(
                                title,
                                date,
                                format,
                                String.join(", ", genres),
                                imageUrl
                        ));
                    }

                    recyclerCalendar.setAdapter(new CalendarAdapter(releaseList));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
