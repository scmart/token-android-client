package com.bakkenbaeck.token.view.Fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QrFragment extends Fragment{
    public static final String TAG = "QrFragment";
    private static final String QR_CODE_BITMAP = "QR_CODE_BITMAP";
    private static final String WALLET_ADDRESS = "WALLET_ADDRESS";

    public static QrFragment newInstance() {
        return new QrFragment();
    }

    public interface OnFragmentClosed {
        void onClose();
    }

    public void setOnFragmentClosed(OnFragmentClosed listener) {
        onFragmentClosed = listener;
    }

    private OnFragmentClosed onFragmentClosed;
    private View v;
    private String walletAddress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        v =  inflater.inflate(R.layout.fragment_qr, container, false);
        
        disableTouches();

        //If you show the dialog before the user is initiated, the wallet address is null.
        //Subscribe to the user observable and wait until it's ready
        BaseApplication.get().getUserManager().getObservable().subscribe(new OnNextSubscriber<User>() {
            @Override
            public void onNext(User user) {
                this.unsubscribe();
                walletAddress = BaseApplication.get().getUserManager().getWalletAddress();
                initView(inState);
            }
        });

        return v;
    }

    private void initView(final Bundle inState) {
        ImageView backNav = (ImageView) v.findViewById(R.id.backNav);
        backNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onFragmentClosed != null) {
                    onFragmentClosed.onClose();
                }
            }
        });

        if(inState != null) {
            final String walletAddress = inState.getString(WALLET_ADDRESS);
            final byte[] bytes = inState.getByteArray(QR_CODE_BITMAP);

            insertContent(bytes, walletAddress);
        }else{
            byte[] decodedBitmap = SharedPrefsUtil.getQrCode();

            if(decodedBitmap != null) {
                insertContent(decodedBitmap, this.walletAddress);
            }else {
                generateQrCode(walletAddress);
            }
        }
    }

    private void insertContent(final byte[] bytes, String walletAddress) {
        final TextView qrCodeText = ((TextView) v.findViewById(R.id.qrCodeText));
        qrCodeText.setAlpha(1f);
        qrCodeText.setText(walletAddress);

        ImageView qrCode = ((ImageView) v.findViewById(R.id.qrCodeImage));
        qrCode.setAlpha(1f);

        Bitmap qrCodeBitmap = ImageUtil.decodeByteArray(bytes);

        if(qrCodeBitmap != null) {
            qrCode.setImageBitmap(qrCodeBitmap);
        }
    }

    private void generateQrCode(final String content) {
        generateqrBitmap(content).subscribe(new OnNextObserver<Bitmap>() {
            @Override
            public void onNext(Bitmap bitmap) {
                ImageView qrCode = ((ImageView) v.findViewById(R.id.qrCodeImage));
                qrCode.setImageBitmap(bitmap);
                qrCode.animate().alpha(1f).setDuration(200).start();
                TextView qrCodeText = (TextView) v.findViewById(R.id.qrCodeText);

                SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(bitmap));

                qrCodeText.setText(content);
                qrCodeText.animate().alpha(1f).setDuration(200).start();
            }
        });
    }

    private Observable<Bitmap> generateqrBitmap(final String content) {
        final String prefix = getResources().getString(R.string.prefixEth);
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                QRCodeWriter writer = new QRCodeWriter();
                try {
                    int size = getResources().getDimensionPixelSize(R.dimen.qr_code_size);
                    Map<EncodeHintType, Integer> map = new HashMap<EncodeHintType, Integer>();
                    map.put(EncodeHintType.MARGIN, 0);

                    BitMatrix bitMatrix = writer.encode(prefix + content, BarcodeFormat.QR_CODE, size, size, map);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    subscriber.onNext(bmp);

                } catch (WriterException e) {
                    e.printStackTrace();
                    subscriber.onError(new Throwable("Error writing qrcode"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(v != null) {
            ImageView qrCode = ((ImageView) v.findViewById(R.id.qrCodeImage));
            if(qrCode.getDrawable() != null) {
                Bitmap bmp = ((BitmapDrawable) qrCode.getDrawable()).getBitmap();
                if(bmp != null) {
                    byte[] compressedBitmap = ImageUtil.compressBitmap(bmp);
                    outState.putByteArray(QR_CODE_BITMAP, compressedBitmap);
                }
            }
        }

        outState.putString(WALLET_ADDRESS, walletAddress);

        super.onSaveInstanceState(outState);
    }

    //For some reason it's possible to click on the background activity when a fragment is visible. Disabling touch events completely
    private void disableTouches() {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(v != null) {
            ((ImageView)v.findViewById(R.id.qrCodeImage)).setImageBitmap(null);
        }

        v = null;
        onFragmentClosed = null;
    }
}
