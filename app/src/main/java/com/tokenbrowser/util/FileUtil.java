package com.tokenbrowser.util;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.tokenbrowser.model.sofa.OutgoingAttachment;
import com.tokenbrowser.view.BaseApplication;

import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class FileUtil {

    public OutgoingAttachment saveFileFromUri(final Context context, final Uri uri) throws IOException {
        final InputStream inputStream = BaseApplication.get().getContentResolver().openInputStream(uri);
        final String mimeType = context.getContentResolver().getType(uri);
        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        final String fileExtension = mimeTypeMap.getExtensionFromMimeType(mimeType);
        final String fileName = String.format("%s.%s", UUID.randomUUID().toString(), fileExtension);
        final File destFile = new File(BaseApplication.get().getFilesDir(), fileName);
        final File file = writeToFileFromInputStream(destFile, inputStream);
        return new OutgoingAttachment()
                .setOutgoingAttachment(file)
                .setMimeType(mimeType);

    }

    private File writeToFileFromInputStream(final File file, final InputStream inputStream) throws IOException {
        final BufferedSink sink = Okio.buffer(Okio.sink(file));
        final Source source = Okio.source(inputStream);
        sink.writeAll(source);
        sink.close();
        return file;
    }

    public File writeAttachmentToFileFromMessageReceiver(final SignalServiceAttachmentPointer attachment,
                                                         final SignalServiceMessageReceiver messageReceiver) {
        File file = null;
        try {
            final String fileName = String.format("%d.jpg", attachment.getId());
            file = new File(BaseApplication.get().getCacheDir(), fileName);
            final InputStream stream = messageReceiver.retrieveAttachment(attachment, file);
            final File destFile = new File(BaseApplication.get().getFilesDir(), fileName);
            return writeToFileFromInputStream(destFile, stream);
        } catch (IOException | InvalidMessageException e) {
            LogUtil.e(getClass(), "Error during writing attachment to file " + e.getMessage());
            return null;
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }
}
