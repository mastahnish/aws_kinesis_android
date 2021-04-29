package com.amazonaws.kinesisvideo.demoapp.fragment;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.demoapp.KinesisVideoDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.activity.SimpleNavActivity;
import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.demoapp.rest.RetrofitClient;
import com.amazonaws.kinesisvideo.demoapp.rest.model.FaceDetectionResult;
import com.amazonaws.mobileconnectors.kinesisvideo.client.KinesisVideoAndroidClientFactory;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSource;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSourceConfiguration;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamingFragment extends Fragment implements TextureView.SurfaceTextureListener {
    public static final String KEY_MEDIA_SOURCE_CONFIGURATION = "mediaSourceConfiguration";
    public static final String KEY_STREAM_NAME = "streamName";

    private static final String TAG = StreamingFragment.class.getSimpleName();

    private TextView challengeLabel;
    private Button mStartStreamingButton;
    private KinesisVideoClient mKinesisVideoClient;
    private String mStreamName;
    private AndroidCameraMediaSourceConfiguration mConfiguration;
    private AndroidCameraMediaSource mCameraMediaSource;
    private FaceDetectionResult faceDetectionResult = null;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int endpointID = 1;

    private SimpleNavActivity navActivity;

    public static StreamingFragment newInstance(SimpleNavActivity navActivity) {
        StreamingFragment s = new StreamingFragment();
        s.navActivity = navActivity;
        return s;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        getArguments().setClassLoader(AndroidCameraMediaSourceConfiguration.class.getClassLoader());
        mStreamName = getArguments().getString(KEY_STREAM_NAME);
        mConfiguration = getArguments().getParcelable(KEY_MEDIA_SOURCE_CONFIGURATION);

        final View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        TextureView textureView = (TextureView) view.findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(this);

        initChallengeViews(view);

        return view;
    }


    private void initChallengeViews(View view) {
        challengeLabel = (TextView) view.findViewById(R.id.challengeInfo);
        final int random = 0 + new Random().nextInt((4 - 0) + 1);

        Button endpointOneButton = view.findViewById(R.id.endpoint_1);
        endpointOneButton.setOnClickListener(v -> {
            endpointID = 1;
        });

        Button endpointTwoButton = view.findViewById(R.id.endpoint_2);
        endpointTwoButton.setOnClickListener(v -> {
            endpointID = 2;
        });

        Button endpointThreeButton = view.findViewById(R.id.endpoint_3);
        endpointThreeButton.setOnClickListener(v -> {
            endpointID = 3;
        });


        challengeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "random: " + random);
                switch (0 + new Random().nextInt((4 - 0) + 1)) {
                    case 0:
                        setShowMeYourFaceLabel();
                    case 2:
                        setPleaseSmileLabel();
                    case 3:
                        setCongratsLabel();
                }
            }
        });
    }

    private void setShowMeYourFaceLabel() {
        challengeLabel.setText(getString(R.string.show_me_your_face).toUpperCase());

        TextPaint paint = challengeLabel.getPaint();
        float width = paint.measureText(getString(R.string.show_me_your_face));

        Shader textShader = new LinearGradient(0, 0, width, challengeLabel.getTextSize(),
                new int[]{
                        Color.parseColor("#F97C3C"),
                        Color.parseColor("#FDB54E"),
                        Color.parseColor("#64B678"),
                        Color.parseColor("#478AEA"),
                        Color.parseColor("#8446CC"),
                }, null, Shader.TileMode.CLAMP);
        challengeLabel.getPaint().setShader(textShader);
    }

    private void setPleaseSmileLabel() {
        challengeLabel.setText(getString(R.string.please_smile).toUpperCase());

        TextPaint paint = challengeLabel.getPaint();
        float width = paint.measureText(getString(R.string.please_smile));

        Shader textShader = new LinearGradient(0, 0, width, challengeLabel.getTextSize(),
                new int[]{
                        Color.parseColor("#64B678"),
                        Color.parseColor("#478AEA"),
                        Color.parseColor("#8446CC"),
                        Color.parseColor("#FDB54E"),
                        Color.parseColor("#F97C3C"),
                }, null, Shader.TileMode.CLAMP);
        challengeLabel.getPaint().setShader(textShader);
    }

    private void setCongratsLabel() {
        challengeLabel.setText(getString(R.string.congrats_label).toUpperCase());

        TextPaint paint = challengeLabel.getPaint();
        float width = paint.measureText(getString(R.string.congrats_label));

        Shader textShader = new LinearGradient(0, 0, width, challengeLabel.getTextSize(),
                new int[]{
                        Color.parseColor("#4ae54a"),
                        Color.parseColor("#30cb00"),
                        Color.parseColor("#0f9200"),
                        Color.parseColor("#30cb00"),
                        Color.parseColor("#4ae54a"),
                }, null, Shader.TileMode.CLAMP);
        challengeLabel.getPaint().setShader(textShader);
    }

    private void createClientAndStartStreaming(final SurfaceTexture previewTexture) {

        try {
            mKinesisVideoClient = KinesisVideoAndroidClientFactory.createKinesisVideoClient(
                    getActivity(),
                    KinesisVideoDemoApp.KINESIS_VIDEO_REGION,
                    KinesisVideoDemoApp.getCredentialsProvider());

            mCameraMediaSource = (AndroidCameraMediaSource) mKinesisVideoClient
                    .createMediaSource(mStreamName, mConfiguration);

            mCameraMediaSource.setPreviewSurfaces(new Surface(previewTexture));

            resumeStreaming();
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "unable to start streaming");
            throw new RuntimeException("unable to start streaming", e);
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mStartStreamingButton = (Button) view.findViewById(R.id.start_streaming);
        mStartStreamingButton.setOnClickListener(stopStreamingWhenClicked());
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeStreaming();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseStreaming();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pauseStreaming();
    }

    private View.OnClickListener stopStreamingWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                pauseStreaming();
                navActivity.startConfigFragment();
            }
        };
    }

    private void resumeStreaming() {
        try {
            if (mCameraMediaSource == null) {
                return;
            }

            mCameraMediaSource.start();
            Toast.makeText(getActivity(), "resumed streaming", Toast.LENGTH_SHORT).show();
            mStartStreamingButton.setText(getActivity().getText(R.string.stop_streaming));
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "unable to resume streaming", e);
            Toast.makeText(getActivity(), "failed to resume streaming", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseStreaming() {
        try {
            if (mCameraMediaSource == null) {
                return;
            }

            mCameraMediaSource.stop();
            Toast.makeText(getActivity(), "stopped streaming", Toast.LENGTH_SHORT).show();
            mStartStreamingButton.setText(getActivity().getText(R.string.start_streaming));
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "unable to pause streaming", e);
            Toast.makeText(getActivity(), "failed to pause streaming", Toast.LENGTH_SHORT).show();
        }
    }

    ////
    // TextureView.SurfaceTextureListener methods
    ////

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("TAG", "onSurfaceTextureAvailable");
        surfaceTexture.setDefaultBufferSize(1280, 720);
        createClientAndStartStreaming(surfaceTexture);
        startSendingFaceDetectionRequests();
    }

    private void startSendingFaceDetectionRequests() {

        compositeDisposable.add(Observable.interval(500, 500,
                TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                            Log.d("TAG", "sending face detection request");
                            sendFaceDetectionRequest();
                        }
                ));
    }

    private void stopSendingFaceDetectionRequests() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    private void sendFaceDetectionRequest() {
        Call<FaceDetectionResult> call = RetrofitClient.getInstance().getMyApi().getFaceResult(endpointID);
        call.enqueue(new Callback<FaceDetectionResult>() {

            @Override
            public void onResponse(Call<FaceDetectionResult> call, Response<FaceDetectionResult> response) {
                Log.d("TAG", "Face detection response retrieved: " + response.body().toString());
                faceDetectionResult = response.body();
                showLabelBasedOnFaceDetectionResult();
            }

            @Override
            public void onFailure(Call<FaceDetectionResult> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void showLabelBasedOnFaceDetectionResult(){
        Log.d("TAG", "onSurfaceTextureUpdated");
        if (!faceDetectionResult.isFace() && !faceDetectionResult.isSmile()) {
            setShowMeYourFaceLabel();
        }

        if (faceDetectionResult.isFace() && !faceDetectionResult.isSmile()) {
            setPleaseSmileLabel();
        }

        if (faceDetectionResult.isFace() && faceDetectionResult.isSmile()) {
            setCongratsLabel();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("TAG", "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d("TAG", "onSurfaceTextureDestroyed");
        stopSendingFaceDetectionRequests();
        try {
            if (mCameraMediaSource != null)
                mCameraMediaSource.stop();
            if (mKinesisVideoClient != null)
                mKinesisVideoClient.stopAllMediaSources();
            KinesisVideoAndroidClientFactory.freeKinesisVideoClient();
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "failed to release kinesis video client", e);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
