package com.tokenbrowser.crypto.signal.store;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tokenbrowser.crypto.util.ByteUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionState;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

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
    public SessionRecord loadSession(@NonNull final SignalProtocolAddress address) {
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
    public void storeSession(@NonNull final SignalProtocolAddress address, @NonNull final SessionRecord record) {
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
    public boolean containsSession(final SignalProtocolAddress address) {
        return getSessionFile(address).exists() &&
                loadSession(address).getSessionState().hasSenderChain();
    }

    @Override
    public void deleteSession(final SignalProtocolAddress address) {
        getSessionFile(address).delete();
    }

    @Override
    public void deleteAllSessions(final String name) {
        List<Integer> devices = getSubDeviceSessions(name);

        deleteSession(new SignalProtocolAddress(name, DEFAULT_DEVICE_ID));

        for (int device : devices) {
            deleteSession(new SignalProtocolAddress(name, device));
        }
    }

    @Override
    public List<Integer> getSubDeviceSessions(final String name) {
        final String recipientId = name.split(":")[0];

        final List<Integer> results = new LinkedList<>();
        final File parent = getSessionDirectory();
        final String[] children = parent.list();

        if (children == null) {
            return results;
        }

        for (final String child : children) {
            final String[] parts = child.split("[.]", 2);
            final String sessionRecipientId = parts[0];

            if (sessionRecipientId.equals(recipientId) && parts.length > 1) {
                results.add(Integer.parseInt(parts[1]));
            }
        }

        return results;
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

    private File getSessionFile(final SignalProtocolAddress address) {
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

    private String getSessionName(final SignalProtocolAddress address) {
        final String recipientId = address.getName();
        final int deviceId = address.getDeviceId();

        return recipientId + (deviceId == SignalServiceAddress.DEFAULT_DEVICE_ID ? "" : "." + deviceId);
    }

    private @Nullable SignalProtocolAddress getAddressName(final File sessionFile) {
        final String[] parts = sessionFile.getName().split("[.]");
        final String recipientId = parts[0];

        final int deviceId
            = parts.length > 1
                ? Integer.parseInt(parts[1])
                : SignalServiceAddress.DEFAULT_DEVICE_ID;

        return new SignalProtocolAddress(recipientId, deviceId);
    }

    private byte[] readBlob(final FileInputStream in) throws IOException {
        int length       = readInteger(in);
        byte[] blobBytes = new byte[length];

        in.read(blobBytes, 0, blobBytes.length);
        return blobBytes;
    }

    private void writeBlob(final byte[] blobBytes, final FileChannel out) throws IOException {
        writeInteger(blobBytes.length, out);
        out.write(ByteBuffer.wrap(blobBytes));
    }

    private int readInteger(final FileInputStream in) throws IOException {
        byte[] integer = new byte[4];
        in.read(integer, 0, integer.length);
        return ByteUtil.byteArrayToInt(integer);
    }

    private void writeInteger(final int value, final FileChannel out) throws IOException {
        byte[] valueBytes = ByteUtil.intToByteArray(value);
        out.write(ByteBuffer.wrap(valueBytes));
    }

}