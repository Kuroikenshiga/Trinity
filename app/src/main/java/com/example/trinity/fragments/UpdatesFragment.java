package com.example.trinity.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.trinity.MainActivity;

import com.example.trinity.R;
import com.example.trinity.adapters.AdapterUpdates;
import com.example.trinity.databinding.FragmentUpdatesBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.UpdateWork;
import com.example.trinity.services.broadcasts.ActionsPending;
import com.example.trinity.services.broadcasts.CancelCurrentWorkReceiver;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.ChapterUpdated;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.viewModel.MangaDataViewModel;
import com.example.trinity.viewModel.MangasFromDataBaseViewModel;
import com.example.trinity.viewModel.UpdatesViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpdatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdatesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentUpdatesBinding binding;

    private MainActivity myActivity;
    private RecyclerView chapContainer;
    private ArrayList<ChapterUpdated> updateds;
    private AdapterUpdates adapter;
    private Model model;
    private MangaDexExtension mangaDexExtension;
    private UpdatesViewModel updatesViewModel;
    private OneTimeWorkRequest workRequest;
    private WorkManager workManager;
    private boolean isUpdatingLibray = false;
    private MangaDataViewModel mangaDataViewModel;
    private NotificationManager notificationManager;
    private final static String CHANNEL_NOTIFICATION_ID = "CHANNEL1";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private long lastUpadate;
    private boolean wasReloaded = false;

    public UpdatesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdatesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdatesFragment newInstance(String param1, String param2) {
        UpdatesFragment fragment = new UpdatesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        myActivity = (MainActivity) getActivity();
        TransitionInflater inflater = TransitionInflater.from(getActivity());
        setExitTransition(inflater.inflateTransition(R.transition.fragment_transition));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.updatesViewModel = new ViewModelProvider(requireActivity()).get(UpdatesViewModel.class);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.checkPermission();
        sharedPreferences = getActivity().getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        this.lastUpadate = sharedPreferences.getLong(ConfigClass.ConfigUpdates.LAST_UPDATE, 0);

        mangaDataViewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        binding = FragmentUpdatesBinding.inflate(inflater, container, false);
        if (lastUpadate != 0) {
            binding.lastUpdate.setText("Última atualização feita " + this.returnLastUpdateTime(lastUpadate, Instant.now().getEpochSecond()));
            binding.lastUpdate.setVisibility(View.VISIBLE);
        }
        workManager = WorkManager.getInstance(getActivity());

//        mangaDexExtension = new MangaDexExtension(imageQuality);
        chapContainer = binding.updatesConteiner;
        this.updateds = updatesViewModel.getChapterUpdatedLiveData().getValue();
        this.adapter = new AdapterUpdates(getActivity(), updateds, this);
        adapter.setMangaDataViewModel(mangaDataViewModel);
        adapter.setFragment(this);
        this.chapContainer.setAdapter(adapter);


        this.updatesViewModel.getItem().observe(getViewLifecycleOwner(), new Observer<ChapterUpdated>() {
            @Override
            public void onChanged(ChapterUpdated chapterUpdated) {
//                System.out.println("changed");
//                adapter.notifyItemInserted(updatesViewModel.getChapterUpdatedLiveData().getValue().size());
                adapter.notifyDataSetChanged();
            }
        });

        this.chapContainer.setLayoutManager(new LinearLayoutManager(myActivity));
        loadUpdates();
        realodUpdates();

        return binding.getRoot();
    }

    private void loadUpdates() {
        if (updatesViewModel.getChapterUpdatedLiveData().getValue().isEmpty() || wasReloaded) {
            wasReloaded = false;
            if (!isUpdatingLibray) {
                new Thread() {
                    @Override
                    public void run() {
                        model = Model.getInstance(myActivity);

                        ArrayList<ChapterManga> chapterUpdatedArrayList = model.loadUpdates();
                        if (getActivity() == null) return;
                        MangasFromDataBaseViewModel mangasFromDataBaseViewModel = new ViewModelProvider(requireActivity()).get(MangasFromDataBaseViewModel.class);


                        updatesViewModel.getChapterUpdatedLiveData().getValue().clear();

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyItemRangeRemoved(0, updatesViewModel.getChapterUpdatedLiveData().getValue().size());
                                }
                            });
                        }
                        int offSet = 0;
                        ArrayList<Manga> mangas = model.selectAllMangas(false,10,offSet);
                        while(!mangas.isEmpty()){
                            for (ChapterManga chapU : chapterUpdatedArrayList) {
                                for (Manga m : mangas) {
                                    if (m.uuid == chapU.mangaUUID) {
                                        ChapterUpdated capU = new ChapterUpdated(m, chapU);
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updatesViewModel.addChapterInLiveData(capU);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                            offSet += 10;
                            mangas = model.selectAllMangas(false,10,offSet);
                        }
                        isUpdatingLibray = false;
                    }
                }.start();
            }
        }
    }

    private void realodUpdates() {
        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                lastUpadate = Instant.now().getEpochSecond();
                editor.putLong(ConfigClass.ConfigUpdates.LAST_UPDATE, lastUpadate);
                editor.apply();
                if (!isUpdatingLibray) {

                    isUpdatingLibray = true;

                    workRequest = new OneTimeWorkRequest.Builder(UpdateWork.class).addTag(ActionsPending.UPDATE_WORK_TAG).build();

                    workManager.enqueueUniqueWork(UpdateWork.WORK_NAME, ExistingWorkPolicy.KEEP, workRequest);
//                    workManager.enqueue(workRequest);
                    workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(UpdatesFragment.this.getActivity(), new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {

                            if (workInfo == null) return;

                            if (workInfo.getState().isFinished()) {
                                isUpdatingLibray = false;
                                binding.swipe.setRefreshing(false);
                                isUpdatingLibray = false;
                                workManager.cancelAllWork();
                                wasReloaded = true;
                                loadUpdates();
                                binding.lastUpdate.setText("Última atualização feita " + returnLastUpdateTime(lastUpadate, Instant.now().getEpochSecond()));

                            }

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastUpadate != 0) {
            binding.lastUpdate.setText("Última atualização feita " + this.returnLastUpdateTime(lastUpadate, Instant.now().getEpochSecond()));
            binding.lastUpdate.setVisibility(View.VISIBLE);
        }
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = false;
            mainActivity.isInReadFragment = false;
        }
    }

    public void turnOffRefresh() {
        binding.swipe.setRefreshing(false);
    }

    public void navigateToRead() {
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_updates_to_readerMangaFragment2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        System.out.println("Destroy");


    }

    public String returnLastUpdateTime(long lastupdate, long currentUpdate) {
        long time = currentUpdate - lastupdate;

        long year = time / 31556926;
        time = year > 0 ? time % 31556926 : time;
        long month = time / 2592000;
        time = month > 0 ? time % 2592000 : time;
        long day = time / 86000;
        time = day > 0 ? time % 86000 : time;
        long hour = time / 3600;
        time = hour > 0 ? time % hour : time;
        long min = time / 60;
        time = min > 0 ? time % 60 : time;
        long sec = time;

        if (year > 0) {
            return "Há " + year + (year > 1 ? " anos atrás" : " ano atrás");
        }
        if (month > 0) {
            return "Há " + month + (month > 1 ? " meses atrás" : " mês atrás");
        }
        if (day > 0) {
            return "Há " + day + (day > 1 ? " dias atrás" : " dia atrás");
        }
        if (hour > 0) {
            return "Há " + hour + (hour > 1 ? " horas atrás" : " hora atrás");
        }
        if (min > 0) {
            return "Há " + min + (min > 1 ? " minutos atrás" : " minuto atrás");
        }
        return "Há " + sec + (sec > 1 ? " segundos atrás" : " segundo atrás");
    }

    @Override
    public void onPause() {
        super.onPause();
        this.turnOffRefresh();
//        System.out.println("Pausado");
    }
}