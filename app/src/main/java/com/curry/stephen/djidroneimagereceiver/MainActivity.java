package com.curry.stephen.djidroneimagereceiver;

import android.app.Activity;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.GimbalState;
import dji.common.mission.activetrack.ActiveTrackMission;
import dji.common.mission.activetrack.ActiveTrackMissionEvent;
import dji.common.mission.activetrack.ActiveTrackMode;
import dji.common.mission.activetrack.ActiveTrackState;
import dji.common.mission.activetrack.ActiveTrackTargetState;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.mission.activetrack.ActiveTrackMissionOperatorListener;
import dji.sdk.mission.activetrack.ActiveTrackOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends Activity implements SurfaceTextureListener, OnClickListener, View.OnTouchListener
        , ActiveTrackMissionOperatorListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Class that handles encoding and decoding of media, Codec for video live view.
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    private TextView recordingTime;
    private TextView mTextViewDroneAttitudeInfo;
    private TextView mTextViewGimbalAttitudeInfo;

    // private Handler handler;

    // Track target variables;
    private ActiveTrackMission mActiveTrackMission;
    private boolean isDrawingRect = false;
    private float mDownX;
    private float mDowny;
    private String mLocationInfo = "";
    private String mGimbalInfo = "";
    private DJIDataModel mDJIDataModel;
    private String mCacheDir;

    private RelativeLayout mBgLayout;
    private ImageView mSendRectIV;
    private ImageView mTrackingImage;
    private ImageButton mStopBtn;
    private Button mConfirmBtn;
    private Button mRejectBtn;
    private SlidingDrawer mPushInfoSd;
    private ImageButton mSendDataIB;
    private ImageButton mPushDrawerIB;
    private TextView mPushInfoTv;

    private ActiveTrackOperator getActiveTrackOperator() {
        return DJISDKManager.getInstance().getMissionControl().getActiveTrackOperator();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // handler = new Handler();

        initUI();

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = DemoApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }

        getActiveTrackOperator().addListener(this);

        mBgLayout = (RelativeLayout) findViewById(R.id.tracking_bg_layout);
        mSendRectIV = (ImageView) findViewById(R.id.tracking_send_rect_iv);
        mTrackingImage = (ImageView) findViewById(R.id.tracking_rst_rect_iv);
        mStopBtn = (ImageButton) findViewById(R.id.tracking_stop_btn);
        mConfirmBtn = (Button) findViewById(R.id.confirm_btn);
        mRejectBtn = (Button) findViewById(R.id.reject_btn);
        mPushInfoSd = (SlidingDrawer) findViewById(R.id.tracking_drawer_sd);
        mPushDrawerIB = (ImageButton) findViewById(R.id.tracking_drawer_control_ib);
        mSendDataIB = (ImageButton) findViewById(R.id.send_drawer_control_ib);
        mPushInfoTv = (TextView) findViewById(R.id.tv_tracking_info);
        mStopBtn.setOnClickListener(this);
        mBgLayout.setOnTouchListener(this);
        mConfirmBtn.setOnClickListener(this);
        mRejectBtn.setOnClickListener(this);
        mPushDrawerIB.setOnClickListener(this);
        mSendDataIB.setOnClickListener(this);

        getActiveTrackOperator().setRecommendedConfiguration(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                showToast("Set Recommended Config" + (error == null ? "Success" : error.getDescription()));
            }
        });

        mDJIDataModel = new DJIDataModel();

        showDroneLocation();

        mCacheDir = FileHelper.getDiskCacheRootDir(this);
        showToast(mCacheDir);
