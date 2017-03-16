package com.tokenbrowser.manager.store;


import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class TokenMigration implements RealmMigration {
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
    }
}
