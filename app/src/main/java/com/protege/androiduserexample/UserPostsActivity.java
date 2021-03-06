package com.protege.androiduserexample;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.protege.androiduserexample.adapter.PostsAdapter;
import com.protege.androiduserexample.api.EndpointInterface;
import com.protege.androiduserexample.model.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserPostsActivity extends BaseActivity {

    @Inject
    Retrofit retrofit;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    private PostsAdapter adapter;

    private ArrayList<Post> postLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((UserApplication) getApplication()).getNetworkingComponent().inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide();
            slide.setDuration(300);
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postLists = new ArrayList<>();
        adapter = new PostsAdapter(postLists);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String userId = getIntent().getStringExtra(UserDetailActivity.USER_ID_EXTRA);

        if(userId != null) {
            populateUserPosts(userId);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_user_posts;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.posts);
    }

    private void populateUserPosts(String userId) {
        EndpointInterface endpointInterface =
                retrofit.create(EndpointInterface.class);

        Call<List<Post>> users = endpointInterface.getUserPosts(userId);
        users.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                progressBar.setVisibility(View.GONE);
                postLists.addAll(response.body());
                getSupportActionBar().setSubtitle(String.format(getString(R.string.total),
                        postLists.size()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.d("User Posts", "Error: " + t.getCause());
            }
        });
    }
}