//        Camera camera = DemoApplication.getCameraInstance();
//        if (camera != null) {
//            camera.setSystemStateCallback(new SystemState.Callback() {
//                @Override
//                public void onUpdate(SystemState cameraSystemState) {
//                    if (null != cameraSystemState) {
//
//                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
//                        int minutes = (recordTime % 3600) / 60;
//                        int seconds = recordTime % 60;
//
//                        final String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
//                        final boolean isVideoRecording = cameraSystemState.isRecording();
//
//                        MainActivity.this.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//
//                                recordingTime.setText(timeString);
//
//                                /*
//                                 * Update recordingTime TextView visibility and mRecordBtn's check state
//                                 */
//                                if (isVideoRecording){
//                                    recordingTime.setVisibility(View.VISIBLE);
//                                }else
//                                {
//                                    recordingTime.setVisibility(View.INVISIBLE);
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//        }
    }

    protected void onProductChange() {
        initPreviewer();
        loginAccount();
    }

    private void loginAccount() {
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view) {
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);
        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        Button btnCapture = (Button) findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(this);

        Button btnSendData = (Button) findViewById(R.id.btn_send_data);
        btnSendData.setOnClickListener(this);

        Button btnLocation = (Button) findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(this);

        mTextViewDroneAttitudeInfo = (TextView) findViewById(R.id.tv_drone_attitude_info);
        mTextViewGimbalAttitudeInfo = (TextView) findViewById(R.id.tv_gimbal_attitude_info);

//        recordingTime = (TextView) findViewById(R.id.timer);
//        ToggleButton recordBtn = (ToggleButton) findViewById(R.id.btn_record);
//        Button shootPhotoModeBtn = (Button) findViewById(R.id.btn_shoot_photo_mode);
//        Button recordVideoModeBtn = (Button) findViewById(R.id.btn_record_video_mode);
//        recordBtn.setOnClickListener(this);
//        shootPhotoModeBtn.setOnClickListener(this);
//        recordVideoModeBtn.setOnClickListener(this);

//        recordingTime.setVisibility(View.INVISIBLE);

