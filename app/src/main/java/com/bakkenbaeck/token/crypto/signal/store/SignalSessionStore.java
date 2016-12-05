package com.bakkenbaeck.token.crypto.signal.store;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bakkenbaeck.token.crypto.util.ByteUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionState;
import org.whispersystems.libsignal.state.SessionStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import static org.whispersystems.libsignal.state.StorageProtos.SessionStructure;

public class SignalSessionStore implements SessionStore {

    private static final String SESSIONS_DIRECTORY_V2 = "sessions-v2";
    private static final Object FILE_LOCK             = new Object();

    private static final int SINGLE_STATE_VERSION   = 1;
    private static final int ARCHIVE_STATES_VERSION = 2;
    private static final int PLAINTEXT_VERSION      = 3;
    private static final int CURRENT_VERSION        = 3;
    private static final int DEFAULT_DEVICE_ID = 1;

    @NonNull  private final Context context;

    public SignalSessionStore() {
        this.context = BaseApplication.get();
    }

    @Override
    public SessionRecord loadSession(@NonNull SignalProtocolAddress address) {
        synchronized (FILE_LOCK) {
            try {
                FileInputStream in            = new FileInputStream(getSessionFile(address));
                int             versionMarker = readInteger(in);

                if (versionMarker > CURRENT_VERSION) {
                    throw new AssertionError("Unknown version: " + versionMarker);
                }

                byte[] serialized = readBlob(in);
                in.close();

                if (versionMarker < PLAINTEXT_VERSION) {
                    throw new AssertionError("Session didn't get migrated: (" + versionMarker + "," + address + ")");
                }

                if (versionMarker == SINGLE_STATE_VERSION) {
                    SessionStructure sessionStructure = SessionStructure.parseFrom(serialized);
                    SessionState     sessionState     = new SessionState(sessionStructure);
                    return new SessionRecord(sessionState);
                } else if (versionMarker >= ARCHIVE_STATES_VERSION) {
                    return new SessionRecord(serialized);
                } else {
                    throw new AssertionError("Unknown version: " + versionMarker);
                }
            } catch (final IOException e) {
                LogUtil.w(getClass(), "No existing session information found.");
                return new SessionRecord();
            }
        }
    }

    @Override
    public void storeSession(@NonNull SignalProtocolAddress address, @NonNull SessionRecord record) {
        synchronized (FILE_LOCK) {
            try {
                RandomAccessFile sessionFile  = new RandomAccessFile(getSessionFile(address), "rw");
                FileChannel      out          = sessionFile.getChannel();

                out.position(0);
                writeInteger(CURRENT_VERSION, out);
                writeBlob(record.serialize(), out);
                out.truncate(out.position());

                sessionFile.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    @Override
    public boolean containsSession(SignalProtocolAddress address) {
        return getSessionFile(address).exists() &&
                loadSession(address).getSessionState().hasSenderChain();
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        getSessionFile(address).delete();
    }

    @Override
    public void deleteAllSessions(String name) {
        List<Integer> devices = getSubDeviceSessions(name);

        deleteSession(new SignalProtocolAddress(name, DEFAULT_DEVICE_ID));

        for (int device : devices) {
            deleteSession(new SignalProtocolAddress(name, device));
        }
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        // ToDo
        return new LinkedList<>();
    }

    public void migrateSessions() {
        synchronized (FILE_LOCK) {
            File directory = getSessionDirectory();

            for (File session : directory.listFiles()) {
                if (session.isFile()) {
                    SignalProtocolAddress address = getAddressName(session);

                    if (address != null) {
                        SessionRecord sessionRecord = loadSession(address);
                        storeSession(address, sessionRecord);
                    }
                }
            }
        }
    }

    private File getSessionFile(SignalProtocolAddress address) {
        return new File(getSessionDirectory(), getSessionName(address));
    }

    private File getSessionDirectory() {
        File directory = new File(context.getFilesDir(), SESSIONS_DIRECTORY_V2);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                LogUtil.w(getClass(), "Session directory creation failed!");
            }
        }

        return directory;
    }

    private String getSessionName(SignalProtocolAddress axolotlAddress) {
        // ToDo
        return "sessionName";
    }

    private @Nullable SignalProtocolAddress getAddressName(File sessionFile) {
        // ToDo
        return new SignalProtocolAddress("name", 1);
    }

    private byte[] readBlob(FileInputStream in) throws IOException {
        int length       = readInteger(in);
        byte[] blobBytes = new byte[length];

        in.read(blobBytes, 0, blobBytes.length);
        return blobBytes;
    }

    private void writeBlob(byte[] blobBytes, FileChannel out) throws IOException {
        writeInteger(blobBytes.length, out);
        out.write(ByteBuffer.wrap(blobBytes));
    }

    private int readInteger(FileInputStream in) throws IOException {
        byte[] integer = new byte[4];
        in.read(integer, 0, integer.length);
        return ByteUtil.byteArrayToInt(integer);
    }

    private void writeInteger(int value, FileChannel out) throws IOException {
        byte[] valueBytes = ByteUtil.intToByteArray(value);
        out.write(ByteBuffer.wrap(valueBytes));
    }

}