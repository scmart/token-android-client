package com.tokenbrowser.manager.store;


import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class TokenMigration implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, final long newVersion) {

        final RealmSchema schema = realm.getSchema();

        // Migrate to version 1: Add a new field on User.
        if (oldVersion == 0) {
            schema.get("User")
                    .addField("cacheTimestamp", long.class);
            oldVersion++;
        }

        // Migrate to version 2: Change owner_address to token_id
        if (oldVersion == 1) {
            schema.get("User")
                    .renameField("owner_address", "token_id");
            oldVersion++;
        }
    }
}