//        recordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    startRecord();
//                } else {
//                    stopRecord();
//                }
//            }
//        });
    }

    private void initPreviewer() {

        BaseProduct product = DemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(mReceivedVideoDataCallBack);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = DemoApplication.getCameraInstance();
        if (camera != null) {
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResultToText(final String string) {
        if (mPushInfoTv == null) {
            showToast("Push info tv has not be init...");
        }
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPushInfoTv.setText(string);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_capture:
                startCaptureMouse();
//                captureAction();
                break;
            case R.id.btn_send_data:
                sendTestData();
                break;
            case R.id.btn_location:
                showDroneLocation();
                break;
            case R.id.tracking_stop_btn:
                getActiveTrackOperator().stopTracking(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError error) {
                        showToast(error == null ? "Stop Tracking Success!" : error.getDescription());
                    }
                });
                break;
            case R.id.confirm_btn:
                getActiveTrackOperator().acceptConfirmation(new CommonCallbacks.CompletionCallback() {

                    @Override
                    public void onResult(DJIError error) {
                        showToast(error == null ? "Accept Confirm Success!" : error.getDescription());
                    }
                });
                break;
            case R.id.reject_btn:
                getActiveTrackOperator().rejectConfirmation(new CommonCallbacks.CompletionCallback() {

                    @Override
                    public void onResult(DJIError error) {
                        showToast(error == null ? "Reject Confirm Success!" : error.getDescription());
                    }
                });
                break;
            case R.id.tracking_drawer_control_ib:
                if (mPushInfoSd.isOpened()) {
                    mPushInfoSd.animateClose();
                } else {
                    mPushInfoSd.animateOpen();
                }
                break;
            case R.id.send_drawer_control_ib:
                sendTestData();
                break;
//            case R.id.btn_shoot_photo_mode:{
//                switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
//                break;
//            }
//            case R.id.btn_record_video_mode:{
//                switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
//                break;
//            }
//            default:
//                break;
        }
    }

    private void startCaptureMouse() {
        final int[] location = new int[2];
        mVideoSurface.getLocationOnScreen(location);
//                Log.i(TAG, "location: " + location[0] + ", " + location[1]);
        Toast.makeText(MainActivity.this, "location: " + location[0] + ", " + location[1], LENGTH_LONG).show();

        Rect viewRect = new Rect();

        mVideoSurface.getGlobalVisibleRect(viewRect);

        mVideoSurface.getLocationInWindow(location);

        Log.i(TAG, "rect: " + viewRect);

        mVideoSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float relativeX = event.getX() - location[0];
                float reLativeY = event.getY() - location[1];
                Toast.makeText(MainActivity.this, "onTouch: " + relativeX + ", " + reLativeY, LENGTH_LONG).show();

                return false;
            }
        });
    }

    private void sendTestData() {
        if (DemoApplication.getProductInstance() == null) {
            showToast("Production is null.");
            return;
        }
        FlightController flightController =
                ((Aircraft) DemoApplication.getProductInstance()).getFlightController();
        byte[] sampleData = new byte[2];
        sampleData[0] = 0x12;
        sampleData[1] = 0x34;

        flightController.sendDataToOnboardSDKDevice(sampleData, new CommonCallbacks.CompletionCallback() {

            @Override
            public void onResult(DJIError djiError) {
//                showToast("Rev data back from outer.");
                if (djiError == null) {
                    showToast("Mobile to Onboard is OK.");
                    return;
                }

                showToast(djiError.toString());
                showToast(djiError.getDescription());
            }
        });

        flightController.setOnboardSDKDeviceDataCallback(new FlightController.OnboardSDKDeviceDataCallback() {
            @Override
            public void onReceive(byte[] bytes) {
                float iDistance = ((bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF)) / 10.0f;
//                String distance = Arrays.toString(bytes);// 4a95;
                String sDistance = String.valueOf(iDistance);
                showToast(sDistance);
                mDJIDataModel.setDistance(sDistance);
                mDJIDataModel.setDatetime(DateTimeHelper.getDateTimeNow());

                File file = new File(mCacheDir, "Record-" + DateTimeHelper.getTickNow() + ".txt");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(mDJIDataModel.toString().getBytes());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                showToast(mDJIDataModel.toString());
            }
        });
    }

    private void showDroneLocation() {
        if (isFlightControllerSupported()) {
            FlightController flightController =
                    ((Aircraft) DemoApplication.getProductInstance()).getFlightController();
            final Gimbal gimbal = (DemoApplication.getProductInstance()).getGimbal();
            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(final FlightControllerState
                                             djiFlightControllerCurrentState) {
                    final double droneLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    final double droneLon = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    final float droneAlt = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();

                    mLocationInfo = "Lat: " + droneLat +
                            " Lon: " + droneLon + " GPS Signal Level: " +
                            djiFlightControllerCurrentState.getGPSSignalLevel().value() + "\n\r";

                    setResultToText(mLocationInfo + mGimbalInfo);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewDroneAttitudeInfo.setText("Lat: " + droneLat +
                                    " Lon: " + droneLon + "GPS Signal Level: " +
                                    djiFlightControllerCurrentState.getGPSSignalLevel().value());
                        }
                    });
                    mDJIDataModel.setLat(droneLat);
                    mDJIDataModel.setLon(droneLon);
                    mDJIDataModel.setAlt(droneAlt);
                }
            });
            gimbal.setStateCallback(new GimbalState.Callback() {
                @Override
                public void onUpdate(final GimbalState gimbalState) {
                    final float pitch = gimbalState.getAttitudeInDegrees().getPitch();
                    final float yaw = gimbalState.getAttitudeInDegrees().getYaw();
                    final float roll = gimbalState.getAttitudeInDegrees().getRoll();

                    mGimbalInfo = "Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw;

                    setResultToText(mLocationInfo + mGimbalInfo);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewGimbalAttitudeInfo.setText(
                                    "Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw
                            );
                        }
                    });
                    mDJIDataModel.setPitch(pitch);
                    mDJIDataModel.setYaw(yaw);
                    mDJIDataModel.setRoll(roll);
                }
            });
        }
    }

    private boolean isFlightControllerSupported() {
        return DJISDKManager.getInstance().getProduct() != null &&
                DJISDKManager.getInstance().getProduct() instanceof Aircraft &&
                ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController() != null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawingRect = false;
                mDownX = event.getX();
                mDowny = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (calcManhattanDistance(mDownX, mDowny, event.getX(), event.getY()) < 20 && !isDrawingRect) {
                    return true;
                }
                isDrawingRect = true;
                mSendRectIV.setVisibility(View.VISIBLE);
                int l = (int) (mDownX < event.getX() ? mDownX : event.getX());
                int t = (int) (mDowny < event.getY() ? mDowny : event.getY());
                int r = (int) (mDownX >= event.getX() ? mDownX : event.getX());
                int b = (int) (mDowny >= event.getY() ? mDowny : event.getY());
                mSendRectIV.setX(l);
                mSendRectIV.setY(t);
                mSendRectIV.getLayoutParams().width = r - l;
                mSendRectIV.getLayoutParams().height = b - t;
                mSendRectIV.requestLayout();

                break;

            case MotionEvent.ACTION_UP:
                RectF rectF = getActiveTrackRect(mSendRectIV);
                PointF pointF = new PointF(mDownX / mBgLayout.getWidth(), mDowny / mBgLayout.getHeight());
                RectF pointRectF = new RectF(pointF.x, pointF.y, 0, 0);
                mActiveTrackMission = isDrawingRect ? new ActiveTrackMission(rectF, ActiveTrackMode.TRACE) :
                        new ActiveTrackMission(pointRectF, ActiveTrackMode.TRACE);

                getActiveTrackOperator().startTracking(mActiveTrackMission, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError error) {
                        showToast("Start Tracking: " + (error == null
                                ? "Success"
                                : error.getDescription()));
                    }
                });
                mSendRectIV.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }

        return true;

    }

    @Override
    public void onUpdate(ActiveTrackMissionEvent event) {
        updateActiveTrackRect(mTrackingImage, event);

        ActiveTrackState state = event.getCurrentState();
        if (state == ActiveTrackState.FINDING_TRACKED_TARGET ||
                state == ActiveTrackState.AIRCRAFT_FOLLOWING ||
                state == ActiveTrackState.ONLY_CAMERA_FOLLOWING ||
                state == ActiveTrackState.CANNOT_CONFIRM ||
                state == ActiveTrackState.WAITING_FOR_CONFIRMATION ||
                state == ActiveTrackState.PERFORMING_QUICK_SHOT) {

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mStopBtn.setVisibility(View.VISIBLE);
                    mStopBtn.setClickable(true);
                    mConfirmBtn.setVisibility(View.VISIBLE);
                    mConfirmBtn.setClickable(true);
                    mRejectBtn.setVisibility(View.VISIBLE);
                    mRejectBtn.setClickable(true);
                }
            });
        } else {
            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mStopBtn.setVisibility(View.INVISIBLE);
                    mStopBtn.setClickable(false);
                    mConfirmBtn.setVisibility(View.INVISIBLE);
                    mConfirmBtn.setClickable(false);
                    mRejectBtn.setVisibility(View.INVISIBLE);
                    mRejectBtn.setClickable(false);
                    mTrackingImage.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private RectF getActiveTrackRect(View iv) {
        View parent = (View) iv.getParent();
        return new RectF(
                ((float) iv.getLeft() + iv.getX()) / (float) parent.getWidth(),
                ((float) iv.getTop() + iv.getY()) / (float) parent.getHeight(),
                ((float) iv.getRight() + iv.getX()) / (float) parent.getWidth(),
                ((float) iv.getBottom() + iv.getY()) / (float) parent.getHeight()
        );
    }

    private void updateActiveTrackRect(final ImageView iv, final ActiveTrackMissionEvent event) {
        if (iv == null || event == null) return;
        View parent = (View) iv.getParent();

        if (event.getTrackingState() != null) {
            RectF trackingRect = event.getTrackingState().getTargetRect();
            final int l = (int) ((trackingRect.centerX() - trackingRect.width() / 2) * parent.getWidth());
            final int t = (int) ((trackingRect.centerY() - trackingRect.height() / 2) * parent.getHeight());
            final int r = (int) ((trackingRect.centerX() + trackingRect.width() / 2) * parent.getWidth());
            final int b = (int) ((trackingRect.centerY() + trackingRect.height() / 2) * parent.getHeight());

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ActiveTrackTargetState targetState = event.getTrackingState().getState();

                    if ((targetState == ActiveTrackTargetState.CANNOT_CONFIRM)
                            || (targetState == ActiveTrackTargetState.UNKNOWN)) {
                        iv.setImageResource(R.drawable.visual_track_cannotconfirm);
                        showToast("reason: " + event.getTrackingState().getReason().toString());
                    } else if (targetState == ActiveTrackTargetState.WAITING_FOR_CONFIRMATION) {
                        iv.setImageResource(R.drawable.visual_track_needconfirm);
                    } else if (targetState == ActiveTrackTargetState.TRACKING_WITH_LOW_CONFIDENCE) {
                        iv.setImageResource(R.drawable.visual_track_lowconfidence);
                    } else if (targetState == ActiveTrackTargetState.TRACKING_WITH_HIGH_CONFIDENCE) {
                        iv.setImageResource(R.drawable.visual_track_highconfidence);
                    }
                    iv.setVisibility(View.VISIBLE);
                    iv.setX(l);
                    iv.setY(t);
                    iv.getLayoutParams().width = r - l;
                    iv.getLayoutParams().height = b - t;
                    iv.requestLayout();
                }
            });
        }
    }

    private double calcManhattanDistance(double point1X, double point1Y, double point2X, double point2Y) {
        return Math.abs(point1X - point2X) + Math.abs(point1Y - point2Y);
    }

    /*
    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode){

        Camera camera = DemoApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }
    }

    // Method for taking photo
    private void captureAction(){

        final Camera camera = DemoApplication.getCameraInstance();
        if (camera != null) {

            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            showToast("take photo: success");
                                        } else {
                                            showToast(djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }, 2000);
                    }
                }
            });
        }
    }

    // Method for starting recording
    private void startRecord(){

        final Camera camera = DemoApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError)
                {
                    if (djiError == null) {
                        showToast("Record video: success");
                    }else {
                        showToast(djiError.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord(){

        Camera camera = DemoApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback(){

                @Override
                public void onResult(DJIError djiError)
                {
                    if(djiError == null) {
                        showToast("Stop recording: success");
                    }else {
                        showToast(djiError.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API
        }

    }*/
}

