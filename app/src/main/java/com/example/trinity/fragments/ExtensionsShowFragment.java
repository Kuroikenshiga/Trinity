package com.example.trinity.fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.trinity.ExtensionShowContentActivity;
import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.MainActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterMangas;
import com.example.trinity.databinding.FragmentExtensionsShowBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExtensionsShowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExtensionsShowFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String[] languages = new String[]{"pt-br", "en", "es-la"};
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentExtensionsShowBinding binding;
    private boolean isSearchFieldShow = false;
    private Handler mainHandler;
    private ArrayList<Manga> mangasPtBr;
    private ArrayList<Manga> mangasEn;
    private ArrayList<Manga> mangasEsLa;
    private AdapterMangas adapterMangasPtBr;
    private AdapterMangas adapterMangasEn;
    private AdapterMangas adapterMangasEsLa;
    private MangaDexExtension mangaDexExtension;
    private boolean isSearching = false;

    public ExtensionsShowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExtensionsShowFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExtensionsShowFragment newInstance(String param1, String param2) {
        ExtensionsShowFragment fragment = new ExtensionsShowFragment();
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
        TransitionInflater inflater = TransitionInflater.from(getActivity());
        setExitTransition(inflater.inflateTransition(R.transition.fragment_transition));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentExtensionsShowBinding.inflate(getLayoutInflater());

        mangasPtBr = new ArrayList<>();
        mangasEn = new ArrayList<>();
        mangasEsLa = new ArrayList<>();

        adapterMangasPtBr = new AdapterMangas(getActivity(), mangasPtBr);
        adapterMangasPtBr.setHorizontalView(true);
        adapterMangasPtBr.setFromUpdates(true);

        adapterMangasEn = new AdapterMangas(getActivity(), mangasEn);
        adapterMangasEn.setHorizontalView(true);
        adapterMangasEn.setFromUpdates(true);

        adapterMangasEsLa = new AdapterMangas(getActivity(), mangasEsLa);
        adapterMangasEsLa.setHorizontalView(true);
        adapterMangasEsLa.setFromUpdates(true);

        binding.recyclerPtBr.setAdapter(adapterMangasPtBr);
        binding.recyclerPtBr.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        binding.recyclerPtBr.setHasFixedSize(false);

        binding.recyclerEn.setAdapter(adapterMangasEn);
        binding.recyclerEn.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        binding.recyclerEn.setHasFixedSize(false);

        binding.recyclerEsLa.setAdapter(adapterMangasEsLa);
        binding.recyclerEsLa.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        binding.recyclerEsLa.setHasFixedSize(false);

        binding.mangaDexClickablePtBr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ExtensionShowContentActivity.class);

                i.putExtra("Logo", R.drawable.mangadex_logo);
                i.putExtra("Titulo", "MangaDex - pt-br");
                i.putExtra("Language", languages[0]);
                i.putExtra("Extension",Extensions.MANGADEX);

                startActivity(i);


            }
        });
        binding.mangaDexClickableEs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ExtensionShowContentActivity.class);

//
                i.putExtra("Logo", R.drawable.mangadex_logo);
                i.putExtra("Titulo", "MangaDex - Espanhol");
                i.putExtra("Language", languages[2]);
                i.putExtra("Extension",Extensions.MANGADEX);
                startActivity(i);

            }
        });
        binding.mangaDexClickableEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ExtensionShowContentActivity.class);

//
                i.putExtra("Logo", R.drawable.mangadex_logo);
                i.putExtra("Titulo", "MangaDex - en");
                i.putExtra("Language", languages[1]);
                i.putExtra("Extension",Extensions.MANGADEX);
                startActivity(i);


            }
        });
        binding.mangakakalotClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ExtensionShowContentActivity.class);
                i.putExtra("Logo", R.drawable.mangakakalot_svg);
                i.putExtra("Titulo", "Mangakakalot - en");
                i.putExtra("Language", languages[2]);
                i.putExtra("Extension",Extensions.MANGAKAKALOT);
                startActivity(i);


            }
        });


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (((MainActivity) requireActivity()).isInReadFragment) {
                    ((MainActivity) requireActivity()).navigateToUpdates();
                    return;
                }
                if (!((MainActivity) requireActivity()).isInFirstDestination) {
                    ((MainActivity) requireActivity()).navigateToLibrary();
                    return;
                }

                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);

        binding.img5.post(()->{
            Shader shader = new LinearGradient(
                    0,
                    0,
                    binding.img5.getPaint().measureText(binding.img5.getText().toString()),
                    binding.img5.getTextSize(),
                    Color.parseColor("#0cff8a"),
                    Color.parseColor("#00b8ff"),
                    Shader.TileMode.CLAMP);
            binding.img5.getPaint().setShader(shader);
            binding.img5.invalidate();
        });
        binding.mangaLivreClickable.setOnClickListener((v)->{
            Intent i = new Intent(getActivity(), ExtensionShowContentActivity.class);
//            i.putExtra("Logo", R.drawable.mangakakalot_svg);

            i.putExtra("Titulo", "MANGALIVRE - pt br");
            i.putExtra("Language", languages[0]);
            i.putExtra("Extension",Extensions.MANGALIVRE);
            startActivity(i);
        });


        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = false;
            mainActivity.isInReadFragment = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}