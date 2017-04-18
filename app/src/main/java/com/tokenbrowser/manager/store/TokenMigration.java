/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.manager.store;


import com.tokenbrowser.crypto.HDWallet;

import java.io.File;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class TokenMigration implements RealmMigration {

    private final HDWallet wallet;

    public TokenMigration(final HDWallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, final long newVersion) {

        final RealmSchema schema = realm.getSchema();

        // Migrate to version 1:
        // Add a new field on User.
        if (oldVersion == 0) {
            schema.get("User")
                    .addField("cacheTimestamp", long.class);
            oldVersion++;
        }

        // Migrate to version 2:
        // Add PendingMessage table
        if (oldVersion == 1) {
            schema.create("PendingMessage")
                    .addField("privateKey", String.class, FieldAttribute.PRIMARY_KEY)
                    .addRealmObjectField("receiver", schema.get("User"))
                    .addRealmObjectField("sofaMessage", schema.get("SofaMessage"));
            oldVersion++;
        }

        // Migrate to version 3:
        // Add attachmentFilename to SofaMessage table
        if (oldVersion == 2) {
            schema.get("SofaMessage")
                    .addField("attachmentFilename", String.class);
            oldVersion++;
        }

        // Migrate to version 4:
        // Add support for reputation to User.
        if (oldVersion == 3) {
            schema.get("User")
                    .addField("reputation_score", Double.class)
                    .addField("review_count", int.class);
            oldVersion++;
        }

        // Migrate to version 5:
        // Move the custom data to top level on a User
        if (oldVersion == 4) {
            final RealmObjectSchema userSchema = schema.get("User");
            userSchema
                    .addField("about", String.class)
                    .addField("avatar", String.class)
                    .addField("location", String.class)
                    .addField("name", String.class)
                    .transform(obj -> {
                        final DynamicRealmObject customUserInfo = obj.getObject("customUserInfo");
                        obj.set("about", customUserInfo.getString("about"));
                        obj.set("avatar", customUserInfo.getString("avatar"));
                        obj.set("location", customUserInfo.getString("location"));
                        obj.set("name", customUserInfo.getString("name"));
                    })
                    .removeField("customUserInfo");
            oldVersion++;
        }

        // Migrate to version 6:
        // Rename attachmentFilename to attachmentFilePath
        if (oldVersion == 5) {
            final RealmObjectSchema sofaMessageSchema = schema.get("SofaMessage");
            sofaMessageSchema.renameField("attachmentFilename", "attachmentFilePath");
            oldVersion++;
        }

        // Migrate to version 7:
        // Add reference to Sender; this deleted all old messages
        // Migration is too much effort considering we're yet to go live.
        if (oldVersion == 6) {
            final RealmObjectSchema sofaMessageSchema = schema.get("SofaMessage");
            sofaMessageSchema
                    .removeField("sentByLocal")
                    .addRealmObjectField("sender", schema.get("User"));
            oldVersion++;
        }

        // Migrate to version 8:
        // Encrypt and shard
        if (oldVersion == 7) {
            realm.writeEncryptedCopyTo(new File(realm.getPath(), this.wallet.getOwnerAddress()), this.wallet.generateDatabaseEncryptionKey());
            oldVersion++;
        }
    }
}