/*
import android.Manifest;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import dji.ui.widget.FPVOverlayWidget;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
//                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
//                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
//                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
//                            Manifest.permission.READ_PHONE_STATE,
//                    }
//                    , 1);
//        }

        setContentView(R.layout.activity_main);

//        (findViewById(R.id.btn_click)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FPVOverlayWidget fpvOverlayWidget = (FPVOverlayWidget)findViewById(R.id.fpv_overlay_widget);
//                final int[] location = new int[2];
//                fpvOverlayWidget.getLocationOnScreen(location);
////                Log.i(TAG, "location: " + location[0] + ", " + location[1]);
//                Toast.makeText(MainActivity.this, "location: " + location[0] + ", " + location[1], LENGTH_LONG).show();
//
//                Rect viewRect = new Rect();
//
//                fpvOverlayWidget.getGlobalVisibleRect(viewRect);
//
//                fpvOverlayWidget.getLocationInWindow(location);
//
//                Log.i(TAG, "rect: " + viewRect );
//
//                fpvOverlayWidget.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        float relativeX = event.getX() - location[0];
//                        float reLativeY = event.getY() - location[1];
//                        Toast.makeText(MainActivity.this, "onTouch: " + relativeX + ", " + reLativeY, LENGTH_LONG).show();
//
//                        return false;
//                    }
//                });
//            }
//        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//
//    }
}
*/